package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.FunctionExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.Arrays;
import java.util.List;

public class AbsStaticHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Arrays.asList(
				new MethodSignature("java/lang/Math", "abs", "(D)D"),
				new MethodSignature("java/lang/Math", "abs", "(I)I"),
				new MethodSignature("java/lang/Math", "abs", "(J)J")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
		ColumnExpressions<?> base = val.args.get(0).visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader, FunctionExpression.singleParam("ABS", base.getOnlyColumn()));
	}
}
