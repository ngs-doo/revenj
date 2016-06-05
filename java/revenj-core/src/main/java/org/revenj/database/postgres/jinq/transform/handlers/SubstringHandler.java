package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubstringHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Arrays.asList(
				MethodSignature.fromMethod(String.class.getMethod("substring", int.class)),
				MethodSignature.fromMethod(String.class.getMethod("substring", int.class, int.class))
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		ColumnExpressions<?> startIndex = val.args.get(0).visit(columns, passdown);
		ArrayList<Expression> params = new ArrayList<>(3);
		params.add(base.getOnlyColumn());
		params.add(new BinaryExpression(startIndex.getOnlyColumn(), "+", new ConstantExpression("1")));
		if (val.args.size() == 2) {
			ColumnExpressions<?> endIndex = val.args.get(1).visit(columns, passdown);
			params.add(new BinaryExpression(endIndex.getOnlyColumn(), "-", startIndex.getOnlyColumn()));
		}
		return ColumnExpressions.singleColumn(base.reader, FunctionExpression.withParams("SUBSTRING", params));
	}
}
