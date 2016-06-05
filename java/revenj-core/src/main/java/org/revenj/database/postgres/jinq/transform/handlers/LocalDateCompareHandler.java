package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.database.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalDateCompareHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Arrays.asList(
				new MethodSignature("java/time/LocalDate", "compareTo", "(Ljava/time/chrono/ChronoLocalDate;)I"),
				new MethodSignature("org/joda/time/LocalDate", "compareTo", "(Lorg/joda/time/ReadablePartial;)I")
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
