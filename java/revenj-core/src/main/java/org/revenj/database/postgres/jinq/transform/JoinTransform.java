package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class JoinTransform extends RevenjOneLambdaQueryTransform {
	boolean withSource;
	boolean joinAsPairs;
	boolean isExpectingStream;
	boolean isJoinFetch;

	public JoinTransform(RevenjQueryTransformConfiguration config, boolean withSource, boolean joinAsPairs, boolean isExpectingStream, boolean isJoinFetch) {
		super(config);
		this.withSource = withSource;
		this.isExpectingStream = isExpectingStream;
		// The old data should be merged with the new data using pairs
		// (otherwise, the new data simply replaces the old data)
		this.joinAsPairs = joinAsPairs;
		this.isJoinFetch = isJoinFetch;
	}

	public JoinTransform(RevenjQueryTransformConfiguration config) {
		this(config, false, true, true, false);
	}

	public JoinTransform setWithSource(boolean withSource) {
		this.withSource = withSource;
		return this;
	}

	public JoinTransform setIsExpectingStream(boolean isExpectingStream) {
		this.isExpectingStream = isExpectingStream;
		return this;
	}

	public JoinTransform setJoinAsPairs(boolean joinAsPairs) {
		this.joinAsPairs = joinAsPairs;
		return this;
	}

	public JoinTransform setIsJoinFetch(boolean isJoinFetch) {
		this.isJoinFetch = isJoinFetch;
		return this;
	}

	static boolean isSimpleFrom(JinqPostgresQuery<?> query) {
		if (!query.isSelectFromWhere()) return false;
		SelectFromWhere<?> sfw = (SelectFromWhere<?>) query;
		if (sfw.where != null) return false;
		if (sfw.froms.size() != 1) return false;
		if (!sfw.cols.isSingleColumn()) return false;
		Expression expr = sfw.cols.getOnlyColumn();
		if (!(expr instanceof FromAliasExpression)) return false;
		if (((FromAliasExpression) expr).from != sfw.froms.get(0)) return false;
		return true;
	}

	@Override
	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		try {
			if (query.isSelectFromWhere()) {
				SelectFromWhere<V> sfw = (SelectFromWhere<V>) query;
				SelectFromWhereLambdaArgumentHandler argHandler = SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, withSource);
				SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, isExpectingStream, lambda.getLambdaIndex());

				// TODO: Handle this case by translating things to use SELECT CASE
				if (lambda.symbolicAnalysis.paths.size() > 1)
					throw new QueryTransformException("Can only handle a single path in a JOIN at the moment");

				SymbExPassDown passdown = SymbExPassDown.with(null, false);
				JinqPostgresQuery<U> returnExpr = (JinqPostgresQuery<U>) PathAnalysisSimplifier
						.simplify(lambda.symbolicAnalysis.paths.get(0).getReturnValue(), config.getComparisonMethods(), config.getStaticComparisonMethods(), config.isAllEqualsSafe)
						.visit(translator, passdown);

				// Create the new query, merging in the analysis of the method

				// Check if the subquery is simply a stream of all of a certain entity
				if (isSimpleFrom(returnExpr)) {
					SelectFromWhere<?> toMerge = (SelectFromWhere<?>) returnExpr;
					SelectFromWhere<U> toReturn = (SelectFromWhere<U>) sfw.shallowCopy();
					if (isJoinFetch) {
						From from = toMerge.froms.get(0);
						if (!OuterJoinTransform.isLeftOuterJoinCompatible(toMerge))
							throw new QueryTransformException("Left outer join must be applied to a navigational link");
						From.FromNavigationalLinksGeneric joinFetchFrom = From.forNavigationalLinksJoinFetch((From.FromNavigationalLinks) from);
//                  if (!isJoinFetch && OuterJoinTransform.isChainedLink(outerJoinFrom.links))
//                  {
//                     // The left outer join only applies to the end part of
//                     // links. So we'll pull off the earlier part of the chain
//                     // and make them a separate alias.
//                     ReadFieldExpression outerLinks = (ReadFieldExpression)outerJoinFrom.links;
//                     From baseFrom = From.forNavigationalLinks(outerLinks.base);
//                     toReturn.froms.add(baseFrom);
//                     outerJoinFrom.links = new ReadFieldExpression(new FromAliasExpression(baseFrom), outerLinks.field);
//                  }
						toReturn.froms.add(joinFetchFrom);
						OuterJoinTransform.rewriteFromAliases(toMerge, from, joinFetchFrom);

					} else {
						toReturn.froms.add(toMerge.froms.get(0));
					}
					if (joinAsPairs) {
						toReturn.cols = new ColumnExpressions<>(createPairReader(sfw.cols.reader, toMerge.cols.reader));
						toReturn.cols.columns.addAll(sfw.cols.columns);
						toReturn.cols.columns.addAll(toMerge.cols.columns);
					} else {
						toReturn.cols = (ColumnExpressions<U>) toMerge.cols;
					}
					return toReturn;
				}

				// Handle other types of subqueries
			}
			throw new QueryTransformException("Existing query cannot be transformed further");
		} catch (TypedValueVisitorException e) {
			throw new QueryTransformException(e);
		}
	}

	protected <U> RowReader<U> createPairReader(RowReader<?> a, RowReader<?> b) {
		return TupleRowReader.createReaderForTuple(TupleRowReader.PAIR_CLASS, a, b);
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return JoinTransform.class.getName();
	}
}
