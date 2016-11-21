package org.revenj;

import org.postgresql.PGNotification;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.PGStream;
import org.revenj.database.postgres.ConnectionFactory;
import org.postgresql.util.HostSpec;
import org.revenj.extensibility.SystemState;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.converters.StringConverter;
import org.revenj.patterns.*;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class PostgresDatabaseNotification implements EagerNotification, Closeable {

	private final DataSource dataSource;
	private final Optional<DomainModel> domainModel;
	private final SystemState systemState;
	private final ServiceLocator locator;
	private final Properties properties;

	private final PublishSubject<NotifyInfo> subject = PublishSubject.create();
	private final Observable<NotifyInfo> notifications;

	private final ConcurrentMap<Class<?>, Repository> repositories = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, HashSet<Class<?>>> targets = new ConcurrentHashMap<>();

	private int retryCount;
	private final int maxTimeout;

	private boolean isClosed;
	private PGStream currentStream;

	public PostgresDatabaseNotification(
			DataSource dataSource,
			Optional<DomainModel> domainModel,
			Properties properties,
			SystemState systemState,
			ServiceLocator locator) {
		this.dataSource = dataSource;
		this.domainModel = domainModel;
		this.properties = properties;
		this.systemState = systemState;
		this.locator = locator;
		notifications = subject.asObservable();
		String timeoutValue = properties.getProperty("revenj.notifications.timeout");
		if (timeoutValue != null) {
			try {
				maxTimeout = Integer.parseInt(timeoutValue);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Error parsing notificationTimeout setting");
			}
		} else {
			maxTimeout = 1000;
		}
		if ("disabled".equals(properties.getProperty("revenj.notifications.status"))) {
			isClosed = true;
		} else if ("pooling".equals(properties.getProperty("revenj.notifications.type"))) {
			setupPooling();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> isClosed = true));
		} else {
			setupListening();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> isClosed = true));
		}
	}

	private void setupPooling() {
		if (dataSource == null) return;
		retryCount++;
		if (retryCount > 60) {
			retryCount = 30;
		}
		try {
			Connection connection = dataSource.getConnection();
			BaseConnection bc = null;
			if (connection instanceof BaseConnection) {
				bc = (BaseConnection) connection;
			} else {
				try {
					if (connection != null && connection.isWrapperFor(BaseConnection.class)) {
						bc = connection.unwrap(BaseConnection.class);
					}
				} catch (AbstractMethodError ignore) {
				}
				if (bc == null && properties.containsKey("revenj.jdbcUrl")) {
					String user = properties.getProperty("revenj.user");
					String pass = properties.getProperty("revenj.password");
					org.postgresql.Driver driver = new org.postgresql.Driver();
					Properties connProps = new Properties(properties);
					if (user != null && pass != null) {
						connProps.setProperty("user", user);
						connProps.setProperty("password", pass);
					}
					cleanupConnection(connection);
					connection = driver.connect(properties.getProperty("revenj.jdbcUrl"), connProps);
					if (connection instanceof BaseConnection) {
						bc = (BaseConnection) connection;
					}
				}
			}
			if (bc != null) {
				Statement stmt = bc.createStatement();
				stmt.execute("LISTEN events; LISTEN aggregate_roots; LISTEN migration; LISTEN revenj");
				retryCount = 0;
				Pooling pooling = new Pooling(bc, stmt);
				Thread thread = new Thread(pooling);
				thread.setDaemon(true);
				thread.start();
			} else cleanupConnection(connection);
		} catch (Exception ex) {
			try {
				systemState.notify(new SystemState.SystemEvent("notification", "issue: " + ex.getMessage()));
				Thread.sleep(1000 * retryCount);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Pooling implements Runnable {
		private final BaseConnection connection;
		private final Statement ping;

		Pooling(BaseConnection connection, Statement ping) {
			this.connection = connection;
			this.ping = ping;
		}

		@Override
		public void run() {
			PostgresReader reader = new PostgresReader();
			int timeout = maxTimeout;
			systemState.notify(new SystemState.SystemEvent("notification", "started"));
			while (!isClosed) {
				try {
					ping.execute("");
					PGNotification[] notifications = connection.getNotifications();
					if (notifications == null || notifications.length == 0) {
						try {
							Thread.sleep(timeout);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (timeout < maxTimeout) {
							timeout++;
						}
						continue;
					} else {
						timeout = 0;
					}
					for (PGNotification n : notifications) {
						processNotification(reader, n);
					}
				} catch (SQLException | IOException ex) {
					try {
						systemState.notify(new SystemState.SystemEvent("notification", "error: " + ex.getMessage()));
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					cleanupConnection(connection);
					if (!isClosed) {
						setupPooling();
					}
					return;
				}
			}
			cleanupConnection(connection);
		}
	}

	private void setupListening() {
		retryCount++;
		if (retryCount > 60) {
			retryCount = 30;
		}
		String jdbcUrl = properties.getProperty("revenj.jdbcUrl");
		if (jdbcUrl == null || jdbcUrl.isEmpty()) {
			throw new RuntimeException("Unable to read revenj.jdbcUrl from properties. Listening notification is not supported without it.\n"
			+"Either disable notifications (revenj.notifications.status=disabled), change it to pooling (revenj.notifications.type=pooling) or provide revenj.jdbcUrl to properties.");
		}
		if (!jdbcUrl.startsWith("jdbc:postgresql:") && jdbcUrl.contains("://")) jdbcUrl = "jdbc:postgresql" + jdbcUrl.substring(jdbcUrl.indexOf("://"));
		Properties parsed = org.postgresql.Driver.parseURL(jdbcUrl, properties);
		if (parsed == null) throw new RuntimeException("Unable to parse revenj.jdbcUrl");
		try {
			String user = properties.containsKey("revenj.user") ? properties.getProperty("revenj.user") : parsed.getProperty("user", "");
			String password = properties.containsKey("revenj.password") ? properties.getProperty("revenj.password") : parsed.getProperty("password", "");
			String db = parsed.getProperty("PGDBNAME");
			HostSpec host = new HostSpec(parsed.getProperty("PGHOST").split(",")[0], Integer.parseInt(parsed.getProperty("PGPORT").split(",")[0]));
			PGStream pgStream = ConnectionFactory.openConnection(host, user, password, db, properties);
			currentStream = pgStream;
			retryCount = 0;
			Listening listening = new Listening(pgStream);
			Thread thread = new Thread(listening);
			thread.setDaemon(true);
			thread.start();
		} catch (Exception ex) {
			try {
				systemState.notify(new SystemState.SystemEvent("notification", "issue: " + ex.getMessage()));
				Thread.sleep(1000 * retryCount);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class Listening implements Runnable {
		private final PGStream stream;

		Listening(PGStream stream) throws IOException {
			this.stream = stream;
			byte[] command = "LISTEN events; LISTEN aggregate_roots; LISTEN migration; LISTEN revenj".getBytes("UTF-8");
			stream.sendChar('Q');
			stream.sendInteger4(command.length + 5);
			stream.send(command);
			stream.sendChar(0);
			stream.flush();
			receiveCommand(stream);
			receiveCommand(stream);
			receiveCommand(stream);
			receiveCommand(stream);
			if (stream.receiveChar() != 'Z') throw new IOException("Unable to setup Postgres listener");
			int num = stream.receiveInteger4();
			if (num != 5) throw new IOException("unexpected length of ReadyForQuery packet");
			stream.receiveChar();
		}

		private void receiveCommand(PGStream pgStream) throws IOException {
			pgStream.receiveChar();
			int len = pgStream.receiveInteger4();
			pgStream.skip(len - 4);
		}

		@Override
		public void run() {
			PostgresReader reader = new PostgresReader();
			final PGStream pgStream = stream;
			systemState.notify(new SystemState.SystemEvent("notification", "started"));
			while (!isClosed) {
				try {
					switch (pgStream.receiveChar()) {
						case 'A':
							pgStream.receiveInteger4();
							int pidA = pgStream.receiveInteger4();
							String msgA = pgStream.receiveString();
							String paramA = pgStream.receiveString();
							processNotification(reader, new org.postgresql.core.Notification(msgA, pidA, paramA));
							break;
						case 'E':
							if (!isClosed) {
								int e_len = pgStream.receiveInteger4();
								String err = pgStream.receiveString(e_len - 4);
								throw new IOException(err);
							} else break;
						default:
							if (!isClosed) {
								throw new IOException("Unexpected packet type");
							} else break;
					}
				} catch (Exception ex) {
					try {
						currentStream = null;
						systemState.notify(new SystemState.SystemEvent("notification", "error: " + ex.getMessage()));
						pgStream.close();
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (!isClosed) {
						setupListening();
					}
					return;
				}
			}
			try {
				currentStream = null;
				pgStream.close();
			} catch (Exception ignore) {
			}
		}
	}

	private void processNotification(PostgresReader reader, PGNotification n) throws IOException {
		if ("events".equals(n.getName()) || "aggregate_roots".equals(n.getName())) {
			String param = n.getParameter();
			String ident = param.substring(0, param.indexOf(':'));
			String op = param.substring(ident.length() + 1, param.indexOf(':', ident.length() + 1));
			String values = param.substring(ident.length() + op.length() + 2);
			reader.process(values);
			List<String> ids = StringConverter.parseCollection(reader, 0, false);
			if (ids != null && ids.size() > 0) {
				String[] uris = ids.toArray(new String[ids.size()]);
				switch (op) {
					case "Update":
						subject.onNext(new NotifyInfo(ident, Operation.Update, Source.Database, uris));
						break;
					case "Change":
						subject.onNext(new NotifyInfo(ident, Operation.Change, Source.Database, uris));
						break;
					case "Delete":
						subject.onNext(new NotifyInfo(ident, Operation.Delete, Source.Database, uris));
						break;
					default:
						subject.onNext(new NotifyInfo(ident, Operation.Insert, Source.Database, uris));
						break;
				}
			}
		} else {
			systemState.notify(new SystemState.SystemEvent(n.getName(), n.getParameter()));
		}
	}

	private Repository getRepository(Class<?> manifest) {
		return repositories.computeIfAbsent(manifest, clazz -> {
			try {
				return (Repository) locator.resolve(Utils.makeGenericType(Repository.class, manifest));
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Repository is not registered for: " + manifest, ex);
			}
		});
	}

	@Override
	public void notify(NotifyInfo info) {
		subject.onNext(info);
	}

	@Override
	public Observable<NotifyInfo> getNotifications() {
		return notifications;
	}

	@Override
	public <T> Observable<TrackInfo<T>> track(final Class<T> manifest) {
		return notifications.filter(it -> {
			HashSet<Class<?>> set = targets.get(it.name);
			if (set == null) {
				set = new HashSet<>();
				Optional<Class<?>> domainType = domainModel.get().find(it.name);
				if (domainType.isPresent()) {
					set.add(domainType.get());
					Collections.addAll(set, domainType.get().getInterfaces());
				}
				targets.put(it.name, set);
			}
			return set.contains(manifest);
		}).map(it -> new TrackInfo<T>(it.uris, new LazyResult<T>(manifest, it.uris)));
	}

	private class LazyResult<T> implements Callable<List<T>> {

		private final Class<T> manifest;
		private final String[] uris;
		private List<T> result;

		LazyResult(Class<T> manifest, String[] uris) {
			this.manifest = manifest;
			this.uris = uris;
		}

		@Override
		public List<T> call() throws Exception {
			if (result == null) {
				Repository repository = getRepository(manifest);
				result = repository.find(uris);
			}
			return result;
		}
	}

	private synchronized void cleanupConnection(Connection connection) {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		isClosed = true;
		try {
			if (currentStream != null) {
				currentStream.close();
			}
		} catch (Exception ignore) {
		}
	}
}
