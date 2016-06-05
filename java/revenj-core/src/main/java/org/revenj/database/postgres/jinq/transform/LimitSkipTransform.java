package org.revenj.database.postgres.jinq.transform;

import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.database.postgres.jinq.jpqlquery.SelectFromWhere;

public class LimitSkipTransform extends RevenjNoLambdaQueryTransform {
	public LimitSkipTransform(RevenjQueryTransformConfiguration config, boolean isLimit, long n) {
		super(config);
		constraint = n;
		this.isLimit = isLimit;
	}

	boolean isLimit;
	long constraint;

	@Override
	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		if (query instanceof SelectFromWhere) {
			SelectFromWhere<V> sfw = (SelectFromWhere<V>) query;

			if (isLimit && sfw.limit >= 0) throw new IllegalArgumentException("Cannot limit a query more than once");
			if (!isLimit && sfw.skip >= 0) throw new IllegalArgumentException("Cannot skip in a query more than once");

			// Create the new query, merging in the analysis of the method
			SelectFromWhere<U> toReturn = (SelectFromWhere<U>) sfw.shallowCopy();

			if (isLimit) {
				toReturn.limit = constraint;
			} else {
				if (toReturn.limit >= 0) toReturn.limit -= constraint;
				toReturn.skip = constraint;
			}

			return toReturn;
		}
		throw new QueryTransformException("Existing query cannot be transformed further");
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return LimitSkipTransform.class.getName() + ":" + isLimit + ":" + constraint;
	}
}
