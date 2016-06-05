package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.UnaryExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UuidParseHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Collections.singletonList(
				MethodSignature.fromMethod(UUID.class.getMethod("fromString", String.class))
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
		ColumnExpressions<?> base = val.args.get(0).visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader, UnaryExpression.postfix("::uuid", base.getOnlyColumn()));
	}
}
