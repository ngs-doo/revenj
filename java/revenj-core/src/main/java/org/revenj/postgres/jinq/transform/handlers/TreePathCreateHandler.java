package org.revenj.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.TreePath;
import org.revenj.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.postgres.jinq.jpqlquery.SimpleRowReader;
import org.revenj.postgres.jinq.jpqlquery.UnaryExpression;
import org.revenj.postgres.jinq.transform.MethodHandlerStatic;
import org.revenj.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.postgres.jinq.transform.SymbExPassDown;
import org.revenj.postgres.jinq.transform.SymbExToColumns;

import java.util.Collections;
import java.util.List;

public class TreePathCreateHandler implements MethodHandlerStatic {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Collections.singletonList(
				MethodSignature.fromMethod(TreePath.class.getMethod("create", String.class))
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> arg = val.args.get(0).visit(columns, passdown);
		return ColumnExpressions.singleColumn(SimpleRowReader.READER,
				UnaryExpression.postfix("::ltree", arg.getOnlyColumn()));
	}
}
