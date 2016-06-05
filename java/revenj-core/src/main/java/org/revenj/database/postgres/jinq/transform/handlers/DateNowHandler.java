package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.ConstantExpression;
import org.revenj.database.postgres.jinq.jpqlquery.SimpleRowReader;
import org.revenj.database.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

public class DateNowHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Arrays.asList(
				MethodSignature.fromMethod(LocalDate.class.getMethod("now")),
				MethodSignature.fromMethod(OffsetDateTime.class.getMethod("now")),
				new MethodSignature("org/joda/LocalDate", "now", "()Lorg/joda/LocalDate;")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression("CURRENT_DATE"));
	}
}
