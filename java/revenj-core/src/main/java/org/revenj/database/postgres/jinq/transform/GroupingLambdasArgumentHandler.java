package org.revenj.database.postgres.jinq.transform;

import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.database.postgres.jinq.jpqlquery.SelectOnly;

/**
 * Handles the lookup of parameters passed to a lambda. Parameters can
 * be used to represent query parameters or references to the data stream.
 * This class handles the lookup of the grouping key and the grouped data
 * stream of the result of a grouping query.
 */
public class GroupingLambdasArgumentHandler extends LambdaParameterArgumentHandler {
	SelectOnly<?> groupKey;
	SelectOnly<?> stream;

	public GroupingLambdasArgumentHandler(SelectOnly<?> groupKey, SelectOnly<?> stream, LambdaAnalysis lambda, MetamodelUtil metamodel, SymbExArgumentHandler parentArgumentScope, boolean hasInQueryStreamSource) {
		super(lambda, metamodel, parentArgumentScope, hasInQueryStreamSource);
		this.groupKey = groupKey;
		this.stream = stream;
	}

	@Override
	protected ColumnExpressions<?> handleLambdaArg(int argIndex, Type argType) throws TypedValueVisitorException {
		if (argIndex == 0)
			return groupKey.cols;
		throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
	}

	@Override
	protected JinqPostgresQuery<?> handleLambdaSubQueryArg(int argIndex, Type argType)
			throws TypedValueVisitorException {
		if (argIndex == 1)
			return stream;
		throw new TypedValueVisitorException("Lambda trying to access unknown lambda parameter");
	}
}
