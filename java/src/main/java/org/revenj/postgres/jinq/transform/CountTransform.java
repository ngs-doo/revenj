package org.revenj.postgres.jinq.transform;

import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;

public class CountTransform extends JPQLNoLambdaQueryTransform {
    AggregateTransform transform;

    public CountTransform(JPQLQueryTransformConfiguration config) {
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
