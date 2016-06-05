package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

import java.util.List;

public class WhereTransform extends RevenjOneLambdaQueryTransform {
	boolean withSource;

	public WhereTransform(RevenjQueryTransformConfiguration config, boolean withSource) {
		super(config);
		this.withSource = withSource;
	}

	@Override
	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis where, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		try {
			if (query.isSelectFromWhere()) {
				SelectFromWhere<U> sfw = (SelectFromWhere<U>) query;
				return apply(where, parentArgumentScope, sfw);
			} else if (query.isSelectFromWhereGroupHaving()) {
				GroupedSelectFromWhere<V, ?> sfw = (GroupedSelectFromWhere<V, ?>) query;
				Expression methodExpr = computeWhereReturnExpr(where, sfw, parentArgumentScope);

				// Create the new query, merging in the analysis of the method
				GroupedSelectFromWhere<U, ?> toReturn = (GroupedSelectFromWhere<U, ?>) sfw.shallowCopy();
				if (sfw.having == null) {
					toReturn.having = methodExpr;
				} else {
					toReturn.having = new BinaryExpression(sfw.having, "AND", methodExpr);
				}
				return toReturn;
			}
			throw new QueryTransformException("Existing query cannot be transformed further");
		} catch (TypedValueVisitorException e) {
			throw new QueryTransformException(e);
		}
	}

	public <T> SelectFromWhere<T> apply(
			LambdaAnalysis where,
			SymbExArgumentHandler parentArgumentScope,
			SelectFromWhere<T> sfw) throws TypedValueVisitorException, QueryTransformException {
		Expression methodExpr = computeWhereReturnExpr(where, sfw, parentArgumentScope);

		// Create the new query, merging in the analysis of the method
		SelectFromWhere<T> toReturn = sfw.shallowCopy();
		if (sfw.where == null) {
			toReturn.where = methodExpr;
		} else {
			toReturn.where = new BinaryExpression(sfw.where, "AND", methodExpr);
		}
		return toReturn;
	}

	private <V> Expression computeWhereReturnExpr(
			LambdaAnalysis where,
			SelectFromWhere<V> sfw,
			SymbExArgumentHandler parentArgumentScope) throws TypedValueVisitorException,
			QueryTransformException {
		SelectFromWhereLambdaArgumentHandler argHandler = SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, where, config.metamodel, parentArgumentScope, withSource);
		SymbExToColumns translator = config.newSymbExToColumns(argHandler, where.getLambdaIndex());
		Expression methodExpr = null;
		final List<PathAnalysis> paths = where.symbolicAnalysis.paths;
		for (int n = 0; n < paths.size(); n++) {
			PathAnalysis path = paths.get(n);

			TypedValue returnVal = PathAnalysisSimplifier
					.simplifyBoolean(path.getReturnValue(), config.getComparisonMethods(), config.getStaticComparisonMethods(), config.isAllEqualsSafe);
			SymbExPassDown returnPassdown = SymbExPassDown.with(null, true);
			ColumnExpressions<?> returnColumns = returnVal.visit(translator, returnPassdown);
			if (!returnColumns.isSingleColumn())
				throw new QueryTransformException("Expecting single column");
			Expression returnExpr = returnColumns.getOnlyColumn();

			if (returnVal instanceof ConstantValue.BooleanConstant) {
				if (((ConstantValue.BooleanConstant) returnVal).val) {
					// This path returns true, so it's redundant to actually
					// put true into the final code.
					returnExpr = null;
				} else if (paths.size() > 1) {
					continue;
				}
			}

			// Handle where path conditions
			Expression conditionExpr = pathConditionsToExpr(translator, path);

			// Merge path conditions and return value to create a value for the path
			Expression pathExpr = returnExpr;
			if (conditionExpr != null) {
				if (pathExpr == null) {
					pathExpr = conditionExpr;
				} else {
					pathExpr = new BinaryExpression(pathExpr, "AND", conditionExpr);
				}
			}

			// Merge into new expression summarizing the method
			if (methodExpr != null) {
				methodExpr = new BinaryExpression(methodExpr, "OR", pathExpr);
			} else {
				methodExpr = pathExpr;
			}
		}
		return methodExpr;
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return WhereTransform.class.getName();
	}
}
