package org.revenj.handlers;

import gen.model.test.Clicked;
import org.revenj.patterns.DomainEventHandler;
import org.revenj.patterns.EventHandler;

@EventHandler
public class ClickedCollectionEventHandler implements DomainEventHandler<Clicked[]> {
	public static int COUNT;

	public void handle(Clicked[] events) {
		COUNT++;
	}
}