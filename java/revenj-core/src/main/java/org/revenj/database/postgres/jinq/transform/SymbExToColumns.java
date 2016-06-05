package org.revenj.database.postgres.jinq.transform;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue.NullConstant;
import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.database.postgres.jinq.jpqlquery.*;

public class SymbExToColumns extends TypedValueVisitor<SymbExPassDown, ColumnExpressions<?>, TypedValueVisitorException> {
	final SymbExArgumentHandler argHandler;
	public final int lambdaIndex;
	final RevenjQueryTransformConfiguration config;

	SymbExToColumns(RevenjQueryTransformConfiguration config, SymbExArgumentHandler argumentHandler, int lambdaIndex) {
		this.config = config;
		this.argHandler = argumentHandler;
		this.lambdaIndex = lambdaIndex;
	}

	@Override
	public ColumnExpressions<?> defaultValue(TypedValue val, SymbExPassDown in) throws TypedValueVisitorException {
		if (val instanceof ConstantValue) {
			ConstantValue cv = (ConstantValue)val;
			if (cv.getType() == Type.FLOAT_TYPE
					|| cv.getType() == Type.DOUBLE_TYPE
					|| cv.getType() == Type.INT_TYPE
					|| cv.getType() == Type.LONG_TYPE) {
				return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(cv.toString()));
			}
		}
		throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
	}

	@Override
	public ColumnExpressions<?> argValue(TypedValue.ArgValue val, SymbExPassDown in) throws TypedValueVisitorException {
		int index = val.getIndex();
		return argHandler.handleArg(index, val.getType());
	}

	@Override
	public ColumnExpressions<?> booleanConstantValue(ConstantValue.BooleanConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(val.val ? "true" : "false"));
	}

	@Override
	public ColumnExpressions<?> integerConstantValue(ConstantValue.IntegerConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(Integer.toString(val.val)));
	}

	@Override
	public ColumnExpressions<?> longConstantValue(ConstantValue.LongConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(Long.toString(val.val)));
	}

	@Override
	public ColumnExpressions<?> doubleConstantValue(ConstantValue.DoubleConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(Double.toString(val.val)));
	}

	@Override
	public ColumnExpressions<?> stringConstantValue(ConstantValue.StringConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression("'" + val.val.replaceAll("'", "''") + "'"));
	}

	@Override
	public ColumnExpressions<?> nullConstantValue(NullConstant val, SymbExPassDown in) throws TypedValueVisitorException {
		throw new TypedValueVisitorException("Unexpected NULL value");
	}

	@Override
	public ColumnExpressions<?> unaryMathOpValue(TypedValue.UnaryMathOpValue val, SymbExPassDown in) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		ColumnExpressions<?> left = val.operand.visit(this, passdown);
		return ColumnExpressions.singleColumn(left.reader,
				UnaryExpression.prefix(val.op.getOpString(), left.getOnlyColumn()));
	}

	@Override
	public ColumnExpressions<?> notOpValue(TypedValue.NotValue val, SymbExPassDown in) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, true);
		ColumnExpressions<?> left = val.operand.visit(this, passdown);
		return ColumnExpressions.singleColumn(left.reader, UnaryExpression.prefix("NOT", left.getOnlyColumn()));
	}

	@Override
	public ColumnExpressions<?> getStaticFieldValue(TypedValue.GetStaticFieldValue val, SymbExPassDown in) throws TypedValueVisitorException {
		// Check if we're just reading an enum constant
		String dbEnum = config.metamodel.getEnumName(val.owner);
		if (dbEnum != null) {
			return ColumnExpressions.singleColumn(SimpleRowReader.READER,
					new ConstantExpression("'" + val.name + "'::" + dbEnum));
		} else if ("java/lang/Boolean".equals(val.owner)) {
			return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression(val.name));
		}
		return defaultValue(val, in);
	}

	@Override
	public ColumnExpressions<?> castValue(TypedValue.CastValue val, SymbExPassDown in) throws TypedValueVisitorException {
		if (val.isPrimitive()) {
			throw new TypedValueVisitorException("Casts of primitive values is not support in JINQ");
		}
		return val.operand.visit(this, SymbExPassDown.with(val, in.isExpectingConditional));
	}

	public boolean isWideningCast(TypedValue val) {
		if (val instanceof TypedValue.CastValue) {
			TypedValue.CastValue castedVal = (TypedValue.CastValue) val;
			Type toType = castedVal.getType();
			Type fromType = castedVal.operand.getType();
			if (!numericPromotionPriority.containsKey(fromType)) return false;
			if (!numericPromotionPriority.containsKey(toType)) return false;
			if (numericPromotionPriority.get(toType) > numericPromotionPriority.get(fromType))
				return true;
		} else if (val instanceof MethodCallValue.VirtualMethodCallValue) {
			MethodCallValue methodCall = (MethodCallValue.VirtualMethodCallValue) val;
			MethodSignature sig = methodCall.getSignature();
			if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
					|| sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
					|| sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger)) {
				return true;
			} else if (sig.equals(TransformationClassAnalyzer.bigDecimalDoubleValue)
					|| sig.equals(TransformationClassAnalyzer.bigIntegerDoubleValue)) {
				return true;
			}

		} else if (val instanceof MethodCallValue.StaticMethodCallValue) {
			MethodCallValue methodCall = (MethodCallValue.StaticMethodCallValue) val;
			MethodSignature sig = methodCall.getSignature();
			if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong)) {
				return true;
			}
		}
		return false;
	}

	public TypedValue skipWideningCast(TypedValue val) throws TypedValueVisitorException {
		if (!isWideningCast(val)) return val;
		if (val instanceof TypedValue.CastValue) {
			TypedValue.CastValue castedVal = (TypedValue.CastValue) val;
			return skipWideningCast(castedVal.operand);
		} else if (val instanceof MethodCallValue.VirtualMethodCallValue) {
			MethodCallValue.VirtualMethodCallValue methodCall = (MethodCallValue.VirtualMethodCallValue) val;
			MethodSignature sig = methodCall.getSignature();
			if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
					|| sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
					|| sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger)) {
				return skipWideningCast(methodCall.args.get(0));
			} else if (sig.equals(TransformationClassAnalyzer.bigDecimalDoubleValue)
					|| sig.equals(TransformationClassAnalyzer.bigIntegerDoubleValue)) {
				return skipWideningCast(methodCall.base);
			}
		} else if (val instanceof MethodCallValue.StaticMethodCallValue) {
			MethodCallValue methodCall = (MethodCallValue.StaticMethodCallValue) val;
			MethodSignature sig = methodCall.getSignature();
			if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong)) {
				return skipWideningCast(methodCall.args.get(0));
			}
		}
		throw new IllegalArgumentException("Cannot skip an unknown widening cast type");
	}

	private <U> ColumnExpressions<U> binaryOpWithNull(
			String opString,
			TypedValue leftVal,
			TypedValue rightVal,
			SymbExPassDown passdown) throws TypedValueVisitorException {
		if (!("=".equals(opString) || "<>".equals(opString)))
			throw new TypedValueVisitorException("Unhandled operation involving NULL");
		if (leftVal instanceof NullConstant && rightVal instanceof NullConstant)
			throw new TypedValueVisitorException("Cannot handle comparisons involving two NULLs");
		TypedValue operandVal;
		if (leftVal instanceof NullConstant)
			operandVal = rightVal;
		else
			operandVal = leftVal;
		ColumnExpressions<?> operand = operandVal.visit(this, passdown);
		if ("=".equals(opString))
			return ColumnExpressions.singleColumn(SimpleRowReader.READER,
					UnaryExpression.postfix("IS NULL", operand.getOnlyColumn()));
		else
			return ColumnExpressions.singleColumn(SimpleRowReader.READER,
					UnaryExpression.postfix("IS NOT NULL", operand.getOnlyColumn()));
	}

	private <U> ColumnExpressions<U> binaryOpWithNumericPromotion(
			String opString,
			TypedValue leftVal,
			TypedValue rightVal,
			SymbExPassDown passdown) throws TypedValueVisitorException {
		boolean isFinalTypeFromLeft = true;
		// Handle operations with NULL separately
		if (leftVal instanceof NullConstant || rightVal instanceof NullConstant)
			return binaryOpWithNull(opString, leftVal, rightVal, passdown);
		// Check if we have a valid numeric promotion (i.e. one side has a widening cast
		// to match the type of the other side).
		assert (leftVal.getType().equals(rightVal.getType())
				|| (leftVal.getType().getInternalName().equals("java/lang/Object") && config.isObjectEqualsSafe)// in Scala, many comparisons are done on Objects
				|| (rightVal.getType().getInternalName().equals("java/lang/Object") && config.isObjectEqualsSafe));
		if (isWideningCast(leftVal)) {
			if (!isWideningCast(rightVal)) {
				leftVal = skipWideningCast(leftVal);
				isFinalTypeFromLeft = false;
			}
		} else if (isWideningCast(rightVal)) {
			rightVal = skipWideningCast(rightVal);
		}
		// Actually translate the expressions now
		ColumnExpressions<U> left = (ColumnExpressions<U>) leftVal.visit(this, passdown);
		ColumnExpressions<U> right = (ColumnExpressions<U>) rightVal.visit(this, passdown);
		return ColumnExpressions.singleColumn(isFinalTypeFromLeft ? left.reader : right.reader,
				new BinaryExpression(left.getOnlyColumn(), opString, right.getOnlyColumn()));
	}

	@Override
	public ColumnExpressions<?> mathOpValue(TypedValue.MathOpValue val, SymbExPassDown in) throws TypedValueVisitorException {
		if (val.op == TypedValue.MathOpValue.Op.cmp)
			throw new TypedValueVisitorException("cmp operator was not converted to a boolean operator");
		if (val.op == TypedValue.MathOpValue.Op.mod) {
			if (val.left.getType().equals(Type.INT_TYPE) || val.right.getType().equals(Type.INT_TYPE)) {
				SymbExPassDown passdown = SymbExPassDown.with(val, false);
				ColumnExpressions<?> left = val.left.visit(this, passdown);
				ColumnExpressions<?> right = val.right.visit(this, passdown);
				return ColumnExpressions.singleColumn(left.reader,
						new BinaryExpression(left.getOnlyColumn(), "%", right.getOnlyColumn()));
			}
			throw new TypedValueVisitorException("mod operator cannot be used for the given types.");
		}
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		return binaryOpWithNumericPromotion(val.sqlOpString(), val.left, val.right, passdown);
	}

	@Override
	public ColumnExpressions<?> comparisonOpValue(TypedValue.ComparisonValue val, SymbExPassDown in) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(val, false);
		return binaryOpWithNumericPromotion(val.sqlOpString(), val.left, val.right, passdown);
	}

	private boolean isAggregateMethod(MethodSignature sig) {
		return sig.equals(MethodChecker.streamSumInt)
				|| sig.equals(MethodChecker.streamSumDouble)
				|| sig.equals(MethodChecker.streamSumLong)
				|| sig.equals(MethodChecker.streamSumBigInteger)
				|| sig.equals(MethodChecker.streamSumBigDecimal)
				|| sig.equals(MethodChecker.streamMax)
				|| sig.equals(MethodChecker.streamMin)
				|| sig.equals(MethodChecker.streamAvg)
				|| sig.equals(MethodChecker.streamCount);
	}

	@Override
	public ColumnExpressions<?> virtualMethodCallValue(
			MethodCallValue.VirtualMethodCallValue val,
			SymbExPassDown in) throws TypedValueVisitorException {
		MethodSignature sig = val.getSignature();
		MethodHandlerVirtual handler;
		if (TransformationClassAnalyzer.newPair.equals(sig)
				|| TransformationClassAnalyzer.newTuple3.equals(sig)
				|| TransformationClassAnalyzer.newTuple4.equals(sig)
				|| TransformationClassAnalyzer.newTuple5.equals(sig)
				|| TransformationClassAnalyzer.newTuple6.equals(sig)
				|| TransformationClassAnalyzer.newTuple7.equals(sig)
				|| TransformationClassAnalyzer.newTuple8.equals(sig)) {
			ColumnExpressions<?>[] vals = new ColumnExpressions<?>[val.args.size()];
			// TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
			SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
			for (int n = 0; n < vals.length; n++)
				vals[n] = val.args.get(n).visit(this, passdown);
			RowReader<?>[] valReaders = new RowReader[vals.length];
			for (int n = 0; n < vals.length; n++)
				valReaders[n] = vals[n].reader;

			ColumnExpressions<?> toReturn = new ColumnExpressions<>(TupleRowReader.createReaderForTuple(sig.owner, valReaders));
			for (int n = 0; n < vals.length; n++) {
				toReturn.columns.addAll(vals[n].columns);
			}
			return toReturn;
		} else if (config.metamodel.isSingularAttributeFieldMethod(sig)) {
			String fieldName = config.metamodel.fieldMethodToFieldName(sig);
			SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
			ColumnExpressions<?> base = val.base.visit(this, passdown);
			if (in.isExpectingConditional &&
					(sig.getReturnType().equals(Type.BOOLEAN_TYPE)
							|| sig.getReturnType().equals(Type.getObjectType("java/lang/Boolean")))) {
				return ColumnExpressions.singleColumn(SimpleRowReader.READER,
						new BinaryExpression(
								new ReadFieldExpression(base.getOnlyColumn(), fieldName),
								"=",
								new ConstantExpression("TRUE")));
			}
			return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ReadFieldExpression(base.getOnlyColumn(), fieldName));
		} else if (MetamodelUtil.TUPLE_ACCESSORS.containsKey(sig)) {
			int idx = MetamodelUtil.TUPLE_ACCESSORS.get(sig) - 1;
			// TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
			SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
			ColumnExpressions<?> base = val.base.visit(this, passdown);
			RowReader<?> subreader = ((TupleRowReader<?>) base.reader).getReaderForIndex(idx);
			ColumnExpressions<?> toReturn = new ColumnExpressions<>(subreader);
			int baseOffset = ((TupleRowReader<?>) base.reader).getColumnForIndex(idx);
			for (int n = 0; n < subreader.getNumColumns(); n++)
				toReturn.columns.add(base.columns.get(n + baseOffset));
			return toReturn;
		} else if (sig.equals(TransformationClassAnalyzer.integerIntValue)
				|| sig.equals(TransformationClassAnalyzer.longLongValue)
				|| sig.equals(TransformationClassAnalyzer.doubleDoubleValue)
				|| sig.equals(TransformationClassAnalyzer.floatFloatValue)
				|| sig.equals(TransformationClassAnalyzer.booleanBooleanValue)) {
			SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
			ColumnExpressions<?> base = val.base.visit(this, passdown);
			return base;
		} else if (sig.equals(TransformationClassAnalyzer.newBigDecimalLong)
				|| sig.equals(TransformationClassAnalyzer.newBigDecimalDouble)
				|| sig.equals(TransformationClassAnalyzer.newBigDecimalInt)
				|| sig.equals(TransformationClassAnalyzer.newBigDecimalBigInteger)) {
			throw new TypedValueVisitorException("New BigDecimals can only be created in the context of numeric promotion");
		} else if (isAggregateMethod(sig)) {
			SymbExPassDown passdown = SymbExPassDown.with(val, false);

			// Check out what stream we're aggregating
			SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, true, lambdaIndex);
			JinqPostgresQuery<?> subQuery = val.base.visit(translator, passdown);

			// Extract the lambda used
			LambdaAnalysis lambda = null;
			if (val.args.size() > 0) {
				if (!(val.args.get(0) instanceof LambdaFactory))
					throw new TypedValueVisitorException("Expecting a lambda factory for aggregate method");
				LambdaFactory lambdaFactory = (LambdaFactory) val.args.get(0);
				try {
					lambda = LambdaAnalysis.analyzeMethod(config.metamodel, config.alternateClassLoader, config.isObjectEqualsSafe, config.isAllEqualsSafe, config.isCollectionContainsSafe, lambdaFactory.getLambdaMethod(), lambdaFactory.getCapturedArgs(), true);
				} catch (Exception e) {
					throw new TypedValueVisitorException("Could not analyze the lambda code", e);
				}
			}

			try {
				AggregateTransform transform;
				if (sig.equals(MethodChecker.streamSumInt)
						|| sig.equals(MethodChecker.streamSumLong)
						|| sig.equals(MethodChecker.streamSumDouble)
						|| sig.equals(MethodChecker.streamSumBigDecimal)
						|| sig.equals(MethodChecker.streamSumBigInteger))
					transform = new AggregateTransform(config, AggregateTransform.AggregateType.SUM);
				else if (sig.equals(MethodChecker.streamMax))
					transform = new AggregateTransform(config, AggregateTransform.AggregateType.MAX);
				else if (sig.equals(MethodChecker.streamMin))
					transform = new AggregateTransform(config, AggregateTransform.AggregateType.MIN);
				else if (sig.equals(MethodChecker.streamAvg))
					transform = new AggregateTransform(config, AggregateTransform.AggregateType.AVG);
				else if (sig.equals(MethodChecker.streamCount))
					transform = new AggregateTransform(config, AggregateTransform.AggregateType.COUNT);
				else
					throw new TypedValueVisitorException("Unhandled aggregate operation");
				JinqPostgresQuery<?> aggregatedQuery = transform.apply(subQuery, lambda, argHandler);
				// Return the aggregated columns that we've now calculated
				if (aggregatedQuery.getClass() == SelectOnly.class) {
					SelectOnly<?> select = (SelectOnly<?>) aggregatedQuery;
					return select.cols;
				} else if (aggregatedQuery.isValidSubquery() && aggregatedQuery instanceof SelectFromWhere) {
					SelectFromWhere<?> sfw = (SelectFromWhere<?>) aggregatedQuery;
					ColumnExpressions<?> toReturn = new ColumnExpressions<>(sfw.cols.reader);
					for (Expression col : sfw.cols.columns) {
						SelectFromWhere<?> oneColQuery = sfw.shallowCopy();
						oneColQuery.cols = ColumnExpressions.singleColumn(SimpleRowReader.READER, col);
						toReturn.columns.add(SubqueryExpression.from(oneColQuery));
					}
					return toReturn;
				} else {
					throw new TypedValueVisitorException("Unknown subquery type");
				}
			} catch (QueryTransformException e) {
				throw new TypedValueVisitorException("Could not derive an aggregate function for a lambda", e);
			}
		} else if (sig.equals(MethodChecker.streamGetOnlyValue)) {
			SymbExPassDown passdown = SymbExPassDown.with(val, false);

			// Check out what stream we're aggregating
			SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, true, lambdaIndex);
			JinqPostgresQuery<?> subQuery = val.base.visit(translator, passdown);

			if (subQuery.isValidSubquery() && subQuery instanceof SelectFromWhere) {
				SelectFromWhere<?> sfw = (SelectFromWhere<?>) subQuery;
				ColumnExpressions<?> toReturn = new ColumnExpressions<>(sfw.cols.reader);
				for (Expression col : sfw.cols.columns) {
					SelectFromWhere<?> oneColQuery = sfw.shallowCopy();
					oneColQuery.cols = ColumnExpressions.singleColumn(SimpleRowReader.READER, col);
					toReturn.columns.add(SubqueryExpression.from(oneColQuery));
				}
				return toReturn;
			}

			throw new TypedValueVisitorException("Cannot apply getOnlyValue() to the given subquery");
		} else if ((handler = MethodChecker.jpqlFunctionMethods.get(sig)) != null) {
			return handler.handle(val, in, this);
		} else if (sig.equals(TransformationClassAnalyzer.stringBuilderToString)) {
			List<ColumnExpressions<?>> concatenatedStrings = new ArrayList<>();
			MethodCallValue.VirtualMethodCallValue baseVal = val;
			while (true) {
				if (!(baseVal.base instanceof MethodCallValue.VirtualMethodCallValue))
					throw new TypedValueVisitorException("Unexpected use of StringBuilder");
				baseVal = (MethodCallValue.VirtualMethodCallValue) baseVal.base;
				if (baseVal.getSignature().equals(TransformationClassAnalyzer.newStringBuilderString)) {
					SymbExPassDown passdown = SymbExPassDown.with(val, false);
					concatenatedStrings.add(baseVal.args.get(0).visit(this, passdown));
					break;
				} else if (baseVal.getSignature().equals(TransformationClassAnalyzer.newStringBuilder)) {
					break;
				} else if (baseVal.getSignature().equals(TransformationClassAnalyzer.stringBuilderAppendString)) {
					SymbExPassDown passdown = SymbExPassDown.with(val, false);
					concatenatedStrings.add(baseVal.args.get(0).visit(this, passdown));
				} else
					throw new TypedValueVisitorException("Unexpected use of StringBuilder");
			}

			if (concatenatedStrings.size() == 1) {
				return concatenatedStrings.get(0);
			}
			Expression head = concatenatedStrings.get(concatenatedStrings.size() - 1).getOnlyColumn();
			for (int n = concatenatedStrings.size() - 2; n >= 0; n--)
				head = new BinaryExpression(head, " || ", concatenatedStrings.get(n).getOnlyColumn());
			return ColumnExpressions.singleColumn(SimpleRowReader.READER, head);
		} else {
			try {
				Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(sig);
				// Special handling of Collection.contains() for subclasses of Collection.
				if ("contains".equals(sig.name)
						&& "(Ljava/lang/Object;)Z".equals(sig.desc)
						&& Collection.class.isAssignableFrom(reflectedMethod.getDeclaringClass())) {
					TypedValue listVal = val.base;
					TypedValue itemVal = val.args.get(0);
					return handleIsIn(val, listVal, itemVal, false);
				}
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				// Eat the error
			}
			return super.virtualMethodCallValue(val, in);
		}
	}

	@Override
	public ColumnExpressions<?> staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException {
		MethodSignature sig = val.getSignature();
		MethodHandlerStatic handler;
		if (sig.equals(TransformationClassAnalyzer.integerValueOf)
				|| sig.equals(TransformationClassAnalyzer.longValueOf)
				|| sig.equals(TransformationClassAnalyzer.doubleValueOf)
				|| sig.equals(TransformationClassAnalyzer.booleanValueOf)) {
			// TODO: fix this. add cast to correct type
			SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
			ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
			return base;
		} else if (sig.equals(TransformationClassAnalyzer.bigIntegerValueOfLong)) {
			throw new TypedValueVisitorException("New BigIntegers can only be created in the context of numeric promotion");
			//TODO this needs to be changed to an instance check, not static check ;(
		} else if ((handler = MethodChecker.jpqlFunctionStaticMethods.get(sig)) != null) {
			return handler.handle(val, in, this);
			//TODO: temporarly hardcode logic for static conversion
		} else if (sig.getOwnerType().equals(sig.getReturnType()) && val.args.size() == 0) {
			String[] names = sig.owner.split("/");
			return ColumnExpressions.singleColumn(SimpleRowReader.READER, new ConstantExpression("\"" + names[names.length - 2] + "\".\"" + names[names.length - 1] + "." + val.name + "\"()"));
		} else {
			return super.staticMethodCallValue(val, in);
		}
	}

	protected ColumnExpressions<?> handleIsIn(
			TypedValue parent,
			TypedValue listVal,
			TypedValue itemVal,
			boolean isExpectingStream) throws TypedValueVisitorException {
		SymbExPassDown passdown = SymbExPassDown.with(parent, false);
		ColumnExpressions<?> item = itemVal.visit(this, passdown);

		// Handle the collection part of isInList as a subquery
		SymbExToSubQuery translator = config.newSymbExToSubQuery(argHandler, isExpectingStream, lambdaIndex);
		JinqPostgresQuery<?> subQuery = listVal.visit(translator, passdown);

		if (subQuery.isValidSubquery() && subQuery instanceof SelectFromWhere) {
			SelectFromWhere<?> sfw = (SelectFromWhere<?>) subQuery;
			return ColumnExpressions.singleColumn(SimpleRowReader.READER,
					new BinaryExpression("", item.getOnlyColumn(), "= ANY(", SubqueryExpression.from(sfw), ")"));
		} else if (subQuery.isValidSubquery() && subQuery instanceof ParameterAsQuery) {
			ParameterAsQuery<?> paramQuery = (ParameterAsQuery<?>) subQuery;
			return ColumnExpressions.singleColumn(SimpleRowReader.READER,
					new BinaryExpression("", item.getOnlyColumn(), "= ANY(", paramQuery.cols.getOnlyColumn(), ")"));
		}
		throw new TypedValueVisitorException("Trying to create a query using IN but with an unhandled subquery type");
	}

	// Tracks which numeric types are considered to have more information than
	// other types.
	static Map<Type, Integer> numericPromotionPriority = new HashMap<>();

	static {
		int n = 0;
		numericPromotionPriority.put(Type.INT_TYPE, n);
		numericPromotionPriority.put(Type.getObjectType("java/lang/Integer"), n);
		n++;
		numericPromotionPriority.put(Type.LONG_TYPE, n);
		numericPromotionPriority.put(Type.getObjectType("java/lang/Long"), n);
		n++;
		numericPromotionPriority.put(Type.getObjectType("java/math/BigInteger"), n);
		n++;
		numericPromotionPriority.put(Type.getObjectType("java/math/BigDecimal"), n);
		n++;
		numericPromotionPriority.put(Type.FLOAT_TYPE, n);
		numericPromotionPriority.put(Type.getObjectType("java/lang/Float"), n);
		n++;
		numericPromotionPriority.put(Type.DOUBLE_TYPE, n);
		numericPromotionPriority.put(Type.getObjectType("java/lang/Double"), n);
		n++;
	}
}
