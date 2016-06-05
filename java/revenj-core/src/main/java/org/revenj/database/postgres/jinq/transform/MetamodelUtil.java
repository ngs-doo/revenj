package org.revenj.database.postgres.jinq.transform;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import org.revenj.patterns.Specification;

public abstract class MetamodelUtil {
	private final Set<Class<?>> safeMethodAnnotations;
	protected final Map<MethodSignature, MetamodelUtilAttribute> fieldMethods;
	protected final Map<MethodSignature, MetamodelUtilAttribute> nLinkMethods;
	protected final Set<MethodSignature> safeMethods;
	protected final Set<MethodSignature> safeStaticMethods;
	protected final Map<String, String> enums;
	protected final Map<MethodSignature, String> statics;
	protected final Set<String> knownEmbeddedtypes = new HashSet<>();
	protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods;
	protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethodsWithObjectEquals;
	protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> staticComparisonMethods;
	protected final Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> staticComparisonMethodsWithObjectEquals;
	protected final Map<Class<?>, Function> specificationRewrites;

	class MetamodelUtilAttribute {
		public final String name;
		public final boolean isAssociation;

		public MetamodelUtilAttribute(String name, boolean isAssociation) {
			this.name = name;
			this.isAssociation = isAssociation;
		}
	}

	public static final Map<MethodSignature, Integer> TUPLE_ACCESSORS = new HashMap<>();

	static {
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.pairGetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.pairGetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple3GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple4GetFour, 4);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetFour, 4);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple5GetFive, 5);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetFour, 4);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetFive, 5);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple6GetSix, 6);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetFour, 4);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetFive, 5);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetSix, 6);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple7GetSeven, 7);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetOne, 1);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetTwo, 2);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetThree, 3);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetFour, 4);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetFive, 5);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetSix, 6);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetSeven, 7);
		TUPLE_ACCESSORS.put(TransformationClassAnalyzer.tuple8GetEight, 8);
	}

	public static final MethodSignature inQueryStream = new MethodSignature("org/jinq/orm/stream/InQueryStreamSource", "stream", "(Ljava/lang/Class;)Lorg/jinq/orm/stream/JinqStream;");

	public MetamodelUtil() {
		enums = new HashMap<>();
		statics = new HashMap<>();
		comparisonMethods = new HashMap<>();
		staticComparisonMethods = new HashMap<>();
		safeMethodAnnotations = new HashSet<>();
		safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
		safeMethods = new HashSet<>();
		safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
		safeMethods.add(TransformationClassAnalyzer.integerIntValue);
		safeMethods.add(TransformationClassAnalyzer.longLongValue);
		safeMethods.add(TransformationClassAnalyzer.floatFloatValue);
		safeMethods.add(TransformationClassAnalyzer.doubleDoubleValue);
		safeMethods.add(TransformationClassAnalyzer.booleanBooleanValue);
		safeMethods.add(inQueryStream);
		safeStaticMethods = new HashSet<>();
		safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
		safeStaticMethods.add(TransformationClassAnalyzer.integerValueOf);
		safeStaticMethods.add(TransformationClassAnalyzer.longValueOf);
		safeStaticMethods.add(TransformationClassAnalyzer.doubleValueOf);
		safeStaticMethods.add(TransformationClassAnalyzer.floatValueOf);
		safeStaticMethods.add(TransformationClassAnalyzer.booleanValueOf);
		fieldMethods = new HashMap<>();
		nLinkMethods = new HashMap<>();
		comparisonMethodsWithObjectEquals = new HashMap<>();
		comparisonMethodsWithObjectEquals.put(MethodChecker.objectEquals, TypedValue.ComparisonValue.ComparisonOp.eq);
		staticComparisonMethodsWithObjectEquals = new HashMap<>();
		staticComparisonMethodsWithObjectEquals.put(MethodChecker.objectsEquals, TypedValue.ComparisonValue.ComparisonOp.eq);
		specificationRewrites = new HashMap<>();
	}

	protected void addProperty(Method method, String property) {
		MethodSignature signature = MethodSignature.fromMethod(method);
		fieldMethods.put(signature, new MetamodelUtilAttribute(property, false));
		safeMethods.add(signature);
	}

	protected void addStatic(Method method, String function) {
		MethodSignature signature = MethodSignature.fromMethod(method);
		statics.put(signature, function);
		safeStaticMethods.add(signature);
	}

	public void register(MethodHandlerVirtual handler) throws IOException {
		//TODO: temp hack. MethodChecked class needs major refactoring ;(
		try {
			for (MethodSignature signature : handler.getSupportedSignatures()) {
				MethodChecker.jpqlFunctionMethods.put(signature, handler);
			}
		} catch (NoSuchMethodException e) {
			throw new IOException(e);
		}
	}

	public void register(MethodHandlerStatic handler) throws IOException {
		try {
			for (MethodSignature signature : handler.getSupportedSignatures()) {
				MethodChecker.jpqlFunctionStaticMethods.put(signature, handler);
			}
		} catch (NoSuchMethodException e) {
			throw new IOException(e);
		}
	}

	public Optional<MethodHandlerVirtual> findVirtualHandler(MethodSignature method) {
		return Optional.ofNullable(MethodChecker.jpqlFunctionMethods.get(method));
	}

	private void insertNLinkMethod(String className, String methodName, String returnType, MetamodelUtilAttribute pluralAttribute) {
		MethodSignature methodSig = new MethodSignature(
				className,
				methodName,
				returnType);
		nLinkMethods.put(methodSig, pluralAttribute);
	}

	public void registerEnum(Class<?> enumClass, String dbName) {
		// Record the enum, and mark equals() using the enum as safe
		String enumTypeName = Type.getInternalName(enumClass);
		enums.put(enumTypeName, dbName);
		MethodSignature eqMethod = new MethodSignature(enumTypeName, "equals", "(Ljava/lang/Object;)Z");
		comparisonMethods.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
		comparisonMethodsWithObjectEquals.put(eqMethod, TypedValue.ComparisonValue.ComparisonOp.eq);
		safeMethods.add(eqMethod);
	}

	public <T, S extends Specification<T>> void registerSpecification(Class<S> manifest, Function<S, Specification<T>> conversion) {
		specificationRewrites.put(manifest, conversion);
	}

	public Function<Specification, Specification> lookupRewrite(Specification filter) {
		return specificationRewrites.get(filter.getClass());
	}

	public <U> boolean isKnownManagedType(String entityClassName) {
		return dataSourceNameFromClassName(entityClassName) != null
				|| knownEmbeddedtypes.contains(entityClassName);
	}

	public abstract <U> String dataSourceNameFromClass(Class<U> dataSource);

	public abstract String dataSourceNameFromClassName(String className);

	/**
	 * Returns true if a method is used to get a singular attribute field from an entity
	 *
	 * @param sig
	 * @return
	 */
	public boolean isSingularAttributeFieldMethod(MethodSignature sig) {
		return fieldMethods.containsKey(sig);
	}

	/**
	 * Given a method used to read a field of an entity, this returns the actual
	 * field name on the entity.
	 *
	 * @param sig
	 * @return
	 */
	public String fieldMethodToFieldName(MethodSignature sig) {
		return fieldMethods.get(sig).name;
	}

	/**
	 * Given a method used to read a field of an entity, this returns whether
	 * the field is an association type (i.e. represents a 1:1 or N:1 link)
	 *
	 * @param sig
	 * @return
	 */
	public boolean isFieldMethodAssociationType(MethodSignature sig) {
		return fieldMethods.get(sig).isAssociation;
	}

	/**
	 * Returns true if a method is used to get a plural attribute field from an entity
	 *
	 * @param sig
	 * @return
	 */
	public boolean isPluralAttributeLinkMethod(MethodSignature sig) {
		return nLinkMethods.containsKey(sig);
	}

	/**
	 * Given a method used for a 1:N or N:M navigational link, this returns the actual
	 * name of the link.
	 *
	 * @param sig
	 * @return
	 */
	public String nLinkMethodToLinkName(MethodSignature sig) {
		return nLinkMethods.get(sig).name;
	}

	public String getEnumName(String className) {
		return enums.get(className);
	}

	public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> getComparisonMethods(boolean withObjectEquals) {
		return withObjectEquals ? comparisonMethodsWithObjectEquals : comparisonMethods;
	}

	public Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> getStaticComparisonMethods(boolean withObjectEquals) {
		return withObjectEquals ? staticComparisonMethodsWithObjectEquals : staticComparisonMethods;
	}

	public Set<Class<?>> getSafeMethodAnnotations() {
		return safeMethodAnnotations;
	}

	public Set<MethodSignature> getSafeMethods() {
		return safeMethods;
	}

	public Set<MethodSignature> getSafeStaticMethods() {
		return safeStaticMethods;
	}

	public MethodChecker getMethodChecker(boolean isObjectEqualsSafe, boolean isCollectionContainsSafe) {
		return new MethodChecker(
				getSafeMethodAnnotations(),
				getSafeMethods(),
				getSafeStaticMethods(),
				isObjectEqualsSafe,
				isCollectionContainsSafe);
	}
}
