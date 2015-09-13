package org.revenj.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.postgres.jinq.jpqlquery.FunctionExpression;
import org.revenj.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.postgres.jinq.transform.SymbExToColumns;
import org.revenj.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.postgres.jinq.jpqlquery.ConstantExpression;
import org.revenj.postgres.jinq.transform.SymbExPassDown;

import java.util.Collections;
import java.util.List;

public class SubstringHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() {
		return Collections.singletonList(
				new MethodSignature("java/lang/String", "substring", "(II)Ljava/lang/String;")
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
		ColumnExpressions<?> endIndex = val.args.get(1).visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader,
				FunctionExpression.threeParam("SUBSTRING",
						base.getOnlyColumn(),
						new BinaryExpression(startIndex.getOnlyColumn(), "+", new ConstantExpression("1")),
						new BinaryExpression(endIndex.getOnlyColumn(), "-", startIndex.getOnlyColumn())));
	}
}
