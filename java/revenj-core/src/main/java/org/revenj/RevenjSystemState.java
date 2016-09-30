package org.revenj;

import org.revenj.extensibility.Container;
import org.revenj.extensibility.SystemState;
import rx.Observable;
import rx.subjects.PublishSubject;

class RevenjSystemState implements SystemState {

	private boolean systemBooting = true;
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
		return systemBooting;
	}

	@Override
	public boolean isReady() {
		return systemReady;
	}

	void started(Container container) {
		systemBooting = false;
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