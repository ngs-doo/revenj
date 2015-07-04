package org.revenj.patterns;

public interface Func<TArg, TRes> {
	TRes create(TArg arg);
}
