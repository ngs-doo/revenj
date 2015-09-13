package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.patterns.DataChangeNotification;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

import java.io.Closeable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

final class ChangeNotification<T> implements Closeable {

	private final Subscription subscription;

	private ChangeNotification(Class<T> manifest, DataChangeNotification notifications) {
		PublishSubject<DataChangeNotification.TrackInfo<T>> subject = PublishSubject.create();
		subscription = notifications.track(manifest).subscribe(subject::onNext);
		Observable<DataChangeNotification.TrackInfo<T>> source = subject.asObservable();

		bulkChanges = source.map(it -> it.result);
		lazyChanges =
				source.flatMapIterable(it -> {
					try {
						List<Callable<T>> callables = new ArrayList<>(it.uris.length);
						for (int i = 0; i < it.uris.length; i++) {
							int ind = i;
							callables.add(() -> it.result.call().get(ind));
						}
						return callables;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
		eagerChanges = source.flatMapIterable(it -> {
			try {
				return it.result.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private final Observable<T> eagerChanges;
	private final Observable<Callable<T>> lazyChanges;
	private final Observable<Callable<List<T>>> bulkChanges;

	public static void registerContainer(Container container, final DataChangeNotification notification) {
		container.registerGenerics(
				rx.Observable.class,
				(locator, arguments) ->
				{
					if (arguments.length == 1) {
						Type arg = arguments[0];
						if (arg instanceof Class<?>) {
							ChangeNotification<?> cn = new ChangeNotification((Class<?>) arg, notification);
							return cn.eagerChanges;
						} else if (arg instanceof ParameterizedType && ((ParameterizedType) arg).getRawType() == Callable.class) {
							Type[] genericArguments = ((ParameterizedType) arg).getActualTypeArguments();
							if (genericArguments[0] instanceof Class<?>) {
								Class<?> clazz = (Class<?>) genericArguments[0];
								if (genericArguments[0] instanceof ParameterizedType && Collection.class.isAssignableFrom(clazz)) {
									ChangeNotification<?> cn = new ChangeNotification(clazz.getComponentType(), notification);
									return cn.bulkChanges;
								} else {
									ChangeNotification<?> cn = new ChangeNotification(clazz, notification);
									return cn.lazyChanges;
								}
							}
						}
					}
					throw new RuntimeException("Invalid arguments for Observable<T>");
				}
		);

	}

	public void close() {
		subscription.unsubscribe();
	}
}
