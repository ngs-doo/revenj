package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;

public abstract class RevenjNoLambdaQueryTransform extends RevenjQueryTransform {

	RevenjNoLambdaQueryTransform(RevenjQueryTransformConfiguration config) {
		super(config);
	}

	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		return null;
	}
}
