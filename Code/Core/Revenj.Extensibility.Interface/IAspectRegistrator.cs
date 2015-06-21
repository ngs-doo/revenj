using System;
using System.Diagnostics.Contracts;
using System.Linq.Expressions;
using System.Reflection;

namespace Revenj.Extensibility
{
	/// <summary>
	/// AOP management service.
	/// Register aspects on join points.
	/// </summary>
	[ContractClass(typeof(AspectRegistratorContract))]
	public interface IAspectRegistrator
	{
		/// <summary>
		/// Register aspect which will be invoked before type construction.
		/// </summary>
		/// <param name="type">target class/interface</param>
		/// <param name="before">advice which will be invoked</param>
		void Before(Type type, Action before);
		/// <summary>
		/// Register aspect which will be invoked after type construction.
		/// Constructed instance will be provided to the advice.
		/// </summary>
		/// <param name="type">target class/interface</param>
		/// <param name="after">advice which will be invoked</param>
		void After(Type type, Action<object> after);
		/// <summary>
		/// Register aspect which will be invoked before a method call on specified type.
		/// Instance and arguments will be provided to the advice.
		/// </summary>
		/// <param name="type">target class/interface</param>
		/// <param name="method">target method</param>
		/// <param name="before">advice which will be invoked</param>
		void Before(Type type, MethodInfo method, Action<object, object[]> before);
		/// <summary>
		/// Register aspect which will be invoked around a method call on specified type.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// Instance and arguments will be provided.
		/// Advice is expected to return the result (null for void methods).
		/// </summary>
		/// <param name="type">target class/interface</param>
		/// <param name="method">target method</param>
		/// <param name="around">advice which will be invoked</param>
		void Around(Type type, MethodInfo method, Func<object, object[], Func<object[], object>, object> around);
		/// <summary>
		/// Register aspect which will be invoked after method call on specified type.
		/// Instance, arguments and result will be provided to the advice.
		/// Advice can modify provided result.
		/// </summary>
		/// <param name="type">target class/interface</param>
		/// <param name="method">target method</param>
		/// <param name="after">advice which will be invoked</param>
		void After(Type type, MethodInfo method, Func<object, object[], object, object> after);
	}

	[ContractClassFor(typeof(IAspectRegistrator))]
	internal sealed class AspectRegistratorContract : IAspectRegistrator
	{
		public void Before(Type type, Action before)
		{
			Contract.Requires(type != null);
			Contract.Requires(before != null);
		}
		public void After(Type type, Action<object> after)
		{
			Contract.Requires(type != null);
			Contract.Requires(after != null);
		}
		public void Before(Type type, MethodInfo method, Action<object, object[]> before)
		{
			Contract.Requires(type != null);
			Contract.Requires(method != null);
			Contract.Requires(before != null);
		}
		public void Around(Type type, MethodInfo method, Func<object, object[], Func<object[], object>, object> around)
		{
			Contract.Requires(type != null);
			Contract.Requires(method != null);
			Contract.Requires(around != null);
		}
		public void After(Type type, MethodInfo method, Func<object, object[], object, object> after)
		{
			Contract.Requires(type != null);
			Contract.Requires(method != null);
			Contract.Requires(after != null);
		}
	}

	/// <summary>
	/// Type-safe helper for aspect registration.
	/// </summary>
	public static class AspectRepositoryHelper
	{
		/// <summary>
		/// Register aspect which will be invoked before type construction.
		/// </summary>
		/// <typeparam name="T">target type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before<T>(
			this IAspectRegistrator repository,
			Action action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(action != null);

			repository.Before(typeof(T), action);
		}
		/// <summary>
		/// Register aspect which will be invoked after type construction.
		/// Created instance will be provided to the advice.
		/// </summary>
		/// <typeparam name="T">target type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<T>(
			this IAspectRegistrator repository,
			Action<T> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(action != null);

			repository.After(typeof(T), a => action((T)a));
		}
		/// <summary>
		/// Register aspect which will be invoked before a method call on specified type.
		/// Method call is specified with lambda expression.
		/// Advice is invoked with target instance and method arguments.
		/// </summary>
		/// <param name="repository">aspect management service</param>
		/// <param name="type">target class/interface</param>
		/// <param name="lambda">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before(
			this IAspectRegistrator repository,
			Type type,
			LambdaExpression lambda,
			Action<object, object[]> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(lambda != null);
			Contract.Requires(action != null);

			var me = lambda.Body as MethodCallExpression;
			repository.Before(type, me.Method, action);
		}
		/// <summary>
		/// Register aspect which will be invoked around a method call on specified type.
		/// Method call is defined with lambda expression.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// Instance and arguments will be provided.
		/// Advice is expected to return the result (null for void methods).
		/// </summary>
		/// <param name="repository">aspect management service</param>
		/// <param name="type">target class/interface</param>
		/// <param name="lambda">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around(
			this IAspectRegistrator repository,
			Type type,
			LambdaExpression lambda,
			Func<object, object[], Func<object[], object>, object> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(lambda != null);
			Contract.Requires(func != null);

			var me = lambda.Body as MethodCallExpression;
			repository.Around(type, me.Method, func);
		}
		/// <summary>
		/// Register aspect which will be invoked after method call on specified type.
		/// Method call is defined with lambda expression.
		/// Advice will be provided with target instance, arguments and result.
		/// Advice can return alternative result.
		/// </summary>
		/// <param name="repository">aspect management service</param>
		/// <param name="type">target class/interface</param>
		/// <param name="lambda">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After(
			this IAspectRegistrator repository,
			Type type,
			LambdaExpression lambda,
			Func<object, object[], object, object> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(lambda != null);
			Contract.Requires(func != null);

			var me = lambda.Body as MethodCallExpression;
			repository.After(type, me.Method, func);
		}
		/// <summary>
		/// Register aspect which will be invoked before a method call without argument on specified type.
		/// Method call is specified with expression.
		/// Advice is invoked with target instance.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before<TWhere>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Before(typeof(TWhere), expression, (w, args) => action((TWhere)w));
		}
		/// <summary>
		/// Register aspect which will be invoked before a method call with single argument on specified type.
		/// Method call is specified with expression.
		/// Advice is invoked with target instance and provided argument.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">method argument</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before<TWhere, TArg>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Before(typeof(TWhere), expression, (w, args) => action((TWhere)w, (TArg)args[0]));
		}
		/// <summary>
		/// Register aspect which will be invoked before a method call with two arguments on specified type.
		/// Method call is specified with expression.
		/// Advice is invoked with target instance and provided arguments.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">first method argument</typeparam>
		/// <typeparam name="TArg2">second method argument</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before<TWhere, TArg1, TArg2>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg1, TArg2> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Before(typeof(TWhere), expression, (w, args) => action((TWhere)w, (TArg1)args[0], (TArg2)args[1]));
		}
		/// <summary>
		/// Register aspect which will be invoked before a function without arguments on specified type.
		/// Function is specified with expression.
		/// Advice is invoked with target instance.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Before<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Action<TWhere> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Before(typeof(TWhere), expression, (w, _) => action((TWhere)w));
		}
		/// <summary>
		/// Register aspect which will be invoked before a function with single argument on specified type.
		/// Function is specified with expression.
		/// Advice is invoked with target instance and provided argument.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">function argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Before<TWhere, TArg, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Before(typeof(TWhere), expression, (w, args) => func((TWhere)w, (TArg)args[0]));
		}
		/// <summary>
		/// Register aspect which will be invoked before a function with two arguments on specified type.
		/// Function is specified with expression.
		/// Advice is invoked with target instance and provided arguments.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Before<TWhere, TArg1, TArg2, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg1, TArg2, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Before(typeof(TWhere), expression, (w, args) => func((TWhere)w, (TArg1)args[0], (TArg2)args[1]));
		}
		/// <summary>
		/// Register aspect which will be invoked around a method call on specified type.
		/// Method call is defined with expression.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<Action> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(typeof(TWhere), expression, (w, args, baseAction) => { action(() => baseAction(args)); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked around a method call on specified type.
		/// Method call is defined with expression.
		/// Target instance will be provided to the advice.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(typeof(TWhere), expression, (w, args, baseAction) => { action((TWhere)w); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked on a method call with single argument on specified type.
		/// Method call is defined with expression.
		/// Target instance and argument will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">target argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere, TArg>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TArg, Action<TArg>> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseAction) =>
				{
					action((TArg)args[0], a => baseAction(new object[] { a }));
					return null;
				});
		}
		/// <summary>
		/// Register aspect which will be invoked on a method call with single argument on specified type.
		/// Method call is defined with expression.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// Target instance, argument and base method call will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">method call argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere, TArg>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg, Action<TArg>> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseAction) =>
				{
					action((TWhere)w, (TArg)args[0], a => baseAction(new object[] { a }));
					return null;
				});
		}
		/// <summary>
		/// Register aspect which will be invoked on a method call with two arguments on specified type.
		/// Method call is defined with expression.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// Arguments and base method call will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">first method call argument type</typeparam>
		/// <typeparam name="TArg2">second method call argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TArg1, TArg2, Action<TArg1, TArg2>> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseAction) =>
				{
					action((TArg1)args[0], (TArg2)args[1], (a1, a2) => baseAction(new object[] { a1, a2 }));
					return null;
				});
		}
		/// <summary>
		/// Register aspect which will be invoked on a method call with two arguments on specified type.
		/// Method call is defined with expression.
		/// Base method call will be provided so advice can choose to invoke it or ignore it.
		/// Target instance, arguments and base method call will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">first method call argument type</typeparam>
		/// <typeparam name="TArg2">second method call argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg1, TArg2, Action<TArg1, TArg2>> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseAction) =>
				{
					action((TWhere)w, (TArg1)args[0], (TArg2)args[1], (a1, a2) => baseAction(new object[] { a1, a2 }));
					return null;
				});
		}
		/// <summary>
		/// Register aspect which will be invoked on a function without arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<Func<TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(typeof(TWhere), expression, (w, args, baseFunc) => func(() => (TResult)baseFunc(args)));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function without arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Target instance and base function will be provided to the advice.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, Func<TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(typeof(TWhere), expression, (w, args, baseFunc) => func((TWhere)w, () => (TResult)baseFunc(args)));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with single argument on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Argument and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">function argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TArg, Func<TArg, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TArg)args[0], a => (TResult)baseFunc(new object[] { a })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with single argument on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Target instance, argument and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">function argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg, Func<TArg, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TWhere)w, (TArg)args[0], a => (TResult)baseFunc(new object[] { a })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with two arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TArg1, TArg2, Func<TArg1, TArg2, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TArg1)args[0], (TArg2)args[1], (a1, a2) => (TResult)baseFunc(new object[] { a1, a2 })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with two arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Target instance, arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg1, TArg2, Func<TArg1, TArg2, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TWhere)w, (TArg1)args[0], (TArg2)args[1], (a1, a2) => (TResult)baseFunc(new object[] { a1, a2 })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with three arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TArg3">function third argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TArg3, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TArg1, TArg2, TArg3, Func<TArg1, TArg2, TArg3, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TArg1)args[0], (TArg2)args[1], (TArg3)args[2], (a1, a2, a3) => (TResult)baseFunc(new object[] { a1, a2, a3 })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with three arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Target instance, arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TArg3">function third argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TArg3, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg1, TArg2, TArg3, Func<TArg1, TArg2, TArg3, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TWhere)w, (TArg1)args[0], (TArg2)args[1], (TArg3)args[2], (a1, a2, a3) => (TResult)baseFunc(new object[] { a1, a2, a3 })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with four arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TArg3">function third argument type</typeparam>
		/// <typeparam name="TArg4">function fourth argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TArg3, TArg4, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TArg1, TArg2, TArg3, TArg4, Func<TArg1, TArg2, TArg3, TArg4, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TArg1)args[0], (TArg2)args[1], (TArg3)args[2], (TArg4)args[3], (a1, a2, a3, a4) => (TResult)baseFunc(new object[] { a1, a2, a3, a4 })));
		}
		/// <summary>
		/// Register aspect which will be invoked on a function with four arguments on specified type.
		/// Function is defined with expression.
		/// Base function be provided so advice can choose to invoke it or ignore it.
		/// Target instance, arguments and base function will be provided to the aspect.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TArg3">function third argument type</typeparam>
		/// <typeparam name="TArg4">function fourth argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void Around<TWhere, TArg1, TArg2, TArg3, TArg4, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TArg1, TArg2, TArg3, TArg4, Func<TArg1, TArg2, TArg3, TArg4, TResult>, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.Around(
				typeof(TWhere),
				expression,
				(w, args, baseFunc) => func((TWhere)w, (TArg1)args[0], (TArg2)args[1], (TArg3)args[2], (TArg4)args[3], (a1, a2, a3, a4) => (TResult)baseFunc(new object[] { a1, a2, a3, a4 })));
		}
		/// <summary>
		/// Register aspect which will be invoked after method call on specified type.
		/// Method call is defined with expression.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action(); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after method call on specified type.
		/// Method call is defined with expression.
		/// Advice will be provided with target instance.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action((TWhere)w); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after method call with one argument on specified type.
		/// Method call is defined with expression.
		/// Advice will be provided with argument.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">method argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere, TArg>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TArg> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action((TArg)args[0]); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after method call with one argument on specified type.
		/// Method call is defined with expression.
		/// Advice will be provided with target instance and argument.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">method argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere, TArg>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action((TWhere)w, (TArg)args[0]); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after method call with two arguments on specified type.
		/// Method call is defined with expression.
		/// Arguments will be provided to the advice.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">method argument type</typeparam>
		/// <typeparam name="TArg2">method argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere, TArg1, TArg2>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TArg1, TArg2> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action((TArg1)args[0], (TArg2)args[1]); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after method call with two arguments on specified type.
		/// Method call is defined with expression.
		/// Target instance and arguments will be provided to the advice.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">method argument type</typeparam>
		/// <typeparam name="TArg2">method argument type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere, TArg1, TArg2>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Action<TWhere, TArg1, TArg2> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => { action((TWhere)w, (TArg1)args[0], (TArg2)args[1]); return null; });
		}
		/// <summary>
		/// Register aspect which will be invoked after function without arguments on specified type.
		/// Function is defined with expression.
		/// Advice should return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => func());
		}
		/// <summary>
		/// Register aspect which will be invoked after function without arguments on specified type.
		/// Function is defined with expression.
		/// Target instance will be provided to the advice.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Func<TWhere, TResult>> expression,
			Func<TWhere, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, _) => func((TWhere)w));
		}
		/// <summary>
		/// Register aspect which will be invoked after function without arguments on specified type.
		/// Function is defined with expression.
		/// Result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TResult, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => func((TResult)res));
		}
		/// <summary>
		/// Register aspect which will be invoked after function without arguments on specified type.
		/// Function is defined with expression.
		/// Target instance and result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TWhere, TResult, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => func((TWhere)w, (TResult)res));
		}
		/// <summary>
		/// Register aspect which will be invoked after function with one argument on specified type.
		/// Function is defined with expression.
		/// Argument and result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">function argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="action">advice which will be applied</param>
		public static void After<TWhere, TArg, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TArg, TResult, TResult> action)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(action != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => action((TArg)args[0], (TResult)res));
		}
		/// <summary>
		/// Register aspect which will be invoked after function with one argument on specified type.
		/// Function is defined with expression.
		/// Target instance, argument and result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg">function argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TArg, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TWhere, TArg, TResult, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => func((TWhere)w, (TArg)args[0], (TResult)res));
		}
		/// <summary>
		/// Register aspect which will be invoked after function with two arguments on specified type.
		/// Function is defined with expression.
		/// Arguments and result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TArg1, TArg2, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TArg1, TArg2, TResult, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => func((TArg1)args[0], (TArg2)args[1], (TResult)res));
		}
		/// <summary>
		/// Register aspect which will be invoked after function with two arguments on specified type.
		/// Function is defined with expression.
		/// Target instance, arguments and result will be provided to the advice.
		/// Advice can return alternative result.
		/// </summary>
		/// <typeparam name="TWhere">target class/interface</typeparam>
		/// <typeparam name="TArg1">function first argument type</typeparam>
		/// <typeparam name="TArg2">function second argument type</typeparam>
		/// <typeparam name="TResult">function result type</typeparam>
		/// <param name="repository">aspect management service</param>
		/// <param name="expression">join point</param>
		/// <param name="func">advice which will be applied</param>
		public static void After<TWhere, TArg1, TArg2, TResult>(
			this IAspectRegistrator repository,
			Expression<Action<TWhere>> expression,
			Func<TWhere, TArg1, TArg2, TResult, TResult> func)
		{
			Contract.Requires(repository != null);
			Contract.Requires(expression != null);
			Contract.Requires(func != null);

			repository.After(typeof(TWhere), expression, (w, args, res) => func((TWhere)w, (TArg1)args[0], (TArg2)args[1], (TResult)res));
		}
	}
}
