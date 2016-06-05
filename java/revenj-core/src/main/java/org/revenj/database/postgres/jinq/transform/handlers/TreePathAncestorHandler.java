package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.TreePath;
import org.revenj.database.postgres.jinq.jpqlquery.*;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.Collections;
import java.util.List;

public class TreePathAncestorHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Collections.singletonList(
				MethodSignature.fromMethod(TreePath.class.getMethod("isAncestor", TreePath.class))
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
				new BinaryExpression("(", base.getOnlyColumn(), "::ltree @> ", arg.getOnlyColumn(), "::ltree)"));
	}
}
