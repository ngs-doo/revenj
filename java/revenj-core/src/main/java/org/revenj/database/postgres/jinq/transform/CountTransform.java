package org.revenj.database.postgres.jinq.transform;

import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;

public class CountTransform extends RevenjNoLambdaQueryTransform {
	AggregateTransform transform;

	public CountTransform(RevenjQueryTransformConfiguration config) {
		super(config);
		transform = new AggregateTransform(config, AggregateTransform.AggregateType.COUNT);
	}

	@Override
	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		return transform.apply(query, null, parentArgumentScope);
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return CountTransform.class.getName();
	}
}
