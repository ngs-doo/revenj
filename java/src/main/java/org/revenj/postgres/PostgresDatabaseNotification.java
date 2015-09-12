package org.revenj.postgres;

import org.postgresql.PGNotification;
import org.postgresql.core.BaseConnection;
import org.revenj.Utils;
import org.revenj.patterns.*;
import org.revenj.postgres.converters.StringConverter;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class PostgresDatabaseNotification implements EagerNotification, Closeable {

	private final Function<ServiceLocator, Connection> connectionFactory;
	private final Optional<DomainModel> domainModel;
	private final ServiceLocator locator;

	private final PublishSubject<NotifyInfo> subject = PublishSubject.create();
	private final Observable<NotifyInfo> notifications;

	private final ConcurrentMap<Class<?>, Repository> repositories = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, HashSet<Class<?>>> targets = new ConcurrentHashMap<>();

	private int retryCount;

	private Connection connection;
	private boolean isClosed;

	public PostgresDatabaseNotification(Function<ServiceLocator, Connection> connectionFactory, Optional<DomainModel> domainModel, ServiceLocator locator) {
		this.connectionFactory = connectionFactory;
		this.domainModel = domainModel;
		this.locator = locator;
		notifications = subject.asObservable();
		setupConnection();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> isClosed = true));
	}

	private void setupConnection() {
		retryCount++;
		if (retryCount > 60) {
			retryCount = 30;
		}
		try {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			connection = connectionFactory.apply(locator);
			if (connection instanceof BaseConnection == false) return;
			BaseConnection bc = (BaseConnection) connection;
			Statement stmt = bc.createStatement();
			stmt.execute("LISTEN events; LISTEN aggregate_roots");
			retryCount = 0;
			Pooling pooling = new Pooling(bc, stmt);
			new Thread(pooling).start();
		} catch (Exception ex) {
			try {
				Thread.sleep(1000 * retryCount);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class Pooling implements Runnable {
		private final BaseConnection connection;
		private final Statement ping;

		public Pooling(BaseConnection connection, Statement ping) {
			this.connection = connection;
			this.ping = ping;
		}

		@Override
		public void run() {
			PostgresReader reader = new PostgresReader();
			while (!isClosed) {
				try {
					ping.execute(";");
					PGNotification[] notifications = connection.getNotifications();
					if (notifications == null || notifications.length == 0) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}
					for (PGNotification n : notifications) {
						if (!"events".equals(n.getName()) && !"aggregate_roots".equals(n.getName())) continue;
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
					setupConnection();
					return;
				}
			}
		}
	}

	private Repository getRepository(Class<?> manifest) {
		return repositories.computeIfAbsent(manifest, clazz ->
		{
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
		}).map(it -> new TrackInfo<T>(it.uris, () -> getRepository(manifest).find(it.uris)));
	}

	public void close() {
		if (isClosed) {
			return;
		}
		isClosed = true;
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		connection = null;
	}
}
