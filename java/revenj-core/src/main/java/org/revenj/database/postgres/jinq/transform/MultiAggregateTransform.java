package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class MultiAggregateTransform extends RevenjMultiLambdaQueryTransform {
    public MultiAggregateTransform(RevenjQueryTransformConfiguration config) {
        super(config);
    }

    @Override
    public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        try {
            if (query.isSelectFromWhere()) {
                SelectFromWhere<V> sfw = (SelectFromWhere<V>) query;

                SelectOnly<V> streamTee = new SelectOnly<>();
                streamTee.cols = sfw.cols;
                ColumnExpressions<?>[] aggregatedQueryEntries = new ColumnExpressions<?>[lambdas.length];

                for (int n = 0; n < lambdas.length; n++) {
                    LambdaAnalysis lambda = lambdas[n];

                    AggregateStreamLambdaArgumentHandler argHandler = new AggregateStreamLambdaArgumentHandler(streamTee, lambda, config.metamodel, parentArgumentScope, false);
                    SymbExToColumns translator = config.newSymbExToColumns(argHandler, n);

                    ColumnExpressions<U> returnQuery = makeSelectExpression(translator, lambda);

                    // TODO: Confirm that the result actually contains an aggregate
                    aggregatedQueryEntries[n] = returnQuery;
                }

                // Create the new query, merging in the analysis of the method
                SelectFromWhere<U> toReturn = (SelectFromWhere<U>) sfw.shallowCopy();
                toReturn.isAggregated = true;
                RowReader<?>[] readers = new RowReader<?>[aggregatedQueryEntries.length];
                for (int n = 0; n < readers.length; n++)
                    readers[n] = aggregatedQueryEntries[n].reader;
                ColumnExpressions<U> cols = new ColumnExpressions<>(createTupleReader(readers));
                for (int n = 0; n < readers.length; n++)
                    cols.columns.addAll(aggregatedQueryEntries[n].columns);
                toReturn.cols = cols;
                return toReturn;
            }
            throw new QueryTransformException("Existing query cannot be transformed further");
        } catch (TypedValueVisitorException e) {
            throw new QueryTransformException(e);
        }
    }

    protected <U> RowReader<U> createTupleReader(RowReader<?>[] readers) {
        return TupleRowReader.createReaderForTuple(readers);
    }

    @Override
    public String getTransformationTypeCachingTag() {
        return MultiAggregateTransform.class.getName();
    }
}
