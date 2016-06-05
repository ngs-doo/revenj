package org.revenj.processor.models;

import javax.inject.Inject;

public class GenericArgument {
	public static class Gen<T>{}
	@Inject
	public GenericArgument(Gen<String> gen) {
	}
}
