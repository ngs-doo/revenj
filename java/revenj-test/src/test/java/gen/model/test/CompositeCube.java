package gen.model.test;



public class CompositeCube  extends org.revenj.postgres.PostgresOlapCubeQuery<gen.model.test.CompositeList> {
	
	public final static String number = "number";
	public final static String max = "max";
	public final static String min = "min";
	
	
	public CompositeCube(
			org.revenj.patterns.ServiceLocator locator) {
		super(locator);	
		cubeDimensions.put("number", n -> "\"" + n + "\".\"number\"");
		cubeFacts.put("max", n -> "MAX(\"" + n + "\".\"change\")");
		cubeFacts.put("min", n -> "MIN(\"" + n + "\".\"change\")");
	}

	
	@Override
	protected org.revenj.patterns.Specification<gen.model.test.CompositeList> rewriteSpecification(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification) {
		return gen.model.test.repositories.CompositeListRepository.rewriteSpecificationToLambda(specification);
	}

	@Override
	protected String getSource() { return "\"test\".\"CompositeList_snowflake\""; }
	public static final Class<?> DataSource = gen.model.test.CompositeList.class;
}
