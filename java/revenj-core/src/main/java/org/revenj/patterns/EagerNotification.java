package org.revenj.patterns;

public interface EagerNotification extends DataChangeNotification {
	void notify(NotifyInfo info);
}
