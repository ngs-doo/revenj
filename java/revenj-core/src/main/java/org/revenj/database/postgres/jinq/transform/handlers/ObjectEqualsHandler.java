package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.Arrays;
import java.util.List;

public class ObjectEqualsHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Arrays.asList(
				new MethodSignature("java/time/LocalDate", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/time/LocalDate", "isEqual", "(Ljava/time/chrono/ChronoLocalDate;)Z"),
				new MethodSignature("java/util/UUID", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/lang/Long", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/lang/Integer", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/lang/Float", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/lang/Double", "equals", "(Ljava/lang/Object;)Z"),
				new MethodSignature("java/math/BigDecimal", "equals", "(Ljava/lang/Object;)Z")
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
				new BinaryExpression(base.getOnlyColumn(), "=", arg.getOnlyColumn()));
	}
}
