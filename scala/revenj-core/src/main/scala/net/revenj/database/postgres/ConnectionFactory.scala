package net.revenj.database.postgres

import java.io.IOException
import java.net.ConnectException
import java.sql.SQLException
import java.util
import java.util.{Properties, TimeZone}

import org.postgresql.PGProperty
import org.postgresql.core._
import org.postgresql.core.v3.QueryExecutorImpl
import org.postgresql.hostchooser.{GlobalHostStatusTracker, HostChooserFactory, HostRequirement, HostStatus}
import org.postgresql.jre8.sasl.ScramAuthenticator
import org.postgresql.sspi.ISSPIClient
import org.postgresql.util._

private [revenj] object ConnectionFactory {

  private val AUTH_REQ_OK = 0
  private val AUTH_REQ_PASSWORD = 3
  private val AUTH_REQ_MD5 = 5
  private val AUTH_REQ_GSS = 7
  private val AUTH_REQ_GSS_CONTINUE = 8
  private val AUTH_REQ_SSPI = 9
  private val AUTH_REQ_SASL = 10
  private val AUTH_REQ_SASL_CONTINUE = 11
  private val AUTH_REQ_SASL_FINAL = 12

  private def createSSPI(pgStream: PGStream, spnServiceClass: String, enableNegotiate: Boolean) = {
    try {
      val c = Class.forName("org.postgresql.sspi.SSPIClient").asInstanceOf[Class[ISSPIClient]]
      val cArg = Array[Class[_]](classOf[PGStream], classOf[String], classOf[Boolean])
      c.getDeclaredConstructor(cArg:_*).newInstance(Array[AnyRef](pgStream, spnServiceClass, enableNegotiate.asInstanceOf[AnyRef]):_*)
    } catch {
      case e: Exception =>
        // This catched quite a lot exceptions, but until Java 7 there is no ReflectiveOperationException
        throw new IllegalStateException("Unable to load org.postgresql.sspi.SSPIClient." + " Please check that SSPIClient is included in your pgjdbc distribution.", e)
    }
  }

  def openConnection(hostSpecs: Array[HostSpec], user: String, password: String, database: String, applicationName: Option[String], info: Properties): PGStream = { // Extract interesting values from the info properties:
    if (hostSpecs.length == 0) throw new PSQLException("No hosts detected in connection string", PSQLState.CONNECTION_DOES_NOT_EXIST)
    // - the SSL setting
    var requireSSL = false
    var trySSL = false
    val sslmode = PGProperty.SSL_MODE.get(info)
    if (sslmode == null) { // Fall back to the ssl property
      // assume "true" if the property is set but empty
      trySSL = PGProperty.SSL.getBoolean(info) || "" == PGProperty.SSL.get(info)
      requireSSL = trySSL
    } else if ("disable" == sslmode) {
      trySSL = false
      requireSSL = trySSL
    } else if ("require" == sslmode || "verify-ca" == sslmode || "verify-full" == sslmode) {
      trySSL = true
      requireSSL = trySSL
    } else throw new PSQLException(GT.tr("Invalid sslmode value: {0}", sslmode), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
    val requireTCPKeepAlive = PGProperty.TCP_KEEP_ALIVE.getBoolean(info)
    val connectTimeout = PGProperty.CONNECT_TIMEOUT.getInt(info) * 1000
    val targetServerTypeStr = PGProperty.TARGET_SERVER_TYPE.get(info)
    val targetServerType = {
      try {
        HostRequirement.getTargetServerType(targetServerTypeStr)
      } catch {
        case _: IllegalArgumentException =>
          throw new PSQLException(GT.tr("Invalid targetServerType value: {0}", targetServerTypeStr), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
      }
    }
    val socketFactory = SocketFactoryFactory.getSocketFactory(info)
    val hostChooser = HostChooserFactory.createHostChooser(hostSpecs, targetServerType, info)
    val hostIter = hostChooser.iterator
    val knownStates = new util.HashMap[HostSpec, HostStatus]()
    var result: Option[PGStream] = None
    while (result.isEmpty && hostIter.hasNext) {
      val candidateHost = hostIter.next
      val hostSpec = candidateHost.hostSpec
      // Note: per-connect-attempt status map is used here instead of GlobalHostStatusTracker
      // for the case when "no good hosts" match (e.g. all the hosts are known as "connectfail")
      // In that case, the system tries to connect to each host in order, thus it should not look into
      // GlobalHostStatusTracker
      val knownStatus = knownStates.get(hostSpec)
      if (knownStatus == null || candidateHost.targetServerType.allowConnectingTo(knownStatus)) {
        //
        // Establish a connection.
        //
        var newStream: PGStream = null
        try {
          newStream = new PGStream(socketFactory, hostSpec, connectTimeout)
          // Construct and send an ssl startup packet if requested.
          if (trySSL) newStream = enableSSL(newStream, requireSSL, info, connectTimeout)
          // Set the socket timeout if the "socketTimeout" property has been set.
          val socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info)
          if (socketTimeout > 0) newStream.getSocket.setSoTimeout(socketTimeout * 1000)
          // Enable TCP keep-alive probe if required.
          newStream.getSocket.setKeepAlive(requireTCPKeepAlive)
          // Try to set SO_SNDBUF and SO_RECVBUF socket options, if requested.
          // If receiveBufferSize and send_buffer_size are set to a value greater
          // than 0, adjust. -1 means use the system default, 0 is ignored since not
          // supported.
          // Set SO_RECVBUF read buffer size
          val receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info)
          if (receiveBufferSize > -1) { // value of 0 not a valid buffer size value
            if (receiveBufferSize > 0) newStream.getSocket.setReceiveBufferSize(receiveBufferSize)
          }
          // Set SO_SNDBUF write buffer size
          val sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info)
          if (sendBufferSize > -1) if (sendBufferSize > 0) newStream.getSocket.setSendBufferSize(sendBufferSize)
          val paramList = getParametersForStartup(user, database, applicationName, info)
          sendStartupPacket(newStream, paramList)
          // Do authentication (until AuthenticationOk).
          doAuthentication(newStream, hostSpec.getHost, user, password, info)
          val cancelSignalTimeout = PGProperty.CANCEL_SIGNAL_TIMEOUT.getInt(info) * 1000
          // Do final startup.
          val queryExecutor = new QueryExecutorImpl(newStream, user, database, cancelSignalTimeout, info)
          // Check Master or Secondary
          var hostStatus = HostStatus.ConnectOK
          if (candidateHost.targetServerType ne HostRequirement.any) hostStatus = if (isMaster(queryExecutor)) HostStatus.Master
          else HostStatus.Secondary
          GlobalHostStatusTracker.reportHostStatus(hostSpec, hostStatus)
          knownStates.put(hostSpec, hostStatus)
          if (!candidateHost.targetServerType.allowConnectingTo(hostStatus)) {
            queryExecutor.close()
          } else {
            runInitialQueries(queryExecutor, applicationName, info)
            result = Some(newStream)
          }
        } catch {
          case cex: ConnectException =>
            // Added by Peter Mount <peter@retep.org.uk>
            // ConnectException is thrown when the connection cannot be made.
            // we trap this an return a more meaningful message for the end user
            GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail)
            knownStates.put(hostSpec, HostStatus.ConnectFail)
            if (!hostIter.hasNext) { // still more addresses to try
              throw new PSQLException(GT.tr("Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.", hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, cex)
            }
          case ioe: IOException =>
            closeStream(newStream)
            GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail)
            knownStates.put(hostSpec, HostStatus.ConnectFail)
            if (!hostIter.hasNext) {
              throw new PSQLException(GT.tr("The connection attempt failed."), PSQLState.CONNECTION_UNABLE_TO_CONNECT, ioe)
            }
          case se: SQLException =>
            closeStream(newStream)
            GlobalHostStatusTracker.reportHostStatus(hostSpec, HostStatus.ConnectFail)
            knownStates.put(hostSpec, HostStatus.ConnectFail)
            if (!hostIter.hasNext) {
              throw se
            }
        }
      }
    }
    if (result.isEmpty) {
      throw new PSQLException(GT.tr("Could not find a server with specified targetServerType: {0}", targetServerType), PSQLState.CONNECTION_UNABLE_TO_CONNECT)
    }
    result.get
  }

  private def getParametersForStartup(user: String, database: String, applicationName: Option[String], info: Properties) = {
    val paramList = new util.ArrayList[Array[String]]
    paramList.add(Array[String]("user", user))
    paramList.add(Array[String]("database", database))
    paramList.add(Array[String]("client_encoding", "UTF8"))
    paramList.add(Array[String]("DateStyle", "ISO"))
    paramList.add(Array[String]("TimeZone", createPostgresTimeZone))
    val assumeVersion = ServerVersion.from(PGProperty.ASSUME_MIN_SERVER_VERSION.get(info))
    if (assumeVersion.getVersionNum >= ServerVersion.v9_0.getVersionNum) { // User is explicitly telling us this is a 9.0+ server so set properties here:
      paramList.add(Array[String]("extra_float_digits", "3"))
      val appName = applicationName.orElse(Option(PGProperty.APPLICATION_NAME.get(info)))
      if (appName.isDefined) {
        paramList.add(Array[String]("application_name", appName.get))
      }
    } else { // User has not explicitly told us that this is a 9.0+ server so stick to old default:
      paramList.add(Array[String]("extra_float_digits", "2"))
    }
    val replication = PGProperty.REPLICATION.get(info)
    if (replication != null && assumeVersion.getVersionNum >= ServerVersion.v9_4.getVersionNum) paramList.add(Array[String]("replication", replication))
    val currentSchema = PGProperty.CURRENT_SCHEMA.get(info)
    if (currentSchema != null) paramList.add(Array[String]("search_path", currentSchema))
    paramList
  }

  /**
    * Convert Java time zone to postgres time zone. All others stay the same except that GMT+nn
    * changes to GMT-nn and vise versa.
    *
    * @return The current JVM time zone in postgresql format.
    */
  private def createPostgresTimeZone: String = {
    val tz = TimeZone.getDefault.getID
    if (tz.length <= 3 || !tz.startsWith("GMT")) {
      tz
    } else {
      val sign = tz.charAt(3)
      val start = sign match {
        case '+' =>
          Some("GMT-")
        case '-' =>
          Some("GMT+")
        case _ =>
          None
      }
      if (start.isDefined) start.get + tz.substring(4)
      else tz
    }
  }

  private def enableSSL(pgStream: PGStream, requireSSL: Boolean, info: Properties, connectTimeout: Int) = {
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
        // Server does not support ssl
        if (requireSSL) throw new PSQLException(GT.tr("The server does not support SSL."), PSQLState.CONNECTION_REJECTED)
        pgStream
      case 'S' =>
        // Server supports ssl
        org.postgresql.ssl.MakeSSL.convert(pgStream, info)
        pgStream
      case _ =>
        throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection."), PSQLState.PROTOCOL_VIOLATION)
    }
  }

  private def sendStartupPacket(pgStream: PGStream, params: util.List[Array[String]]): Unit = {
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

    for (encodedParam <- encodedParams) {
      pgStream.send(encodedParam)
      pgStream.sendChar(0)
    }
    pgStream.sendChar(0)
    pgStream.flush()
  }

  private def doAuthentication(pgStream: PGStream, host: String, user: String, password: String, info: Properties): Unit = { // Now get the response from the backend, either an error message
    // or an authentication request
    var sspiClient: ISSPIClient = null
    var scramAuthenticator: ScramAuthenticator = null
    try {
      var authenticating = true
      while (authenticating) {
        val beresp = pgStream.receiveChar
        beresp match {
          case 'E' =>
            // An error occurred, so pass the error message to the
            // user.
            // The most common one to be thrown here is:
            // "User authentication failed"
            val l_elen = pgStream.receiveInteger4
            val errorMsg = new ServerErrorMessage(pgStream.receiveErrorString(l_elen - 4))
            throw new PSQLException(errorMsg)
          case 'R' =>
            // Authentication request.
            // Get the message length
            val l_msgLen = pgStream.receiveInteger4
            // Get the type of request
            val areq = pgStream.receiveInteger4
            // Process the request.
            areq match {
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
                if (areq != AUTH_REQ_GSS || gsslib != "gssapi") {
                  /* Determine if SSPI is supported by the client */ sspiClient = createSSPI(pgStream, PGProperty.SSPI_SERVICE_CLASS.get(info), /* Use negotiation for SSPI, or if explicitly requested for GSS */ areq == AUTH_REQ_SSPI || (areq == AUTH_REQ_GSS && usespnego))
                  useSSPI = sspiClient.isSSPISupported
                  if (!useSSPI) {
                    /* No need to dispose() if no SSPI used */ sspiClient = null
                    if (gsslib == "sspi") throw new PSQLException("SSPI forced with gsslib=sspi, but SSPI not available; set loglevel=2 for details", PSQLState.CONNECTION_UNABLE_TO_CONNECT)
                  }
                }
                if (useSSPI) /* SSPI requested and detected as available */ sspiClient.startSSPI()
                else /* Use JGSS's GSSAPI for this request */ org.postgresql.gss.MakeGSS.authenticate(pgStream, host, user, password, PGProperty.JAAS_APPLICATION_NAME.get(info), PGProperty.KERBEROS_SERVER_NAME.get(info), usespnego, PGProperty.JAAS_LOGIN.getBoolean(info))
                authenticating = false
              case AUTH_REQ_GSS_CONTINUE =>
                /*
                 * Only called for SSPI, as GSS is handled by an inner loop in MakeGSS.
                 */
                sspiClient.continueSSPI(l_msgLen - 8)
              case AUTH_REQ_SASL =>
                scramAuthenticator = new ScramAuthenticator(user, password, pgStream)
                scramAuthenticator.processServerMechanismsAndInit()
                scramAuthenticator.sendScramClientFirstMessage()
              case AUTH_REQ_SASL_CONTINUE =>
                scramAuthenticator.processServerFirstMessage(l_msgLen - 4 - 4)
              case AUTH_REQ_SASL_FINAL =>
                scramAuthenticator.verifyServerSignature(l_msgLen - 4 - 4)
              case AUTH_REQ_OK =>
                /* Cleanup after successful authentication */
                authenticating = false
              case _ =>
                throw new PSQLException(GT.tr("The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.", areq.asInstanceOf[AnyRef]), PSQLState.CONNECTION_REJECTED)
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
          case _: RuntimeException =>
        }
      }
    }
  }

  private def runInitialQueries(queryExecutor: QueryExecutor, applicationName: Option[String], info: Properties): Unit = {
    val assumeMinServerVersion = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info)
    if (Utils.parseServerVersionStr(assumeMinServerVersion) < ServerVersion.v9_0.getVersionNum) { // We already sent the parameter values in the StartupMessage so skip this
      val dbVersion = queryExecutor.getServerVersionNum
      if (dbVersion >= ServerVersion.v9_0.getVersionNum) SetupQueryRunner.run(queryExecutor, "SET extra_float_digits = 3", false)
      val appName = applicationName.orElse(Option(PGProperty.APPLICATION_NAME.get(info)))
      if (appName.isDefined && dbVersion >= ServerVersion.v9_0.getVersionNum) {
        val sql = new java.lang.StringBuilder
        sql.append("SET application_name = '")
        Utils.escapeLiteral(sql, appName.get, queryExecutor.getStandardConformingStrings)
        sql.append("'")
        SetupQueryRunner.run(queryExecutor, sql.toString, false)
      }
    }
  }

  private def isMaster(queryExecutor: QueryExecutor) = {
    val results = SetupQueryRunner.run(queryExecutor, "show transaction_read_only", true)
    val value = queryExecutor.getEncoding.decode(results(0))
    value.equalsIgnoreCase("off")
  }

  /**
    * Safely close the given stream.
    *
    * @param newStream The stream to close.
    */
  protected def closeStream(newStream: PGStream): Unit = {
    if (newStream != null) {
      try {
        newStream.close()
      } catch {
        case _: IOException =>
      }
    }
  }
}
