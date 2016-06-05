package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class JoinFetchTransform extends RevenjOneLambdaQueryTransform {
    boolean isExpectingStream;
    boolean isOuterJoinFetch;

    public JoinFetchTransform(RevenjQueryTransformConfiguration config, boolean isExpectingStream, boolean isOuterJoinFetch) {
        super(config);
        this.isExpectingStream = isExpectingStream;
        this.isOuterJoinFetch = isOuterJoinFetch;
    }

    public JoinFetchTransform(RevenjQueryTransformConfiguration config) {
        this(config, false, false);
    }

    public JoinFetchTransform setIsExpectingStream(boolean isExpectingStream) {
        this.isExpectingStream = isExpectingStream;
        return this;
    }

    public JoinFetchTransform setIsOuterJoinFetch(boolean isOuterJoinFetch) {
        this.isOuterJoinFetch = isOuterJoinFetch;
        return this;
    }

    private static boolean isJoinFetchCompatible(SelectFromWhere<?> toMerge) {
        // Hibernate only allows (LEFT OUTER) JOIN FETCH for one level of associations
        // and the left part of the association must be returned in the query.
        // We don't strictly enforce the fact that the query must return the left
        // part of the association, but we'll loosely enforce it here.
        From from = toMerge.froms.get(0);
        if (!(from instanceof From.FromNavigationalLinks))
            return false;
        Expression navLink = ((From.FromNavigationalLinks) from).links;
        if (!(navLink instanceof ReadFieldExpression))
            return false;
        return true;
    }

    @Override
    public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaAnalysis lambda, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
        try {
            if (query.isSelectFromWhere()) {
                SelectFromWhere<V> sfw = (SelectFromWhere<V>) query;
                SelectFromWhereLambdaArgumentHandler argHandler = SelectFromWhereLambdaArgumentHandler.fromSelectFromWhere(sfw, lambda, config.metamodel, parentArgumentScope, false);
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
                if (JoinTransform.isSimpleFrom(returnExpr)) {
                    SelectFromWhere<?> toMerge = (SelectFromWhere<?>) returnExpr;
                    SelectFromWhere<U> toReturn = (SelectFromWhere<U>) sfw.shallowCopy();
                    From from = toMerge.froms.get(0);

                    if (!isJoinFetchCompatible(toMerge))
                        throw new QueryTransformException("Join fetch must be a single navigational link");
                    From.FromNavigationalLinksGeneric joinFetchFrom;
                    if (isOuterJoinFetch)
                        joinFetchFrom = From.forNavigationalLinksLeftOuterJoinFetch((From.FromNavigationalLinks) from);
                    else
                        joinFetchFrom = From.forNavigationalLinksJoinFetch((From.FromNavigationalLinks) from);

                    toReturn.froms.add(joinFetchFrom);
                    OuterJoinTransform.rewriteFromAliases(toMerge, from, joinFetchFrom);
                    return toReturn;
                }

                // Handle other types of subqueries
            }
            throw new QueryTransformException("Existing query cannot be transformed further");
        } catch (TypedValueVisitorException e) {
            throw new QueryTransformException(e);
        }
    }

    @Override
    public String getTransformationTypeCachingTag() {
        return JoinFetchTransform.class.getName();
    }
}
