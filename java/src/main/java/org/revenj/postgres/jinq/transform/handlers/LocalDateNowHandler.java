package org.revenj.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.postgres.jinq.jpqlquery.ConstantExpression;
import org.revenj.postgres.jinq.jpqlquery.SimpleRowReader;
import org.revenj.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.postgres.jinq.transform.SymbExPassDown;
import org.revenj.postgres.jinq.transform.SymbExToColumns;

import java.util.Collections;
import java.util.List;

public class LocalDateNowHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Collections.singletonList(
				new MethodSignature("java/time/LocalDate", "now", "()Ljava/time/LocalDate;")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression("CURRENT_DATE"));
	}
}
