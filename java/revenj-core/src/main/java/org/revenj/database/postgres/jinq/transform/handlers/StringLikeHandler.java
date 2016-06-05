package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.BinaryExpression;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.ConstantExpression;
import org.revenj.database.postgres.jinq.jpqlquery.FunctionExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.*;

public class StringLikeHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Arrays.asList(
				MethodSignature.fromMethod(String.class.getMethod("contains", CharSequence.class)),
				MethodSignature.fromMethod(String.class.getMethod("startsWith", String.class)),
				MethodSignature.fromMethod(String.class.getMethod("endsWith", String.class))
		);
	}

	private static final Map<String, Map.Entry<String, String>> comparisons = new HashMap<>();
	static {
		comparisons.put("contains", new AbstractMap.SimpleEntry<>("LIKE '%' || ", " || '%'"));
		comparisons.put("startsWith", new AbstractMap.SimpleEntry<>("LIKE ", " || '%'"));
		comparisons.put("endsWith", new AbstractMap.SimpleEntry<>("LIKE '%' || ", ""));
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		ColumnExpressions<?> search = val.args.get(0).visit(columns, passdown);
		Map.Entry<String, String> found = comparisons.get(val.name);
		if (found == null) return null;
		return ColumnExpressions.singleColumn(base.reader,
				new BinaryExpression(
						"",
						base.getOnlyColumn(),
						found.getKey(),
						search.getOnlyColumn(),
						found.getValue()));
	}
}
