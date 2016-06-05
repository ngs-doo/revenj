package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class AggregateTransform extends RevenjOneLambdaQueryTransform {
	public enum AggregateType {
		SUM, AVG, MAX, MIN,
		COUNT, // COUNT is only usable for multiaggregate and grouping subqueries
	}

	public AggregateTransform(RevenjQueryTransformConfiguration config, AggregateType type) {
		super(config);
		this.type = type;
	}

	private AggregateType type;

	@Override
	public <U, V> JinqPostgresQuery<U> apply(
			JinqPostgresQuery<V> query,
			LambdaAnalysis lambda,
			SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		try {
			if (query.isSelectFromWhere() || query instanceof SelectOnly) {
				SelectOnly<V> select = (SelectOnly<V>) query;
				Expression aggregatedExpr = null;
				SymbExArgumentHandler argumentHandler;
				if (type != AggregateType.COUNT) {
					if (select.isDistinct) {
						// Can only perform an aggregation like SUM() or AVG() on distinct streams if we don't
						// further modify those streams (i.e. we just pass the data through directly).
						argumentHandler = SelectFromWhereLambdaArgumentHandler.forPassthroughTest(lambda, config.metamodel, parentArgumentScope, false);
						SymbExToColumns translator = config.newSymbExToColumns(argumentHandler, lambda.getLambdaIndex());
						aggregatedExpr = makeSelectExpression(translator, lambda).getOnlyColumn();
						if (aggregatedExpr != SelectFromWhereLambdaArgumentHandler.passthroughColsForTesting.getOnlyColumn())
							throw new TypedValueVisitorException("Applying an aggregation to a distinct stream, but modifying the stream after the distinct but before the aggregation");
					}
					if (select.isSelectFromWhere()) {
						SelectFromWhere<V> sfw = (SelectFromWhere<V>) select;
						argumentHandler = SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, false);
					} else // if (query instanceof SelectOnly)
					{
						argumentHandler = SelectFromWhereLambdaArgumentHandler.fromSelectOnly(select, lambda, config.metamodel, parentArgumentScope, false);
					}
					SymbExToColumns translator = config.newSymbExToColumns(argumentHandler, lambda.getLambdaIndex());
					aggregatedExpr = makeSelectExpression(translator, lambda).getOnlyColumn();
				} else {
					if (select.cols.isSingleColumn())
						aggregatedExpr = select.cols.getOnlyColumn();
					else
						aggregatedExpr = new ConstantExpression("1");
				}
				// Create the new query, merging in the analysis of the method
				SelectOnly<U> toReturn = (SelectOnly<U>) select.shallowCopy();
				toReturn.isAggregated = true;
				toReturn.cols = ColumnExpressions.singleColumn(
						SimpleRowReader.READER,
						new AggregateFunctionExpression(aggregatedExpr, type.name(), select.isDistinct));
				return toReturn;
			}
			throw new QueryTransformException("Existing query cannot be transformed further");
		} catch (TypedValueVisitorException e) {
			throw new QueryTransformException(e);
		}
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return AggregateTransform.class.getName() + ":" + type.name();
	}
}
