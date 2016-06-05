package org.revenj.database.postgres.jinq.transform;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;

import java.util.List;

public interface MethodHandlerStatic {
	List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException;

	ColumnExpressions<?> handle(
			MethodCallValue.StaticMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException;
}