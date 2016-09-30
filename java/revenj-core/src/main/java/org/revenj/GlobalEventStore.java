package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.patterns.DomainEvent;
import org.revenj.patterns.DomainEventStore;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

class GlobalEventStore implements Closeable {

	private final Container container;
	private final DataSource dataSource;
	private Connection connection;
	private final ConcurrentMap<Class<?>, DomainEventStore> eventStores = new ConcurrentHashMap<>();
	private final BlockingQueue<DomainEvent> eventQueue = new LinkedBlockingDeque<>();
	private final Thread loop;
	private boolean isClosed;

	public GlobalEventStore(Container container, DataSource dataSource) {
		this.container = container;
		this.dataSource = dataSource;
		setupConnection();
		loop = new Thread(new WaitForEvents());
		loop.setDaemon(true);
		loop.start();
	}

	<TEvent extends DomainEvent> void queue(TEvent domainEvent) {
		eventQueue.add(domainEvent);
	}

	@SuppressWarnings("unchecked")
	private class WaitForEvents implements Runnable {
		@Override
		public void run() {
			List<DomainEvent> bulk = new ArrayList<>(1000);
			Class<?> lastType;
			DomainEvent info = null;
			while (!isClosed) {
				try {
					if (bulk.size() == 0) {
						info = eventQueue.take();
						bulk.add(info);
					}
					if (info == null) break;
					lastType = info.getClass();
					int i = 0;
					while (i++ < 1000 && !eventQueue.isEmpty()) {
						info = eventQueue.take();
						if (info.getClass() != lastType) break;
						bulk.add(info);
					}
					final Class<?> currentType = lastType;
					DomainEventStore store = eventStores.computeIfAbsent(lastType, t -> {
						try {
							return container.resolve(DomainEventStore.class, currentType);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					});
					try {
						store.submit(bulk);
					} finally {
						bulk.clear();
					}
					if (info.getClass() != currentType) {
						bulk.add(info);
					}
				} catch (Exception e) {
					try {
						connection.close();
					} catch (Exception ignore) {
					}
					setupConnection();
				}
			}
		}
	}

	private void setupConnection() {
		try {
			eventStores.clear();
			connection = dataSource.getConnection();
			connection.setAutoCommit(true);
			container.registerInstance(connection);
		} catch (Exception ignore) {
		}
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
		try {
			container.close();
		} catch (Exception ignore) {
		}
	}
}
