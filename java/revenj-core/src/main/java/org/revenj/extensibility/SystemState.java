package org.revenj.extensibility;

public interface SystemState {
	boolean isBooting();

	boolean isReady();

	rx.Observable<Container> ready();

	rx.Observable<SystemEvent> change();

	void notify(SystemEvent value);

	class SystemEvent {

		public final String id;
		public final String detail;

		public SystemEvent(String id, String detail) {
			this.id = id;
			this.detail = detail;
		}
	}
}
