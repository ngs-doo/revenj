/*-------------------------------------------------------------------------
*
* Copyright (c) 2003-2016, PostgreSQL Global Development Group
* Copyright (c) 2004, Open Cloud Limited.
*
*
*-------------------------------------------------------------------------
*/
package org.postgresql.core.v3

import org.postgresql.PGProperty
import org.postgresql.core.Encoding
import org.postgresql.core.Logger
import org.postgresql.core.PGStream
import org.postgresql.core.ProtocolConnection
import org.postgresql.core.ServerVersion
import org.postgresql.core.SetupQueryRunner
import org.postgresql.core.Utils
import org.postgresql.core.v2.SocketFactoryFactory
import org.postgresql.hostchooser.GlobalHostStatusTracker
import org.postgresql.hostchooser.HostRequirement
import org.postgresql.hostchooser.HostStatus
import org.postgresql.sspi.ISSPIClient
import org.postgresql.util.GT
import org.postgresql.util.HostSpec
import org.postgresql.util.MD5Digest
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import org.postgresql.util.PSQLWarning
import org.postgresql.util.ServerErrorMessage
import org.postgresql.util.UnixCrypt
import java.io.IOException
import java.net.ConnectException
import java.sql.SQLException
import java.util
import java.util.Properties
import java.util.TimeZone

object RevenjConnectionFactory {
  private val AUTH_REQ_OK = 0
  private val AUTH_REQ_KRB4 = 1
  private val AUTH_REQ_KRB5 = 2
  private val AUTH_REQ_PASSWORD = 3
  private val AUTH_REQ_CRYPT = 4
  private val AUTH_REQ_MD5 = 5
  private val AUTH_REQ_SCM = 6
  private val AUTH_REQ_GSS = 7
  private val AUTH_REQ_GSS_CONTINUE = 8
  private val AUTH_REQ_SSPI = 9

  /**
    * Marker exception; thrown when we want to fall back to using V2.
    */
  private class UnsupportedProtocolException extends IOException {}

  private def createSSPI(pgStream: PGStream, spnServiceClass: String, enableNegotiate: Boolean, logger: Logger): ISSPIClient = {
    try {
      val c = Class.forName("org.postgresql.sspi.SSPIClient")
      val cArg = Array[Class[_]](classOf[PGStream], classOf[String], classOf[Boolean], classOf[Logger])
      c.getDeclaredConstructor(cArg:_*).newInstance(Array[AnyRef](pgStream, spnServiceClass, enableNegotiate.asInstanceOf[AnyRef], logger):_*).asInstanceOf[ISSPIClient]
    } catch {
      case e: Exception =>
        // This catched quite a lot exceptions, but until Java 7 there is no ReflectiveOperationException
        throw new IllegalStateException("Unable to load org.postgresql.sspi.SSPIClient." + " Please check that SSPIClient is included in your pgjdbc distribution.", e)
    }
  }

  def openConnection(hostSpec: HostSpec, user: String, password: String, database: String, info: Properties): PGStream = {
    // Extract interesting values from the info properties:
    // - the SSL setting
    val logger = new Logger
    var requireSSL = false
    var trySSL = false
    val sslmode = PGProperty.SSL_MODE.get(info)
    if (sslmode == null) {
      // Fall back to the ssl property
      // assume "true" if the property is set but empty
      trySSL = PGProperty.SSL.getBoolean(info) || "" == PGProperty.SSL.get(info)
      requireSSL = trySSL
    } else if ("disable" == sslmode) {
      requireSSL = false
      trySSL = false
    } else if ("require" == sslmode || "verify-ca" == sslmode || "verify-full" == sslmode) {
      requireSSL = true
      trySSL = true
    } else throw new PSQLException(GT.tr("Invalid sslmode value: {0}", sslmode), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
    // NOTE: To simplify this code, it is assumed that if we are
    // using the V3 protocol, then the database is at least 7.4. That
    // eliminates the need to check database versions and maintain
    // backward-compatible code here.
    //
    // Change by Chris Smith <cdsmith@twu.net>
    val connectTimeout = PGProperty.CONNECT_TIMEOUT.getInt(info) * 1000
    // - the targetServerType setting
    val targetServerType = {
      try {
        HostRequirement.valueOf(info.getProperty("targetServerType", HostRequirement.any.name))
      } catch {
        case ex: IllegalArgumentException =>
          throw new PSQLException(GT.tr("Invalid targetServerType value: {0}", info.getProperty("targetServerType")), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
      }
    }
    val socketFactory = SocketFactoryFactory.getSocketFactory(info)
    //
    // Establish a connection.
    //
    var newStream: PGStream = null
    try {
      newStream = new PGStream(socketFactory, hostSpec, connectTimeout)
      // Construct and send an ssl startup packet if requested.
      if (trySSL) {
        newStream = enableSSL(newStream, requireSSL, info, logger, connectTimeout)
      }
      // Set the socket timeout if the "socketTimeout" property has been set.
      val socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info)
      if (socketTimeout > 0) {
        newStream.getSocket.setSoTimeout(socketTimeout * 1000)
      }
      // Enable TCP keep-alive probe if required.
      newStream.getSocket.setKeepAlive(true)
      // Try to set SO_SNDBUF and SO_RECVBUF socket options, if requested.
      // If receiveBufferSize and send_buffer_size are set to a value greater
      // than 0, adjust. -1 means use the system default, 0 is ignored since not
      // supported.
      // Set SO_RECVBUF read buffer size
      val receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info)
      if (receiveBufferSize > -1) {
        // value of 0 not a valid buffer size value
        if (receiveBufferSize > 0) newStream.getSocket.setReceiveBufferSize(receiveBufferSize)
        else logger.info("Ignore invalid value for receiveBufferSize: " + receiveBufferSize)
      }
      // Set SO_SNDBUF write buffer size
      val sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info)
      if (sendBufferSize > -1) if (sendBufferSize > 0) newStream.getSocket.setSendBufferSize(sendBufferSize)
      else logger.info("Ignore invalid value for sendBufferSize: " + sendBufferSize)
      logger.info("Receive Buffer Size is " + newStream.getSocket.getReceiveBufferSize)
      logger.info("Send Buffer Size is " + newStream.getSocket.getSendBufferSize)
      val paramList = new util.ArrayList[Array[String]]
      paramList.add(Array[String]("user", user))
      paramList.add(Array[String]("database", database))
      paramList.add(Array[String]("client_encoding", "UTF8"))
      paramList.add(Array[String]("DateStyle", "ISO"))
      paramList.add(Array[String]("TimeZone", createPostgresTimeZone))
      val assumeMinServerVersion: String = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info)
      if (Utils.parseServerVersionStr(assumeMinServerVersion) >= ServerVersion.v9_0.getVersionNum) {
        // User is explicitly telling us this is a 9.0+ server so set properties here:
        paramList.add(Array[String]("extra_float_digits", "3"))
        val appName = PGProperty.APPLICATION_NAME.get(info)
        if (appName != null) paramList.add(Array[String]("application_name", appName))
      } else {
        // User has not explicitly told us that this is a 9.0+ server so stick to old default:
        paramList.add(Array[String]("extra_float_digits", "2"))
      }
      val currentSchema = PGProperty.CURRENT_SCHEMA.get(info)
      if (currentSchema != null) {
        paramList.add(Array[String]("search_path", currentSchema))
      }
      sendStartupPacket(newStream, paramList, logger)
      // Do authentication (until AuthenticationOk).
      doAuthentication(newStream, hostSpec.getHost, user, password, info, logger)
      // Do final startup.
      val protoConnection = new ProtocolConnectionImpl(newStream, user, database, info, logger, connectTimeout)
      readStartupMessages(newStream, protoConnection, logger)
      // Check Master or Slave
      var hostStatus = HostStatus.ConnectOK
      if (targetServerType ne HostRequirement.any) hostStatus = if (isMaster(protoConnection, logger)) HostStatus.Master
      else HostStatus.Slave
      GlobalHostStatusTracker.reportHostStatus(hostSpec, hostStatus)
      if (!targetServerType.allowConnectingTo(hostStatus)) {
        protoConnection.close()
        throw new PSQLException(GT.tr("Could not find a server with specified targetServerType: {0}", targetServerType), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
      }
      runInitialQueries(protoConnection, info, logger)
      // And we're done.
      newStream

    } catch {
      case upe: RevenjConnectionFactory.UnsupportedProtocolException =>
        // Swallow this and return null so ConnectionFactory tries the next protocol.
        if (logger.logDebug) logger.debug("Protocol not supported, abandoning connection.")
        closeStream(newStream)
        throw new PSQLException(GT.tr("Unsupported protocol."), PSQLState.CONNECTION_UNABLE_TO_CONNECT, upe)
      case cex: ConnectException =>
        // Added by Peter Mount <peter@retep.org.uk>
        // ConnectException is thrown when the connection cannot be made.
        // we trap this an return a more meaningful message for the end user
        GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail)
        throw new PSQLException(GT.tr("Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.", hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, cex)
      case ioe: IOException =>
        closeStream(newStream)
        GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail)
        throw new PSQLException(GT.tr("The connection attempt failed."), PSQLState.CONNECTION_UNABLE_TO_CONNECT, ioe)
      case se: SQLException =>
        closeStream(newStream)
        throw se
    }
  }

  private def closeStream(newStream: PGStream): Unit = {
    if (newStream != null) {
      try {
        newStream.close()
      } catch {
        case ignore: IOException =>
      }
    }
  }

  /**
    * Convert Java time zone to postgres time zone. All others stay the same except that GMT+nn
    * changes to GMT-nn and vise versa.
    *
    * @return The current JVM time zone in postgresql format.
    */
  private def createPostgresTimeZone: String = {
    val tz = TimeZone.getDefault.getID
    if (tz.length <= 3 || !tz.startsWith("GMT")) tz
    else {
      val sign = tz.charAt(3)
      if (sign == '+') "GMT-" + tz.substring(4)
      else if (sign == '-') "GMT+" + tz.substring(4)
      else tz // unknown type
    }
  }

  private def enableSSL(pgStream: PGStream, requireSSL: Boolean, info: Properties, logger: Logger, connectTimeout: Int): PGStream = {
    if (logger.logDebug) logger.debug(" FE=> SSLRequest")
    // Send SSL request packet
    pgStream.SendInteger4(8)
    pgStream.SendInteger2(1234)
    pgStream.SendInteger2(5679)
    pgStream.flush()
    // Now get the response from the backend, one of N, E, S.
    val beresp: Int = pgStream.ReceiveChar
    beresp match {
      case 'E' =>
        if (logger.logDebug) logger.debug(" <=BE SSLError")
        // Server doesn't even know about the SSL handshake protocol
        if (requireSSL) throw new PSQLException(GT.tr("The server does not support SSL."), PSQLState.CONNECTION_REJECTED)
        // We have to reconnect to continue.
        pgStream.close()
        new PGStream(pgStream.getSocketFactory, pgStream.getHostSpec, connectTimeout)
      case 'N' =>
        if (logger.logDebug) logger.debug(" <=BE SSLRefused")
        // Server does not support ssl
        if (requireSSL) throw new PSQLException(GT.tr("The server does not support SSL."), PSQLState.CONNECTION_REJECTED)
        pgStream
      case 'S' =>
        if (logger.logDebug) logger.debug(" <=BE SSLOk")
        // Server supports ssl
        org.postgresql.ssl.MakeSSL.convert(pgStream, info, logger)
        pgStream
      case _ =>
        throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection."), PSQLState.PROTOCOL_VIOLATION)
    }
  }

  private def sendStartupPacket(pgStream: PGStream, params: util.List[Array[String]], logger: Logger): Unit = {
    if (logger.logDebug) {
      val details: StringBuilder = new StringBuilder
      var i = 0
      while (i < params.size) {
        if (i != 0) details.append(", ")
        details.append(params.get(i)(0))
        details.append("=")
        details.append(params.get(i)(1))
        i += 1
      }
      logger.debug(" FE=> StartupPacket(" + details + ")")
    }
    // Precalculate message length and encode params.
    var length = 4 + 4
    val encodedParams = new Array[Array[Byte]](params.size * 2)
    var i = 0
    while (i < params.size) {
      encodedParams(i * 2) = params.get(i)(0).getBytes("UTF-8")
      encodedParams(i * 2 + 1) = params.get(i)(1).getBytes("UTF-8")
      length += encodedParams(i * 2).length + 1 + encodedParams(i * 2 + 1).length + 1
      i += 1
    }
    length += 1 // Terminating \0
    // Send the startup message.
    pgStream.SendInteger4(length)
    pgStream.SendInteger2(3) // protocol major
    pgStream.SendInteger2(0) // protocol minor
    encodedParams foreach { encodedParam =>
      pgStream.Send(encodedParam)
      pgStream.SendChar(0)
    }
    pgStream.SendChar(0)
    pgStream.flush()
  }

  private def doAuthentication(pgStream: PGStream, host: String, user: String, password: String, info: Properties, logger: Logger): Unit = {
    // Now get the response from the backend, either an error message
    // or an authentication request
    /* SSPI negotiation state, if used */
    var sspiClient: ISSPIClient = null
    try {
      var isAuthenticating = true
      while (isAuthenticating) {
        val beresp = pgStream.ReceiveChar
        beresp match {
          case 'E' =>
            // An error occurred, so pass the error message to the
            // user.
            //
            // The most common one to be thrown here is:
            // "User authentication failed"
            //
            val l_elen: Int = pgStream.ReceiveInteger4
            if (l_elen > 30000) {
              // if the error length is > than 30000 we assume this is really a v2 protocol
              // server, so trigger fallback.
              throw new RevenjConnectionFactory.UnsupportedProtocolException
            }
            val errorMsg: ServerErrorMessage = new ServerErrorMessage(pgStream.ReceiveString(l_elen - 4), logger.getLogLevel)
            if (logger.logDebug) logger.debug(" <=BE ErrorMessage(" + errorMsg + ")")
            throw new PSQLException(errorMsg)
          case 'R' =>
            // Authentication request.
            // Get the message length
            val l_msgLen = pgStream.ReceiveInteger4
            // Get the type of request
            val areq = pgStream.ReceiveInteger4
            // Process the request.
            areq match {
              case AUTH_REQ_CRYPT =>
                val salt = pgStream.Receive(2)
                if (logger.logDebug) logger.debug(" <=BE AuthenticationReqCrypt(salt='" + new String(salt, "US-ASCII") + "')")
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val encodedResult = UnixCrypt.crypt(salt, password.getBytes("UTF-8"))
                if (logger.logDebug) logger.debug(" FE=> Password(crypt='" + new String(encodedResult, "US-ASCII") + "')")
                pgStream.SendChar('p')
                pgStream.SendInteger4(4 + encodedResult.length + 1)
                pgStream.Send(encodedResult)
                pgStream.SendChar(0)
                pgStream.flush()
              case AUTH_REQ_MD5 =>
                val md5Salt = pgStream.Receive(4)
                if (logger.logDebug) logger.debug(" <=BE AuthenticationReqMD5(salt=" + Utils.toHexString(md5Salt) + ")")
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val digest = MD5Digest.encode(user.getBytes("UTF-8"), password.getBytes("UTF-8"), md5Salt)
                if (logger.logDebug) logger.debug(" FE=> Password(md5digest=" + new String(digest, "US-ASCII") + ")")
                pgStream.SendChar('p')
                pgStream.SendInteger4(4 + digest.length + 1)
                pgStream.Send(digest)
                pgStream.SendChar(0)
                pgStream.flush()
              case AUTH_REQ_PASSWORD =>
                if (logger.logDebug) {
                  logger.debug(" <=BE AuthenticationReqPassword")
                  logger.debug(" FE=> Password(password=<not shown>)")
                }
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val encodedPassword = password.getBytes("UTF-8")
                pgStream.SendChar('p')
                pgStream.SendInteger4(4 + encodedPassword.length + 1)
                pgStream.Send(encodedPassword)
                pgStream.SendChar(0)
                pgStream.flush()
              case AUTH_REQ_GSS =>
              case AUTH_REQ_SSPI =>
                /*
                 * Use GSSAPI if requested on all platforms, via JSSE.
                 *
                 * For SSPI auth requests, if we're on Windows attempt native SSPI authentication if
                 * available, and if not disabled by setting a kerberosServerName. On other
                 * platforms, attempt JSSE GSSAPI negotiation with the SSPI server.
                 *
                 * Note that this is slightly different to libpq, which uses SSPI for GSSAPI where
                 * supported. We prefer to use the existing Java JSSE Kerberos support rather than
                 * going to native (via JNA) calls where possible, so that JSSE system properties
                 * etc continue to work normally.
                 *
                 * Note that while SSPI is often Kerberos-based there's no guarantee it will be; it
                 * may be NTLM or anything else. If the client responds to an SSPI request via
                 * GSSAPI and the other end isn't using Kerberos for SSPI then authentication will
                 * fail.
                 */
                val gsslib = PGProperty.GSS_LIB.get(info)
                val usespnego = PGProperty.USE_SPNEGO.getBoolean(info)
                var useSSPI = false
                /*
                 * Use SSPI if we're in auto mode on windows and have a request for SSPI auth, or if
                 * it's forced. Otherwise use gssapi. If the user has specified a Kerberos server
                 * name we'll always use JSSE GSSAPI.
                 */
                if (gsslib == "gssapi") logger.debug("Using JSSE GSSAPI, param gsslib=gssapi")
                else if (areq == AUTH_REQ_GSS && gsslib != "sspi") logger.debug("Using JSSE GSSAPI, gssapi requested by server and gsslib=sspi not forced")
                else {
                  /* Determine if SSPI is supported by the client */ sspiClient = createSSPI(pgStream, PGProperty.SSPI_SERVICE_CLASS.get(info), /* Use negotiation for SSPI, or if explicitly requested for GSS */ areq == AUTH_REQ_SSPI || (areq == AUTH_REQ_GSS && usespnego), logger)
                  useSSPI = sspiClient.isSSPISupported
                  if (logger.logDebug) logger.debug("SSPI support detected: " + useSSPI)
                  if (!useSSPI) {
                    /* No need to dispose() if no SSPI used */ sspiClient = null
                    if (gsslib == "sspi") throw new PSQLException("SSPI forced with gsslib=sspi, but SSPI not available; set loglevel=2 for details", PSQLState.CONNECTION_UNABLE_TO_CONNECT)
                  }
                  if (logger.logDebug) logger.debug("Using SSPI: " + useSSPI + ", gsslib=" + gsslib + " and SSPI support detected")
                }
                /* SSPI requested and detected as available */
                if (useSSPI) sspiClient.startSSPI()
                /* Use JGSS's GSSAPI for this request */
                else org.postgresql.gss.MakeGSS.authenticate(pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), logger, usespnego)
              case AUTH_REQ_GSS_CONTINUE =>
                /*
                 * Only called for SSPI, as GSS is handled by an inner loop in MakeGSS.
                 */
                sspiClient.continueSSPI(l_msgLen - 8)
              case AUTH_REQ_OK =>
                /* Cleanup after successful authentication */ if (logger.logDebug) logger.debug(" <=BE AuthenticationOk")
                isAuthenticating = false
              case _ =>
                if (logger.logDebug) logger.debug(" <=BE AuthenticationReq (unsupported type " + areq + ")")
                throw new PSQLException(GT.tr("The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", areq), PSQLState.CONNECTION_REJECTED)
            }
          case _ =>
            throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
        }
      }
    } finally {
      /* Cleanup after successful or failed authentication attempts */
      if (sspiClient != null) {
        try {
          sspiClient.dispose()
        } catch {
          case ex: RuntimeException =>
            logger.log("Unexpected error during SSPI context disposal", ex)
        }
      }
    }
  }

  private def readStartupMessages(pgStream: PGStream, protoConnection: ProtocolConnectionImpl, logger: Logger): Unit = {
    var isEnd = false
    while (!isEnd) {
      val beresp = pgStream.ReceiveChar
      beresp match {
        case 'Z' =>
          // Ready For Query; we're done.
          if (pgStream.ReceiveInteger4 != 5) throw new IOException("unexpected length of ReadyForQuery packet")
          val tStatus: Char = pgStream.ReceiveChar.toChar
          if (logger.logDebug) logger.debug(" <=BE ReadyForQuery(" + tStatus + ")")
          // Update connection state.
          tStatus match {
            case 'I' =>
              protoConnection.setTransactionState(ProtocolConnection.TRANSACTION_IDLE)
            case 'T' =>
              protoConnection.setTransactionState(ProtocolConnection.TRANSACTION_OPEN)
            case 'E' =>
              protoConnection.setTransactionState(ProtocolConnection.TRANSACTION_FAILED)
            case _ =>
              // Huh?
          }
          isEnd = true
        case 'K' =>
          // BackendKeyData
          val l_msgLen: Int = pgStream.ReceiveInteger4
          if (l_msgLen != 12) throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
          val pid: Int = pgStream.ReceiveInteger4
          val ckey: Int = pgStream.ReceiveInteger4
          if (logger.logDebug) logger.debug(" <=BE BackendKeyData(pid=" + pid + ",ckey=" + ckey + ")")
          protoConnection.setBackendKeyData(pid, ckey)
        case 'E' =>
          // Error
          val l_elen: Int = pgStream.ReceiveInteger4
          val l_errorMsg: ServerErrorMessage = new ServerErrorMessage(pgStream.ReceiveString(l_elen - 4), logger.getLogLevel)
          if (logger.logDebug) logger.debug(" <=BE ErrorMessage(" + l_errorMsg + ")")
          throw new PSQLException(l_errorMsg)
        case 'N' =>
          // Warning
          val l_nlen: Int = pgStream.ReceiveInteger4
          val l_warnMsg: ServerErrorMessage = new ServerErrorMessage(pgStream.ReceiveString(l_nlen - 4), logger.getLogLevel)
          if (logger.logDebug) logger.debug(" <=BE NoticeResponse(" + l_warnMsg + ")")
          protoConnection.addWarning(new PSQLWarning(l_warnMsg))
        case 'S' =>
          // ParameterStatus
          val l_len: Int = pgStream.ReceiveInteger4
          val name: String = pgStream.ReceiveString
          val value: String = pgStream.ReceiveString
          if (logger.logDebug) logger.debug(" <=BE ParameterStatus(" + name + " = " + value + ")")
          if (name == "server_version_num") protoConnection.setServerVersionNum(value.toInt)
          if (name == "server_version") protoConnection.setServerVersion(value)
          else if (name == "client_encoding") {
            if (value != "UTF8") throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
            pgStream.setEncoding(Encoding.getDatabaseEncoding("UTF8"))
          }
          else if (name == "standard_conforming_strings") if (value == "on") protoConnection.setStandardConformingStrings(true)
          else if (value == "off") protoConnection.setStandardConformingStrings(false)
          else throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
          else if (name == "integer_datetimes") if (value == "on") protoConnection.setIntegerDateTimes(true)
          else if (value == "off") protoConnection.setIntegerDateTimes(false)
          else throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
        case _ =>
          if (logger.logDebug) logger.debug("invalid message type=" + beresp.toChar)
          throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
      }
    }
  }

  private def runInitialQueries(protoConnection: ProtocolConnection, info: Properties, logger: Logger): Unit = {
    val assumeMinServerVersion = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info)
    if (Utils.parseServerVersionStr(assumeMinServerVersion) < ServerVersion.v9_0.getVersionNum) {
      val dbVersion = protoConnection.getServerVersionNum
      if (dbVersion >= ServerVersion.v9_0.getVersionNum) {
        SetupQueryRunner.run(protoConnection, "SET extra_float_digits = 3", false)
      }
      var appName = PGProperty.APPLICATION_NAME.get(info)
      if (appName == null) appName = "Revenj"
      if (dbVersion >= ServerVersion.v9_0.getVersionNum) {
        val sql = new java.lang.StringBuilder
        sql.append("SET application_name = '")
        Utils.escapeLiteral(sql, appName, protoConnection.getStandardConformingStrings)
        sql.append("'")
        SetupQueryRunner.run(protoConnection, sql.toString, false)
      }
    }
  }

  private def isMaster(protoConnection: ProtocolConnectionImpl, logger: Logger): Boolean = {
    val results = SetupQueryRunner.run(protoConnection, "show transaction_read_only", true)
    val value = protoConnection.getEncoding.decode(results(0))
    value.equalsIgnoreCase("off")
  }
}
