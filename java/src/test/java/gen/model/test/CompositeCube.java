/*
* Created by DSL Platform
* v1.0.0.43041 
*/

package gen.model.test;



public class CompositeCube  extends org.revenj.postgres.PostgresOlapCubeQuery<gen.model.test.CompositeList> {
	
	public final static String number = "number";
	public final static String enn = "enn";
	public final static String hasEntities = "hasEntities";
	public final static String simple = "simple";
	public final static String indexes = "indexes";
	public final static String max = "max";
	public final static String min = "min";
	public final static String count = "count";
	public final static String hasSum = "hasSum";
	public final static String avgInd = "avgInd";
	
	
	public CompositeCube(
			org.revenj.patterns.ServiceLocator locator) {
		super(locator);
			final int context = 1;	
		
			cubeDimensions.put("number", n -> "\"" + n + "\".\"number\"");
			cubeConverters.put("number", reader -> org.revenj.postgres.converters.IntConverter.parse(reader));
		
			cubeDimensions.put("enn", n -> "\"" + n + "\".\"enn\"");
			cubeConverters.put("enn", rdr -> { gen.model.test.En[] __arg = null; java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(rdr, 1, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {__arg = __list.toArray(new gen.model.test.En[__list.size()]);} else __arg = new gen.model.test.En[] { }; return __arg; });
		
			cubeDimensions.put("hasEntities", n -> "\"" + n + "\".\"hasEntities\"");
			cubeConverters.put("hasEntities", reader -> org.revenj.postgres.converters.BoolConverter.parse(reader));
		
			cubeDimensions.put("simple", n -> "\"" + n + "\".\"simple\"");
			cubeConverters.put("simple", rdr -> locator.resolve(gen.model.test.converters.SimpleConverter.class).from(rdr, 1));
		
			cubeDimensions.put("indexes", n -> "\"" + n + "\".\"indexes\"");
			cubeConverters.put("indexes", reader -> { Long[] __arg = null; { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, true); if(__list != null) {__arg = __list.toArray(new Long[__list.size()]);} } return __arg; });
		
			cubeFacts.put("max", n -> "MAX(\"" + n + "\".\"change\")");
			cubeConverters.put("max", reader -> org.revenj.postgres.converters.DateConverter.parse(reader, true));
		
			cubeFacts.put("min", n -> "MIN(\"" + n + "\".\"change\")");
			cubeConverters.put("min", reader -> org.revenj.postgres.converters.DateConverter.parse(reader, true));
		
			cubeFacts.put("count", n -> "COUNT(\"" + n + "\".\"enn\")");
			cubeConverters.put("count", org.revenj.postgres.converters.LongConverter::parse);
		
			cubeFacts.put("hasSum", n -> "SUM(\"" + n + "\".\"hasEntities\"::int)");
			cubeConverters.put("hasSum", org.revenj.postgres.converters.IntConverter::parse);
		
			cubeFacts.put("avgInd", n -> "AVG((SELECT AVG(x) FROM UNNEST(\"" + n + "\".\"indexes\") x))");
			cubeConverters.put("avgInd", rdr -> org.revenj.postgres.converters.DecimalConverter.parse(rdr, true));
	}

	
	@Override
	protected org.revenj.patterns.Specification<gen.model.test.CompositeList> rewriteSpecification(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification) {
		return gen.model.test.repositories.CompositeListRepository.rewriteSpecificationToLambda(specification);
	}

	@Override
	protected String getSource() { return "\"test\".\"CompositeList_snowflake\""; }
}
