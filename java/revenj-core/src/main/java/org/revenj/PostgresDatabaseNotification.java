package org.revenj;

import org.postgresql.PGNotification;
import org.postgresql.core.BaseConnection;
import org.revenj.extensibility.SystemState;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.converters.StringConverter;
import org.revenj.patterns.DomainModel;
import org.revenj.patterns.EagerNotification;
import org.revenj.patterns.Repository;
import org.revenj.patterns.ServiceLocator;
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
		} else {
			setupPooling();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> isClosed = true));
		}
	}

	private void setupPooling() {
		retryCount++;
		if (retryCount > 60) {
			retryCount = 30;
		}
		try {
			Connection connection = dataSource != null ? dataSource.getConnection() : null;
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
				stmt.execute("LISTEN events; LISTEN aggregate_roots; LISTEN migration;");
				retryCount = 0;
				Pooling pooling = new Pooling(bc, stmt);
				Thread thread = new Thread(pooling);
				thread.setDaemon(true);
				thread.start();
			} else cleanupConnection(connection);
		} catch (Exception ex) {
			try {
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
						if (!"events".equals(n.getName()) && !"aggregate_roots".equals(n.getName())) {
							if ("migration".equals(n.getName())) {
								systemState.notify(new SystemState.SystemEvent("migration", n.getParameter()));
							}
							continue;
						}
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
									subject.onNext(new NotifyInfo(ident, Operation.Update, uris));
									break;
								case "Change":
									subject.onNext(new NotifyInfo(ident, Operation.Change, uris));
									break;
								case "Delete":
									subject.onNext(new NotifyInfo(ident, Operation.Delete, uris));
									break;
								default:
									subject.onNext(new NotifyInfo(ident, Operation.Insert, uris));
									break;
							}
						}
					}
				} catch (SQLException | IOException ex) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					cleanupConnection(connection);
					setupPooling();
					return;
				}
			}
			cleanupConnection(connection);
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

	class LazyResult<T> implements Callable<List<T>> {

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
	}
}
