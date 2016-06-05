package org.revenj.database.postgres.jinq.jpqlquery;

public final class SimpleRowReader<T> implements RowReader<T> {

	public final static SimpleRowReader READER = new SimpleRowReader();

	@SuppressWarnings("unchecked")
	@Override
	public T readResult(Object result) {
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T readResult(Object[] results, int offset) {
		return (T) results[offset];
	}

	@Override
	public int getNumColumns() {
		return 1;
	}
}
