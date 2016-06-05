package org.revenj.database.postgres.jinq.jpqlquery;

import org.revenj.database.postgres.jinq.transform.LambdaInfo;

import java.util.function.Function;

/**
 * When generating a Postgres query string, we also need to store a list of generated
 * parameters that will need to substituted into the query before the query can be
 * run. This class is the data structure holding info about these parameters.
 */
public class GeneratedQueryParameter {
	public final String paramName;
	public final int lambdaIndex;
	public final int argIndex;
	public final String fieldName;
	public final String javaType;
	public final String sqlType;
	public final Function<LambdaInfo, Object> getValue;

	public GeneratedQueryParameter(String paramName, int lambdaIndex, int argIndex, String type) {
		this.paramName = paramName;
		this.lambdaIndex = lambdaIndex;
		this.argIndex = argIndex;
		this.fieldName = null;
		this.javaType = type;
		this.sqlType = null;
		this.getValue = li -> li.getCapturedArg(argIndex);
	}

	public GeneratedQueryParameter(String paramName, int lambdaIndex, String fieldName, String type) {
		this.paramName = paramName;
		this.lambdaIndex = lambdaIndex;
		this.argIndex = -1;
		this.fieldName = fieldName;
		this.javaType = type;
		this.sqlType = null;
		this.getValue = li -> li.getField(fieldName);
	}

	public GeneratedQueryParameter(String paramName, int lambdaIndex, Function<LambdaInfo, Object> getValue, String javaType, String sqlType) {
		this.paramName = paramName;
		this.lambdaIndex = lambdaIndex;
		this.argIndex = -1;
		this.fieldName = null;
		this.javaType = javaType;
		this.sqlType = sqlType;
		this.getValue = getValue;
	}
}
