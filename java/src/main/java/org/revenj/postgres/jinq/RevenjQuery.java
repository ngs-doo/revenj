package org.revenj.postgres.jinq;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class RevenjQuery<T extends DataSource> implements Query<T> {
	final RevenjQueryComposer<T> queryComposer;

	public RevenjQuery(RevenjQueryComposer<T> query) {
		queryComposer = query;
	}

	static <U extends DataSource> RevenjQuery<U> makeQueryStream(RevenjQueryComposer<U> query) {
		return new RevenjQuery<>(query);
	}

	@Override
	public Query<T> filter(Specification<T> predicate) {
		RevenjQueryComposer newComposer = this.queryComposer.where(predicate);
		return makeQueryStream(newComposer);
	}

	@Override
	public Query<T> skip(long n) {
		RevenjQueryComposer newComposer = this.queryComposer.skip(n);
		return makeQueryStream(newComposer);
	}

	@Override
	public Query<T> limit(long n) {
		RevenjQueryComposer newComposer = this.queryComposer.limit(n);
		return makeQueryStream(newComposer);
	}

	@Override
	public <V extends Comparable<V>> Query<T> sortedBy(Compare<T, V> order) {
		RevenjQueryComposer newComposer = this.queryComposer.sortedBy(order, true);
		return makeQueryStream(newComposer);
	}

	@Override
	public <V extends Comparable<V>> Query<T> sortedDescendingBy(Compare<T, V> order) {
		RevenjQueryComposer newComposer = this.queryComposer.sortedBy(order, false);
		return makeQueryStream(newComposer);
	}

	@Override
	public long count() throws IOException {
		try {
			return queryComposer.count();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean anyMatch(Specification<? super T> predicate) throws IOException {
		try {
			return this.queryComposer.where(predicate).any();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean allMatch(Specification<? super T> predicate) throws IOException {
		try {
			return queryComposer.all(predicate);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean noneMatch(Specification<? super T> predicate) throws IOException {
		try {
			return this.queryComposer.where(predicate).none();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Optional<T> findFirst() throws IOException {
		try {
			return queryComposer.first();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Optional<T> findAny() throws IOException {
		try {
			return queryComposer.first();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public List<T> list() throws IOException {
		try {
			return queryComposer.toList();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
}
