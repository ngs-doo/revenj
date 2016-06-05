package org.revenj.database.postgres.jinq.transform;

import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.database.postgres.jinq.jpqlquery.SelectOnly;

public class DistinctTransform extends RevenjNoLambdaQueryTransform {
	public DistinctTransform(RevenjQueryTransformConfiguration config) {
		super(config);
	}

	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		if (query.canDistinct()) {
			SelectOnly<V> select = (SelectOnly<V>) query;

			// Create the new query, merging in the analysis of the method
			SelectOnly<U> toReturn = (SelectOnly<U>) select.shallowCopy();
			toReturn.isDistinct = true;

			return toReturn;
		}
		throw new QueryTransformException("Existing query cannot be transformed further");
	}

	@Override
	public String getTransformationTypeCachingTag() {
		return DistinctTransform.class.getName();
	}

}
