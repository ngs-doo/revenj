package org.revenj.database.postgres.jinq.transform;

import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;

public abstract class RevenjOneLambdaQueryTransform extends RevenjQueryTransform {

	RevenjOneLambdaQueryTransform(RevenjQueryTransformConfiguration config) {
		super(config);
	}

	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		return null;
	}
}
