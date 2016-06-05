package org.revenj.processor.models;

import javax.inject.Inject;

public class GenericNonPublicArgument {
	static class Arg {}
	public static class Gen<T>{}
	@Inject
	public GenericNonPublicArgument(Gen<Arg> gen) {
	}
}
