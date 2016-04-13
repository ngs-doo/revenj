package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.extensibility.SystemState;
import rx.Observable;
import rx.subjects.PublishSubject;

class RevenjSystemState implements SystemState {

	private boolean systemBoooting = true;
	private boolean systemReady = false;
	private final PublishSubject<SystemEvent> changeSubject = PublishSubject.create();
	private final PublishSubject<Container> startupSubject = PublishSubject.create();
	private final Observable<SystemEvent> changeEvents;
	private final Observable<Container> startupEvents;

	public RevenjSystemState() {
		changeEvents = changeSubject.asObservable();
		startupEvents = startupSubject.asObservable();
	}

	@Override
	public boolean isBooting() {
		return systemBoooting;
	}

	@Override
	public boolean isReady() {
		return systemReady;
	}

	void started(Container container) {
		systemBoooting = false;
		systemReady = true;
		startupSubject.onNext(container);
	}

	@Override
	public Observable<Container> ready() {
		return startupEvents;
	}

	@Override
	public Observable<SystemEvent> change() {
		return changeEvents;
	}

	@Override
	public void notify(SystemEvent value) {
		changeSubject.onNext(value);
	}
}