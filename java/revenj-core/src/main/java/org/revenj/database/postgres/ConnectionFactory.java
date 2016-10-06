/*-------------------------------------------------------------------------
*
* Copyright (c) 2003-2016, PostgreSQL Global Development Group
* Copyright (c) 2004, Open Cloud Limited.
*
*
*-------------------------------------------------------------------------
*/

package org.revenj.database.postgres;

import org.postgresql.PGProperty;
import org.postgresql.core.Encoding;
import org.postgresql.core.Logger;
import org.postgresql.core.PGStream;
import org.postgresql.core.ProtocolConnection;
import org.postgresql.core.ServerVersion;
import org.postgresql.core.SetupQueryRunner;
import org.postgresql.core.Utils;
import org.postgresql.core.v2.SocketFactoryFactory;
import org.postgresql.hostchooser.GlobalHostStatusTracker;
import org.postgresql.hostchooser.HostRequirement;
import org.postgresql.hostchooser.HostStatus;
import org.postgresql.sspi.ISSPIClient;
import org.postgresql.util.GT;
import org.postgresql.util.HostSpec;
import org.postgresql.util.MD5Digest;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.PSQLWarning;
import org.postgresql.util.ServerErrorMessage;
import org.postgresql.util.UnixCrypt;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.net.SocketFactory;

/**
 * ConnectionFactory implementation for version 3 (7.4+) connections.
 *
 * @author Oliver Jowett (oliver@opencloud.com), based on the previous implementation
 *         modified by Rikard Pavelic
 */
public class ConnectionFactory {
	private static final int AUTH_REQ_OK = 0;
	private static final int AUTH_REQ_PASSWORD = 3;
	private static final int AUTH_REQ_CRYPT = 4;
	private static final int AUTH_REQ_MD5 = 5;
	private static final int AUTH_REQ_GSS = 7;
	private static final int AUTH_REQ_GSS_CONTINUE = 8;
	private static final int AUTH_REQ_SSPI = 9;

	/**
	 * Marker exception; thrown when we want to fall back to using V2.
	 */
	private static class UnsupportedProtocolException extends IOException {
	}

	private static ISSPIClient createSSPI(PGStream pgStream,
								   String spnServiceClass,
								   boolean enableNegotiate,
								   Logger logger) {
		try {
			Class c = Class.forName("org.postgresql.sspi.SSPIClient");
			Class[] cArg = new Class[]{PGStream.class, String.class, boolean.class, Logger.class};
			return (ISSPIClient) c.getDeclaredConstructor(cArg)
					.newInstance(pgStream, spnServiceClass, enableNegotiate, logger);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to load org.postgresql.sspi.SSPIClient."
					+ " Please check that SSPIClient is included in your pgjdbc distribution.", e);
		}
	}

	public static PGStream openConnection(HostSpec hostSpec, String user, String password, String database, Properties info) throws SQLException {
		// Extract interesting values from the info properties:
		// - the SSL setting
		Logger logger = new Logger();
		boolean requireSSL;
		boolean trySSL;
		String sslmode = PGProperty.SSL_MODE.get(info);
		if (sslmode == null) { // Fall back to the ssl property
			// assume "true" if the property is set but empty
			requireSSL = trySSL = PGProperty.SSL.getBoolean(info) || "".equals(PGProperty.SSL.get(info));
		} else {
			if ("disable".equals(sslmode)) {
				requireSSL = trySSL = false;
			} else if ("require".equals(sslmode) || "verify-ca".equals(sslmode)
					|| "verify-full".equals(sslmode)) {
				requireSSL = trySSL = true;
			} else {
				throw new PSQLException(GT.tr("Invalid sslmode value: {0}", sslmode),
						PSQLState.CONNECTION_UNABLE_TO_CONNECT);
			}
		}

		int connectTimeout = PGProperty.CONNECT_TIMEOUT.getInt(info) * 1000;

		SocketFactory socketFactory = SocketFactoryFactory.getSocketFactory(info);


		PGStream newStream = null;

		try {
			newStream = new PGStream(socketFactory, hostSpec, connectTimeout);

			// Construct and send an ssl startup packet if requested.
			if (trySSL) {
				newStream = enableSSL(newStream, requireSSL, info, logger, connectTimeout);
			}

			// Set the socket timeout if the "socketTimeout" property has been set.
			int socketTimeout = PGProperty.SOCKET_TIMEOUT.getInt(info);
			if (socketTimeout > 0) {
				newStream.getSocket().setSoTimeout(socketTimeout * 1000);
			}

			// Enable TCP keep-alive probe if required.
			newStream.getSocket().setKeepAlive(true);

			int receiveBufferSize = PGProperty.RECEIVE_BUFFER_SIZE.getInt(info);
			if (receiveBufferSize > -1 && receiveBufferSize > 0) {
				newStream.getSocket().setReceiveBufferSize(receiveBufferSize);
			}
			int sendBufferSize = PGProperty.SEND_BUFFER_SIZE.getInt(info);
			if (sendBufferSize > -1 && sendBufferSize > 0) {
				newStream.getSocket().setSendBufferSize(sendBufferSize);
			}

			List<String[]> paramList = new ArrayList<String[]>();
			paramList.add(new String[]{"user", user});
			paramList.add(new String[]{"database", database});
			paramList.add(new String[]{"client_encoding", "UTF8"});
			paramList.add(new String[]{"DateStyle", "ISO"});
			paramList.add(new String[]{"TimeZone", createPostgresTimeZone()});
			String assumeMinServerVersion = PGProperty.ASSUME_MIN_SERVER_VERSION.get(info);
			// User is explicitly telling us this is a 9.0+ server so set properties here:
			paramList.add(new String[]{"extra_float_digits", "3"});
			String appName = PGProperty.APPLICATION_NAME.get(info);
			paramList.add(new String[]{"application_name", appName == null ? "Revenj" : appName});

			String currentSchema = PGProperty.CURRENT_SCHEMA.get(info);
			if (currentSchema != null) {
				paramList.add(new String[]{"search_path", currentSchema});
			}

			sendStartupPacket(newStream, paramList, logger);

			// Do authentication (until AuthenticationOk).
			doAuthentication(newStream, hostSpec.getHost(), user, password, info, logger);

			// Do final startup.
			readStartupMessages(newStream, logger);

			// And we're done.
			return newStream;
		} catch (UnsupportedProtocolException upe) {
			closeStream(newStream);
			throw new PSQLException(GT.tr("Unsupported protocol"), PSQLState.CONNECTION_UNABLE_TO_CONNECT, upe);
		} catch (ConnectException cex) {
			throw new PSQLException(GT.tr(
					"Connection to {0} refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.",
					hostSpec), PSQLState.CONNECTION_UNABLE_TO_CONNECT, cex);
		} catch (IOException ioe) {
			closeStream(newStream);
			throw new PSQLException(GT.tr("The connection attempt failed."),
					PSQLState.CONNECTION_UNABLE_TO_CONNECT, ioe);
		} catch (SQLException se) {
			closeStream(newStream);
			throw se;
		}
	}

	private static void closeStream(PGStream newStream) {
		if (newStream != null) {
			try {
				newStream.close();
			} catch (IOException ignore) {
			}
		}
	}

	/**
	 * Convert Java time zone to postgres time zone. All others stay the same except that GMT+nn
	 * changes to GMT-nn and vise versa.
	 *
	 * @return The current JVM time zone in postgresql format.
	 */
	private static String createPostgresTimeZone() {
		String tz = TimeZone.getDefault().getID();
		if (tz.length() <= 3 || !tz.startsWith("GMT")) {
			return tz;
		}
		char sign = tz.charAt(3);
		String start;
		if (sign == '+') {
			start = "GMT-";
		} else if (sign == '-') {
			start = "GMT+";
		} else {
			// unknown type
			return tz;
		}

		return start + tz.substring(4);
	}

	private static PGStream enableSSL(PGStream pgStream, boolean requireSSL, Properties info, Logger logger,
							   int connectTimeout) throws IOException, SQLException {
		// Send SSL request packet
		pgStream.SendInteger4(8);
		pgStream.SendInteger2(1234);
		pgStream.SendInteger2(5679);
		pgStream.flush();

		// Now get the response from the backend, one of N, E, S.
		int beresp = pgStream.ReceiveChar();
		switch (beresp) {
			case 'E':
				// Server doesn't even know about the SSL handshake protocol
				if (requireSSL) {
					throw new PSQLException(GT.tr("The server does not support SSL."),
							PSQLState.CONNECTION_REJECTED);
				}

				// We have to reconnect to continue.
				pgStream.close();
				return new PGStream(pgStream.getSocketFactory(), pgStream.getHostSpec(), connectTimeout);

			case 'N':
				// Server does not support ssl
				if (requireSSL) {
					throw new PSQLException(GT.tr("The server does not support SSL."),
							PSQLState.CONNECTION_REJECTED);
				}

				return pgStream;

			case 'S':
				// Server supports ssl
				org.postgresql.ssl.MakeSSL.convert(pgStream, info, logger);
				return pgStream;

			default:
				throw new PSQLException(GT.tr("An error occurred while setting up the SSL connection."),
						PSQLState.PROTOCOL_VIOLATION);
		}
	}

	private static void sendStartupPacket(PGStream pgStream, List<String[]> params, Logger logger)
			throws IOException {
		// Precalculate message length and encode params.
		int length = 4 + 4;
		byte[][] encodedParams = new byte[params.size() * 2][];
		for (int i = 0; i < params.size(); ++i) {
			encodedParams[i * 2] = params.get(i)[0].getBytes("UTF-8");
			encodedParams[i * 2 + 1] = params.get(i)[1].getBytes("UTF-8");
			length += encodedParams[i * 2].length + 1 + encodedParams[i * 2 + 1].length + 1;
		}

		length += 1; // Terminating \0

		// Send the startup message.
		pgStream.SendInteger4(length);
		pgStream.SendInteger2(3); // protocol major
		pgStream.SendInteger2(0); // protocol minor
		for (byte[] encodedParam : encodedParams) {
			pgStream.Send(encodedParam);
			pgStream.SendChar(0);
		}

		pgStream.SendChar(0);
		pgStream.flush();
	}

	private static void doAuthentication(PGStream pgStream, String host, String user, String password, Properties info,
								  Logger logger) throws IOException, SQLException {
		// Now get the response from the backend, either an error message
		// or an authentication request

		/* SSPI negotiation state, if used */
		ISSPIClient sspiClient = null;

		try {
			authloop:
			while (true) {
				int beresp = pgStream.ReceiveChar();

				switch (beresp) {
					case 'E':
						// An error occurred, so pass the error message to the
						// user.
						//
						// The most common one to be thrown here is:
						// "User authentication failed"
						//
						int l_elen = pgStream.ReceiveInteger4();
						if (l_elen > 30000) {
							// if the error length is > than 30000 we assume this is really a v2 protocol
							// server, so trigger fallback.
							throw new UnsupportedProtocolException();
						}

						ServerErrorMessage errorMsg =
								new ServerErrorMessage(pgStream.ReceiveString(l_elen - 4), logger.getLogLevel());
						throw new PSQLException(errorMsg);

					case 'R':
						// Authentication request.
						// Get the message length
						int l_msgLen = pgStream.ReceiveInteger4();

						// Get the type of request
						int areq = pgStream.ReceiveInteger4();

						// Process the request.
						switch (areq) {
							case AUTH_REQ_CRYPT: {
								byte[] salt = pgStream.Receive(2);

								if (password == null) {
									throw new PSQLException(
											GT.tr("The server requested password-based authentication, but no password was provided."),
											PSQLState.CONNECTION_REJECTED);
								}

								byte[] encodedResult = UnixCrypt.crypt(salt, password.getBytes("UTF-8"));

								pgStream.SendChar('p');
								pgStream.SendInteger4(4 + encodedResult.length + 1);
								pgStream.Send(encodedResult);
								pgStream.SendChar(0);
								pgStream.flush();

								break;
							}

							case AUTH_REQ_MD5: {
								byte[] md5Salt = pgStream.Receive(4);

								if (password == null) {
									throw new PSQLException(
											GT.tr("The server requested password-based authentication, but no password was provided."),
											PSQLState.CONNECTION_REJECTED);
								}

								byte[] digest = MD5Digest.encode(user.getBytes("UTF-8"), password.getBytes("UTF-8"), md5Salt);

								pgStream.SendChar('p');
								pgStream.SendInteger4(4 + digest.length + 1);
								pgStream.Send(digest);
								pgStream.SendChar(0);
								pgStream.flush();

								break;
							}

							case AUTH_REQ_PASSWORD: {
								if (logger.logDebug()) {
									logger.debug(" <=BE AuthenticationReqPassword");
									logger.debug(" FE=> Password(password=<not shown>)");
								}

								if (password == null) {
									throw new PSQLException(
											GT.tr("The server requested password-based authentication, but no password was provided."),
											PSQLState.CONNECTION_REJECTED);
								}

								byte[] encodedPassword = password.getBytes("UTF-8");

								pgStream.SendChar('p');
								pgStream.SendInteger4(4 + encodedPassword.length + 1);
								pgStream.Send(encodedPassword);
								pgStream.SendChar(0);
								pgStream.flush();

								break;
							}

							case AUTH_REQ_GSS:
							case AUTH_REQ_SSPI:
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
								final String gsslib = PGProperty.GSS_LIB.get(info);
								final boolean usespnego = PGProperty.USE_SPNEGO.getBoolean(info);

								boolean useSSPI = false;

								/*
								 * Use SSPI if we're in auto mode on windows and have a request for SSPI auth, or if
								 * it's forced. Otherwise use gssapi. If the user has specified a Kerberos server
								 * name we'll always use JSSE GSSAPI.
								 */
								if (gsslib.equals("gssapi")) {
									logger.debug("Using JSSE GSSAPI, param gsslib=gssapi");
								} else if (areq == AUTH_REQ_GSS && !gsslib.equals("sspi")) {
									logger.debug(
											"Using JSSE GSSAPI, gssapi requested by server and gsslib=sspi not forced");
								} else {
									/* Determine if SSPI is supported by the client */
									sspiClient = createSSPI(pgStream, PGProperty.SSPI_SERVICE_CLASS.get(info),
											/* Use negotiation for SSPI, or if explicitly requested for GSS */
											areq == AUTH_REQ_SSPI || (areq == AUTH_REQ_GSS && usespnego), logger);

									useSSPI = sspiClient.isSSPISupported();

									if (!useSSPI) {
										/* No need to dispose() if no SSPI used */
										sspiClient = null;

										if (gsslib.equals("sspi")) {
											throw new PSQLException(
													"SSPI forced with gsslib=sspi, but SSPI not available; set loglevel=2 for details",
													PSQLState.CONNECTION_UNABLE_TO_CONNECT);
										}
									}
								}

								if (useSSPI) {
									/* SSPI requested and detected as available */
									sspiClient.startSSPI();
								} else {
									/* Use JGSS's GSSAPI for this request */
									org.postgresql.gss.MakeGSS.authenticate(pgStream, host, user, password,
											PGProperty.JAAS_APPLICATION_NAME.get(info),
											PGProperty.KERBEROS_SERVER_NAME.get(info), logger, usespnego);
								}

								break;

							case AUTH_REQ_GSS_CONTINUE:
								/*
								 * Only called for SSPI, as GSS is handled by an inner loop in MakeGSS.
								 */
								sspiClient.continueSSPI(l_msgLen - 8);
								break;

							case AUTH_REQ_OK:

								break authloop; // We're done.

							default:
								throw new PSQLException(GT.tr(
										"The authentication type {0} is not supported. Check that you have configured the pg_hba.conf file to include the client''s IP address or subnet, and that it is using an authentication scheme supported by the driver.",
										areq), PSQLState.CONNECTION_REJECTED);
						}

						break;

					default:
						throw new PSQLException(GT.tr("Protocol error.  Session setup failed."),
								PSQLState.PROTOCOL_VIOLATION);
				}
			}
		} finally {
			/* Cleanup after successful or failed authentication attempts */
			if (sspiClient != null) {
				try {
					sspiClient.dispose();
				} catch (RuntimeException ex) {
					logger.log("Unexpected error during SSPI context disposal", ex);
				}

			}
		}

	}

	private static void readStartupMessages(PGStream pgStream, Logger logger) throws IOException, SQLException {
		while (true) {
			int beresp = pgStream.ReceiveChar();
			switch (beresp) {
				case 'Z':
					// Ready For Query; we're done.
					if (pgStream.ReceiveInteger4() != 5) {
						throw new IOException("unexpected length of ReadyForQuery packet");
					}

					pgStream.ReceiveChar();

					return;

				case 'K':
					// BackendKeyData
					int l_msgLen = pgStream.ReceiveInteger4();
					if (l_msgLen != 12) {
						throw new PSQLException(GT.tr("Protocol error.  Session setup failed."),
								PSQLState.PROTOCOL_VIOLATION);
					}

					pgStream.ReceiveInteger4();
					pgStream.ReceiveInteger4();
					break;

				case 'E':
					// Error
					int l_elen = pgStream.ReceiveInteger4();
					ServerErrorMessage l_errorMsg =
							new ServerErrorMessage(pgStream.ReceiveString(l_elen - 4), logger.getLogLevel());

					throw new PSQLException(l_errorMsg);

				case 'N':
					// Warning
					int l_nlen = pgStream.ReceiveInteger4();
					pgStream.ReceiveString(l_nlen - 4);
					break;

				case 'S':
					// ParameterStatus
					pgStream.ReceiveInteger4();
					String name = pgStream.ReceiveString();
					String value = pgStream.ReceiveString();

					if (logger.logDebug()) {
						logger.debug(" <=BE ParameterStatus(" + name + " = " + value + ")");
					}

					if (name.equals("client_encoding")) {
						if (!value.equals("UTF8")) {
							throw new PSQLException(GT.tr("Protocol error.  Session setup failed."),
									PSQLState.PROTOCOL_VIOLATION);
						}
						pgStream.setEncoding(Encoding.getDatabaseEncoding("UTF8"));
					}

					break;

				default:
					throw new PSQLException(GT.tr("Protocol error.  Session setup failed."),
							PSQLState.PROTOCOL_VIOLATION);
			}
		}
	}
}
