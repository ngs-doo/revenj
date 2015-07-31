package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JPQLQuery;

public abstract class JPQLGroupingMultiLambdaQueryTransform extends JPQLQueryTransform {

    JPQLGroupingMultiLambdaQueryTransform(JPQLQueryTransformConfiguration config) {
        super(config);
    }

    public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, LambdaInfo lambda, LambdaInfo[] groupingLambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        return null;
    }
}
