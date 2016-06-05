package org.revenj.database.postgres.jinq.transform;

import java.util.Arrays;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class GroupingTransform extends RevenjMultiLambdaQueryTransform {
	public GroupingTransform(RevenjQueryTransformConfiguration config) {
		super(config);
	}

	private <U, V, W> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis groupingLambda, LambdaAnalysis[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		try {
			if (query.isSelectFromWhere()) {
				SelectFromWhere<V> sfw = (SelectFromWhere<V>) query;

				// Figure out the columns needed for the key value
				SelectTransform keyTransform = new SelectTransform(config, false);
				JinqPostgresQuery<W> keyQuery = keyTransform.apply(query, groupingLambda, parentArgumentScope);
				if (!keyQuery.isSelectFromWhere())
					throw new QueryTransformException("Expecting the result of the key calculation to be a SelectFromWhere query");
				SelectOnly<W> keySelect = new SelectOnly<>();
				keySelect.cols = ((SelectFromWhere<W>) keyQuery).cols;

				// Handle the aggregates part of the group
				SelectOnly<V> streamTee = new SelectOnly<>();
				streamTee.cols = sfw.cols;
				ColumnExpressions<?>[] aggregatedQueryEntries = new ColumnExpressions<?>[lambdas.length];

				for (int n = 0; n < lambdas.length; n++) {
					LambdaAnalysis lambda = lambdas[n];

					GroupingLambdasArgumentHandler argHandler = new GroupingLambdasArgumentHandler(keySelect, streamTee, lambda, config.metamodel, parentArgumentScope, false);
					SymbExToColumns translator = config.newSymbExToColumns(argHandler, n);

					ColumnExpressions<U> returnQuery = makeSelectExpression(translator, lambda);

					// TODO: Confirm that the result actually contains an aggregate
					aggregatedQueryEntries[n] = returnQuery;
				}

				// Create the new query, merging in the analysis of the method
				GroupedSelectFromWhere<U, W> toReturn = (GroupedSelectFromWhere<U, W>) sfw.shallowCopyWithGrouping();
				toReturn.isAggregated = true;
				RowReader<?>[] readers = new RowReader<?>[aggregatedQueryEntries.length + 1];
				for (int n = 0; n < aggregatedQueryEntries.length; n++)
					readers[n + 1] = aggregatedQueryEntries[n].reader;
				readers[0] = keySelect.getRowReader();
				ColumnExpressions<U> cols = new ColumnExpressions<>(createTupleReader(readers));
				cols.columns.addAll(keySelect.cols.columns);
				for (int n = 0; n < aggregatedQueryEntries.length; n++)
					cols.columns.addAll(aggregatedQueryEntries[n].columns);
				toReturn.groupingCols = keySelect.cols;
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
	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis[] lambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		return apply(query, lambdas[0], Arrays.copyOfRange(lambdas, 1, lambdas.length), parentArgumentScope);
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return GroupingTransform.class.getName();
	}
}
