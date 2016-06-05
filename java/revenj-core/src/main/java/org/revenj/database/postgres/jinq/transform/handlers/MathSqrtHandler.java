package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.FunctionExpression;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.SimpleRowReader;
import org.revenj.database.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;

import java.util.Collections;
import java.util.List;

public class MathSqrtHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Collections.singletonList(
				new MethodSignature("java/lang/Math", "sqrt", "(D)D")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
		TypedValue baseVal = val.args.get(0);
		if (columns.isWideningCast(baseVal)) {
			baseVal = columns.skipWideningCast(baseVal);
		}
		ColumnExpressions<?> base = baseVal.visit(columns, passdown);
		return ColumnExpressions.singleColumn(SimpleRowReader.READER,
				FunctionExpression.singleParam("SQRT", base.getOnlyColumn()));
	}
}
