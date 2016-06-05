package org.revenj.database.postgres.jinq.jpqlquery;

import java.util.List;

/**
 * Data structure used to represent Postgres queries and the conversions
 * needed to parse results into a form usable by Jinq.
 */
public abstract class JinqPostgresQuery<T> implements JinqPostgresFragment {
	@SuppressWarnings("unchecked")
	public static <U> JinqPostgresQuery<U> findAll(String dataSource) {
		SelectFromWhere<U> query = new SelectFromWhere<>();
		From from = From.forDataSource(dataSource);
		query.cols = ColumnExpressions.singleColumn(SimpleRowReader.READER, new FromAliasExpression(from));
		query.froms.add(from);
		return query;
	}

	public JinqPostgresQuery() {
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

	public abstract JinqPostgresQuery<T> shallowCopy();

	public abstract RowReader<T> getRowReader();
}
