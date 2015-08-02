package org.revenj.postgres.jinq.jpqlquery;

/**
 * When generating a Postgres query string, we also need to store a list of generated
 * parameters that will need to substituted into the query before the query can be
 * run. This class is the data structure holding info about these parameters.
 */
public class GeneratedQueryParameter {
	// TODO: It doesn't feel right to make these public, but using these parameters
	//    requires knowledge of LambdaInfo, and I don't want this package to depend
	//    on the LambdaInfo package.
	public final String paramName;
	public final int lambdaIndex;
	public final int argIndex;
	public final String fieldName;

	public GeneratedQueryParameter(String paramName, int lambdaIndex, int argIndex) {
		this.paramName = paramName;
		this.lambdaIndex = lambdaIndex;
		this.argIndex = argIndex;
		this.fieldName = null;
	}

	public GeneratedQueryParameter(String paramName, int lambdaIndex, String fieldName) {
		this.paramName = paramName;
		this.lambdaIndex = lambdaIndex;
		this.argIndex = -1;
		this.fieldName = fieldName;
	}
}
