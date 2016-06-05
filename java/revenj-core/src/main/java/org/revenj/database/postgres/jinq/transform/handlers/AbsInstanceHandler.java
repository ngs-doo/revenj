package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.FunctionExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;

import java.util.Arrays;
import java.util.List;

public class AbsInstanceHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Arrays.asList(
				new MethodSignature("java/math/BigDecimal", "abs", "()Ljava/math/BigDecimal;"),
				new MethodSignature("java/math/BigInteger", "abs", "()Ljava/math/BigInteger;")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader, FunctionExpression.singleParam("ABS", base.getOnlyColumn()));
	}
}
