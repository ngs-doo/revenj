package org.revenj.database.postgres.jinq.transform.handlers;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.jinq.rebased.org.objectweb.asm.Type;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;
import org.revenj.database.postgres.jinq.jpqlquery.ConstantExpression;
import org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual;
import org.revenj.database.postgres.jinq.transform.SymbExPassDown;
import org.revenj.database.postgres.jinq.transform.SymbExToColumns;
import org.revenj.security.PermissionManager;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrincipalGetName implements MethodHandlerVirtual {
	@Override
	public List<MethodSignature> getSupportedSignatures() throws NoSuchMethodException {
		return Collections.singletonList(
				MethodSignature.fromMethod(Principal.class.getMethod("getName"))
		);
	}

	private static final String boundPrincipal =
			(new TypedValue.CastValue(
					Type.getObjectType(Principal.class.getName()),
					new MethodCallValue.VirtualMethodCallValue(
							"java/lang/ThreadLocal",
							"get",
							"()Ljava/lang/Object;",
							Collections.EMPTY_LIST,
							new TypedValue.GetStaticFieldValue("org/revenj/security/PermissionManager", "boundPrincipal", "()Ljava/lang/Object;")))).toString();

	@Override
	public ColumnExpressions<?> handle(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in,
			SymbExToColumns columns) throws TypedValueVisitorException {
		if (boundPrincipal.equals(val.base.toString())) {
			return ColumnExpressions.singleColumn(null, new ConstantExpression("'" + PermissionManager.boundPrincipal.get().getName().replace("'", "''") + "'"));
		}
		return null;
	}
}
