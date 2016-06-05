package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.UnaryExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.Arrays;
import java.util.List;

public class ToStringHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Arrays.asList(
				MethodSignature.fromMethod(StringBuilder.class.getMethod("toString")),
				new MethodSignature("java/lang/Integer", "toString", "()Ljava/lang/String;"),
				new MethodSignature("java/lang/Long", "toString", "()Ljava/lang/String;"),
				new MethodSignature("java/math/BigDecimal", "toString", "()Ljava/lang/String;"),
				new MethodSignature("java/util/UUID", "toString", "()Ljava/lang/String;")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader, UnaryExpression.postfix("::text", base.getOnlyColumn()));
	}
}
