package org.revenj.processor.models;

import javax.inject.Inject;

public class NonPublicArgument {
	static class NonPublic {
	}
	@Inject
	public NonPublicArgument(NonPublic issue) {
	}
}