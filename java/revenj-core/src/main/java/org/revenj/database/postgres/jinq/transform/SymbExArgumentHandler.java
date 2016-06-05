package org.revenj.database.postgres.jinq.transform;

import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;
import org.revenj.database.postgres.jinq.jpqlquery.ColumnExpressions;

// TODO: Creating a whole interface for handling arguments might be overkill. I'm not sure
//    how many variants there actually are

public interface SymbExArgumentHandler {
	ColumnExpressions<?> handleArg(int argIndex, Type argType) throws TypedValueVisitorException;

	JinqPostgresQuery<?> handleSubQueryArg(int argIndex, Type argType) throws TypedValueVisitorException;

	boolean checkIsInQueryStreamSource(int argIndex);

	ColumnExpressions<?> handleThisFieldRead(String name, Type argType) throws TypedValueVisitorException;

	JinqPostgresQuery<?> handleSubQueryThisFieldRead(String name, Type argType) throws TypedValueVisitorException;
}
