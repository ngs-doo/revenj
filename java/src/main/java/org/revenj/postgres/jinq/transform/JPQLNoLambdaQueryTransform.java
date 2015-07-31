package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JPQLQuery;

public abstract class JPQLNoLambdaQueryTransform extends JPQLQueryTransform {

    JPQLNoLambdaQueryTransform(JPQLQueryTransformConfiguration config) {
        super(config);
    }

    public <U, V> JPQLQuery<U> apply(JPQLQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        return null;
    }
}
