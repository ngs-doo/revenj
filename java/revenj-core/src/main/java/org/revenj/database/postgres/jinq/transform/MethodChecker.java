package org.revenj.database.postgres.jinq.transform;

import java.lang.reflect.Method;
import java.util.*;

import org.jinq.orm.stream.JinqStream;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter.OperationSideEffect;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

class MethodChecker implements PathAnalysisMethodChecker {
	private final Set<Class<?>> safeMethodAnnotations;
	private final Set<MethodSignature> safeMethods;
	private final Set<MethodSignature> safeStaticMethods;
	private final boolean isObjectEqualsSafe;
	private final boolean isCollectionContainsSafe;

	public final static MethodSignature objectEquals = new MethodSignature("java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
	public final static MethodSignature objectsEquals = new MethodSignature("java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");

	static {
		try {
			// I'm initializing some of these method signatures through reflection
			// instead of statically so that it's easier to find breakages due to method renaming etc.

			streamSelectAll = MethodSignature.fromMethod(JinqStream.class.getMethod("selectAll", JinqStream.Join.class));
			streamSelectAllList = MethodSignature.fromMethod(JinqStream.class.getMethod("selectAllList", JinqStream.JoinToIterable.class));
			streamJoinList = MethodSignature.fromMethod(JinqStream.class.getMethod("joinList", JinqStream.JoinToIterable.class));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Cannot initialize MethodChecker because it cannot find a needed method", e);
		}
	}

	final static Map<MethodSignature, MethodHandlerVirtual> jpqlFunctionMethods = new HashMap<>();
	final static Map<MethodSignature, MethodHandlerStatic> jpqlFunctionStaticMethods = new HashMap<>();

	static {
		ServiceLoader<MethodHandlerStatic> staticHandlers = ServiceLoader.load(MethodHandlerStatic.class);
		for (MethodHandlerStatic handler : staticHandlers) {
			try {
				for (MethodSignature signature : handler.getSupportedSignatures()) {
					jpqlFunctionStaticMethods.put(signature, handler);
				}
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		}

		ServiceLoader<MethodHandlerVirtual> virtualHandlers = ServiceLoader.load(MethodHandlerVirtual.class);
		for (MethodHandlerVirtual handler : virtualHandlers) {
			try {
				for (MethodSignature signature : handler.getSupportedSignatures()) {
					jpqlFunctionMethods.put(signature, handler);
				}
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		}
	}

	public final static MethodSignature streamSumInt = TransformationClassAnalyzer.streamSumInt;
	public final static MethodSignature streamSumDouble = TransformationClassAnalyzer.streamSumDouble;
	public final static MethodSignature streamSumLong = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumLong", "(Lorg/jinq/orm/stream/JinqStream$CollectLong;)Ljava/lang/Long;");
	public final static MethodSignature streamSumBigDecimal = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigDecimal", "(Lorg/jinq/orm/stream/JinqStream$CollectBigDecimal;)Ljava/math/BigDecimal;");
	public final static MethodSignature streamSumBigInteger = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumBigInteger", "(Lorg/jinq/orm/stream/JinqStream$CollectBigInteger;)Ljava/math/BigInteger;");
	public final static MethodSignature streamMax = TransformationClassAnalyzer.streamMax;
	public final static MethodSignature streamMin = TransformationClassAnalyzer.streamMin;
	public final static MethodSignature streamAvg = new MethodSignature("org/jinq/orm/stream/JinqStream", "avg", "(Lorg/jinq/orm/stream/JinqStream$CollectNumber;)Ljava/lang/Double;");
	public final static MethodSignature streamCount = new MethodSignature("org/jinq/orm/stream/JinqStream", "count", "()J");
	public final static MethodSignature streamDistinct = new MethodSignature("org/jinq/orm/stream/JinqStream", "distinct", "()Lorg/jinq/orm/stream/JinqStream;");
	public final static MethodSignature streamSelect = new MethodSignature("org/jinq/orm/stream/JinqStream", "select", "(Lorg/jinq/orm/stream/JinqStream$Select;)Lorg/jinq/orm/stream/JinqStream;");
	public final static MethodSignature streamSelectAll;
	public final static MethodSignature streamSelectAllList;
	public final static MethodSignature streamWhere = new MethodSignature("org/jinq/orm/stream/JinqStream", "where", "(Lorg/jinq/orm/stream/JinqStream$Where;)Lorg/jinq/orm/stream/JinqStream;");
	public final static MethodSignature streamJoin = new MethodSignature("org/jinq/orm/stream/JinqStream", "join", "(Lorg/jinq/orm/stream/JinqStream$Join;)Lorg/jinq/orm/stream/JinqStream;");
	public final static MethodSignature streamJoinList;
	public final static MethodSignature streamGetOnlyValue = new MethodSignature("org/jinq/orm/stream/JinqStream", "getOnlyValue", "()Ljava/lang/Object;");

	private static final Set<MethodSignature> subqueryMethods = new HashSet<>();

	static {
		subqueryMethods.add(streamSumInt);
		subqueryMethods.add(streamSumDouble);
		subqueryMethods.add(streamSumLong);
		subqueryMethods.add(streamSumBigInteger);
		subqueryMethods.add(streamSumBigDecimal);
		subqueryMethods.add(streamMax);
		subqueryMethods.add(streamMin);
		subqueryMethods.add(streamAvg);
		subqueryMethods.add(streamCount);
		subqueryMethods.add(streamDistinct);
		subqueryMethods.add(streamSelect);
		subqueryMethods.add(streamSelectAll);
		subqueryMethods.add(streamSelectAllList);
		subqueryMethods.add(streamWhere);
		subqueryMethods.add(streamJoin);
		subqueryMethods.add(streamJoinList);
		subqueryMethods.add(streamGetOnlyValue);
	}

	MethodChecker(Set<Class<?>> safeMethodAnnotations,
				  Set<MethodSignature> safeMethods,
				  Set<MethodSignature> safeStaticMethods,
				  boolean isObjectEqualsSafe,
				  boolean isCollectionContainsSafe) {
		this.safeMethodAnnotations = safeMethodAnnotations;
		this.safeMethods = safeMethods;
		this.safeStaticMethods = safeStaticMethods;
		this.isObjectEqualsSafe = isObjectEqualsSafe;
		this.isCollectionContainsSafe = isCollectionContainsSafe;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isStaticMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature)
	 */
	@Override
	public OperationSideEffect isStaticMethodSafe(MethodSignature m) {
		if (isObjectEqualsSafe && objectsEquals.equals(m)) {
			return OperationSideEffect.NONE;
		}
		return safeStaticMethods.contains(m) || jpqlFunctionStaticMethods.containsKey(m)
				? OperationSideEffect.NONE
				: OperationSideEffect.UNSAFE;
	}

	/* (non-Javadoc)
	 * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
	 */
	@Override
	public OperationSideEffect isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args) {
		if (isObjectEqualsSafe && objectEquals.equals(m)) {
			return OperationSideEffect.NONE;
		} else if (safeMethods.contains(m) || subqueryMethods.contains(m) || jpqlFunctionMethods.containsKey(m)) {
			return OperationSideEffect.NONE;
		} else {
			// Use reflection to get info about the method (or would it be better
			// to do this through direct bytecode inspection?), and see if it's
			// annotated as safe
			try {
				Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(m);
				// Special handling of Collection.contains() for subclasses of Collection.
				if (isCollectionContainsSafe
						&& "contains".equals(m.name)
						&& "(Ljava/lang/Object;)Z".equals(m.desc)
						&& Collection.class.isAssignableFrom(reflectedMethod.getDeclaringClass()))
					return OperationSideEffect.NONE;
				if (Annotations.methodHasSomeAnnotations(reflectedMethod,
						safeMethodAnnotations))
					return OperationSideEffect.NONE;
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				// Eat the error
			}
			return OperationSideEffect.UNSAFE;

		}
	}

	@Override
	public boolean isFluentChaining(MethodSignature sig) {
		return TransformationClassAnalyzer.stringBuilderAppendString.equals(sig);
	}

	@Override
	public boolean isPutFieldAllowed() {
		return false;
	}
}