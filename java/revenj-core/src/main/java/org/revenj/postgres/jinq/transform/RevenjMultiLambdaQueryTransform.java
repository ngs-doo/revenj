package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;

public abstract class RevenjMultiLambdaQueryTransform extends RevenjQueryTransform {

    RevenjMultiLambdaQueryTransform(RevenjQueryTransformConfiguration config) {
        super(config);
    }

    public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        return null;
    }
}
