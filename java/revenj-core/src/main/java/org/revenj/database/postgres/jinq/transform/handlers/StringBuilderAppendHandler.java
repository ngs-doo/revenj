package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;

import java.util.Arrays;
import java.util.List;

public class StringBuilderAppendHandler implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Arrays.asList(
				MethodSignature.fromMethod(StringBuilder.class.getMethod("append", Object.class)),
				MethodSignature.fromMethod(StringBuilder.class.getMethod("append", int.class)),
				MethodSignature.fromMethod(StringBuilder.class.getMethod("append", long.class)),
				MethodSignature.fromMethod(StringBuilder.class.getMethod("append", float.class)),
				MethodSignature.fromMethod(StringBuilder.class.getMethod("append", double.class)),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/StringBuilder;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/Integer;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/Long;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/Float;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/lang/Double;)Ljava/lang/StringBuilder;"),
				new MethodSignature("java/lang/StringBuilder", "append", "(Ljava/math/BigDecimal;)Ljava/lang/StringBuilder;")
		);
	}

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> arg = val.args.get(0).visit(columns, passdown);
		if (val.base instanceof MethodCallValue.VirtualMethodCallValue) {
			MethodCallValue.VirtualMethodCallValue vmc = (MethodCallValue.VirtualMethodCallValue)val.base;
			if (vmc.name.equals("<init>") && vmc.desc.equals("()V")) {
				return ColumnExpressions.singleColumn(SimpleRowReader.READER, arg.getOnlyColumn());
			}
		}
		ColumnExpressions<?> base = val.base.visit(columns, passdown);
		return ColumnExpressions.singleColumn(base.reader,
				new BinaryExpression("(", base.getOnlyColumn(), " || ", arg.getOnlyColumn(), ")"));
	}
}
