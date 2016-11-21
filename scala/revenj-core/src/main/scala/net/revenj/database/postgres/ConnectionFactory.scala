package net.revenj.database.postgres

import java.io.IOException
import java.net.ConnectException
import java.sql.SQLException
import java.util
import java.util.{Properties, TimeZone}

import org.postgresql.PGProperty
import org.postgresql.core._
import org.postgresql.sspi.ISSPIClient
import org.postgresql.util._

private [revenj] object ConnectionFactory {
  private val AUTH_REQ_OK = 0
  private val AUTH_REQ_PASSWORD = 3
  private val AUTH_REQ_CRYPT = 4
  private val AUTH_REQ_MD5 = 5
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
      case e: ReflectiveOperationException =>
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

    val socketFactory = SocketFactoryFactory.getSocketFactory(info)
    //
    // Establish a connection.
    //
    var newStream: PGStream = null
    try {
      newStream = new PGStream(socketFactory, hostSpec, connectTimeout)
      // Construct and send an ssl startup packet if requested.
      if (trySSL) {
        newStream = enableSSL(newStream, requireSSL, logger, info, connectTimeout)
      }
      // Set the socket timeout if the "socketTimeout" property has been set.
      val socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info)
      if (socketTimeout > 0) {
        newStream.getSocket.setSoTimeout(socketTimeout * 1000)
      }
      // Enable TCP keep-alive probe if required.
      newStream.getSocket.setKeepAlive(true)
      val receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info)
      if (receiveBufferSize > -1 && receiveBufferSize > 0) newStream.getSocket.setReceiveBufferSize(receiveBufferSize)
      val sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info)
      if (sendBufferSize > -1 && sendBufferSize > 0) newStream.getSocket.setSendBufferSize(sendBufferSize)
      val paramList = new util.ArrayList[Array[String]]
      paramList.add(Array[String]("user", user))
      paramList.add(Array[String]("database", database))
      paramList.add(Array[String]("client_encoding", "UTF8"))
      paramList.add(Array[String]("DateStyle", "ISO"))
      paramList.add(Array[String]("TimeZone", createPostgresTimeZone))
      paramList.add(Array[String]("extra_float_digits", "3"))
      val appName = PGProperty.APPLICATION_NAME.get(info)
      if (appName != null) paramList.add(Array[String]("application_name", appName))
      else paramList.add(Array[String]("application_name", "Revenj"))
      val currentSchema = PGProperty.CURRENT_SCHEMA.get(info)
      if (currentSchema != null) {
        paramList.add(Array[String]("search_path", currentSchema))
      }
      sendStartupPacket(newStream, paramList, logger)
      doAuthentication(newStream, hostSpec.getHost, user, password, info, logger)
      readStartupMessages(newStream, logger)

      newStream
    } catch {
      case upe: ConnectionFactory.UnsupportedProtocolException =>
        closeStream(newStream)
        throw new PSQLException(GT.tr("Unsupported protocol."), PSQLState.CONNECTION_UNABLE_TO_CONNECT, upe)
      case cex: ConnectException =>
        throw new PSQLException(GT.tr("Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.", hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, cex)
      case ioe: IOException =>
        closeStream(newStream)
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

  private def enableSSL(pgStream: PGStream, requireSSL: Boolean, logger: Logger, info: Properties, connectTimeout: Int): PGStream = {
    // Send SSL request packet
    pgStream.sendInteger4(8)
    pgStream.sendInteger2(1234)
    pgStream.sendInteger2(5679)
    pgStream.flush()
    // Now get the response from the backend, one of N, E, S.
    val beresp = pgStream.receiveChar
    beresp match {
      case 'E' =>
        // Server doesn't even know about the SSL handshake protocol
        if (requireSSL) throw new PSQLException(GT.tr("The server does not support SSL."), PSQLState.CONNECTION_REJECTED)
        // We have to reconnect to continue.
        pgStream.close()
        new PGStream(pgStream.getSocketFactory, pgStream.getHostSpec, connectTimeout)
      case 'N' =>
        if (requireSSL) throw new PSQLException(GT.tr("The server does not support SSL."), PSQLState.CONNECTION_REJECTED)
        pgStream
      case 'S' =>
        // Server supports ssl
        org.postgresql.ssl.MakeSSL.convert(pgStream, info, logger)
        pgStream
      case _ =>
        throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection."), PSQLState.PROTOCOL_VIOLATION)
    }
  }

  private def sendStartupPacket(pgStream: PGStream, params: util.List[Array[String]], logger: Logger): Unit = {
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
    pgStream.sendInteger4(length)
    pgStream.sendInteger2(3) // protocol major
    pgStream.sendInteger2(0) // protocol minor
    encodedParams foreach { ep =>
      pgStream.send(ep)
      pgStream.sendChar(0)
    }
    pgStream.sendChar(0)
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
        pgStream.receiveChar match {
          case 'E' =>
            // The most common one to be thrown here is:
            // "User authentication failed"
            //
            val l_elen = pgStream.receiveInteger4
            if (l_elen > 30000) {
              // if the error length is > than 30000 we assume this is really a v2 protocol
              // server, so trigger fallback.
              throw new ConnectionFactory.UnsupportedProtocolException
            }
            val errorMsg = new ServerErrorMessage(pgStream.receiveString(l_elen - 4), logger.getLogLevel)
            throw new PSQLException(errorMsg)
          case 'R' =>
            // Authentication request.
            // Get the message length
            val l_msgLen = pgStream.receiveInteger4
            // Get the type of request
            val areq = pgStream.receiveInteger4
            // Process the request.
            areq match {
              case AUTH_REQ_CRYPT =>
                val salt = pgStream.receive(2)
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val encodedResult = UnixCrypt.crypt(salt, password.getBytes("UTF-8"))
                pgStream.sendChar('p')
                pgStream.sendInteger4(4 + encodedResult.length + 1)
                pgStream.send(encodedResult)
                pgStream.sendChar(0)
                pgStream.flush()
              case AUTH_REQ_MD5 =>
                val md5Salt = pgStream.receive(4)
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val digest = MD5Digest.encode(user.getBytes("UTF-8"), password.getBytes("UTF-8"), md5Salt)
                pgStream.sendChar('p')
                pgStream.sendInteger4(4 + digest.length + 1)
                pgStream.send(digest)
                pgStream.sendChar(0)
                pgStream.flush()
              case AUTH_REQ_PASSWORD =>
                if (password == null) throw new PSQLException(GT.tr("The server requested password-based authentication, but no password was provided."), PSQLState.CONNECTION_REJECTED)
                val encodedPassword = password.getBytes("UTF-8")
                pgStream.sendChar('p')
                pgStream.sendInteger4(4 + encodedPassword.length + 1)
                pgStream.send(encodedPassword)
                pgStream.sendChar(0)
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
                  if (logger.logDebug) logger.debug(s"Using SSPI: $useSSPI, gsslib=$gsslib and SSPI support detected")
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
                isAuthenticating = false
              case _ =>
                throw new PSQLException(GT.tr("The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", Integer.valueOf(areq)), PSQLState.CONNECTION_REJECTED)
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

  private def readStartupMessages(pgStream: PGStream, logger: Logger): Unit = {
    var isEnd = false
    while (!isEnd) {
      pgStream.receiveChar match {
        case 'Z' =>
          if (pgStream.receiveInteger4 != 5) throw new IOException("unexpected length of ReadyForQuery packet")
          pgStream.receiveChar
          isEnd = true
        case 'K' =>
          val l_msgLen = pgStream.receiveInteger4
          if (l_msgLen != 12) throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
          pgStream.receiveInteger4
          pgStream.receiveInteger4
        case 'E' =>
          // Error
          val l_elen = pgStream.receiveInteger4
          val l_errorMsg = new ServerErrorMessage(pgStream.receiveString(l_elen - 4), logger.getLogLevel)
          throw new PSQLException(l_errorMsg)
        case 'N' =>
          // Warning
          val l_nlen = pgStream.receiveInteger4
          pgStream.receiveString(l_nlen - 4)
        case 'S' =>
          // ParameterStatus
          pgStream.receiveInteger4
          val name = pgStream.receiveString
          val value = pgStream.receiveString
          if (name == "client_encoding") {
            if (value != "UTF8") throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
            pgStream.setEncoding(Encoding.getDatabaseEncoding("UTF8"))
          }
        case _ =>
          throw new PSQLException(GT.tr("Protocol error.  Session setup failed."), PSQLState.PROTOCOL_VIOLATION)
      }
    }
  }
}
