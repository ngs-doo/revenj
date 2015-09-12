package org.revenj.patterns;

import rx.Observable;

import java.util.List;
import java.util.concurrent.Callable;

public interface DataChangeNotification {

	final class NotifyInfo {
		public final String name;
		public final Operation operation;
		public final String[] uris;

		public NotifyInfo(String name, Operation operation, String[] uris) {
			this.name = name;
			this.operation = operation;
			this.uris = uris;
		}
	}

	enum Operation {Insert, Update, Change, Delete}

	Observable<NotifyInfo> getNotifications();

	final class TrackInfo<T> {
		public final String[] uris;
		public final Callable<List<T>> result;

		public TrackInfo(String[] uris, Callable<List<T>> result) {
			this.uris = uris;
			this.result = result;
		}
	}

	<T> Observable<TrackInfo<T>> track(Class<T> manifest);
}
