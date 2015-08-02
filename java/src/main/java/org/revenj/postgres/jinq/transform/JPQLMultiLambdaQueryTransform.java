package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;

public abstract class JPQLMultiLambdaQueryTransform extends JPQLQueryTransform {

    JPQLMultiLambdaQueryTransform(JPQLQueryTransformConfiguration config) {
        super(config);
    }

    public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        return null;
    }
}
