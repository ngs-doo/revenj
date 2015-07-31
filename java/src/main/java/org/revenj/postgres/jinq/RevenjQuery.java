package org.revenj.postgres.jinq;

import org.revenj.patterns.DataSource;
import org.revenj.patterns.Query;
import org.revenj.patterns.Specification;

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
    public long count() throws SQLException {
        return queryComposer.runCount();
    }

    @Override
    public boolean anyMatch(Specification<? super T> predicate) throws SQLException {
        return this.queryComposer.where(predicate).any();
    }

    @Override
    public boolean allMatch(Specification<? super T> predicate) throws SQLException {
        return queryComposer.all(predicate);
    }

    @Override
    public boolean noneMatch(Specification<? super T> predicate) throws SQLException {
        return this.queryComposer.where(predicate).none();
    }

    @Override
    public Optional<T> findFirst() throws SQLException {
        return queryComposer.first();
    }

    @Override
    public Optional<T> findAny() throws SQLException {
        return queryComposer.first();
    }

    @Override
    public List<T> list() throws SQLException {
        return queryComposer.toList();
    }
}
