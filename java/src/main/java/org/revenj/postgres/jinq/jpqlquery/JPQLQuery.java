package org.revenj.postgres.jinq.jpqlquery;

import java.util.List;

/**
 * Data structure used to represent JPQL queries and the conversions
 * needed to parse results into a form usable by Jinq.
 */
public abstract class JPQLQuery<T> implements JPQLFragment {
    public static <U> JPQLQuery<U> findAll(String dataSource) {
        SelectFromWhere<U> query = new SelectFromWhere<>();
        From from = From.forDataSource(dataSource);
        query.cols = ColumnExpressions.singleColumn(new SimpleRowReader<>(), new FromAliasExpression(from));
        query.froms.add(from);
        return query;
    }

    public JPQLQuery() {
    }

    /**
     * @return true iff the query is a simple select...from...where style query
     */
    public abstract boolean isSelectFromWhere();

    public abstract boolean isSelectOnly();

    public abstract boolean isSelectFromWhereGroupHaving();

    public abstract boolean canSort();

    public abstract boolean canDistinct();

    public abstract boolean isValidSubquery();

    public abstract String getQueryString();

    public abstract List<GeneratedQueryParameter> getQueryParameters();

    public abstract JPQLQuery<T> shallowCopy();

    public abstract RowReader<T> getRowReader();
}
