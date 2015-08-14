package org.revenj.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.postgres.jinq.transform.SymbExPassDown;
import org.revenj.postgres.jinq.transform.SymbExToColumns;

import java.util.Collections;
import java.util.List;

public class LocalDateCompareHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Collections.singletonList(
				new MethodSignature("java/time/LocalDate", "compareTo", "(Ljava/time/chrono/ChronoLocalDate;)I")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		ColumnExpressions<?> arg = val.args.get(0).visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader,
				new BinaryExpression("(", base.getOnlyColumn(), "-", arg.getOnlyColumn(), ")"));
	}
}
