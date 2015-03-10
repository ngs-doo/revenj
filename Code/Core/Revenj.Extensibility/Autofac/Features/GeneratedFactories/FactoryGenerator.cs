// This software is part of the Autofac IoC container
// Copyright © 2011 Autofac Contributors
// http://autofac.org
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Core.Resolving;
using Revenj.Extensibility.Autofac.Util;

namespace Revenj.Extensibility.Autofac.Features.GeneratedFactories
{
	/// <summary>
	/// Generates context-bound closures that represent factories from
	/// a set of heuristics based on delegate type signatures.
	/// </summary>
	public class FactoryGenerator
	{
		readonly Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate> _generator;

		private static ConcurrentDictionary<FuncPair, Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate>> _factoryCache =
			new ConcurrentDictionary<FuncPair, Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate>>(1, 127);

		static Dictionary<Type, MethodInfo> ServiceMethods = new Dictionary<Type, MethodInfo>();
		static Dictionary<Type, MethodInfo> LookupMethods = new Dictionary<Type, MethodInfo>();

		static MethodInfo _resolveServiceMethod = ReflectionExtensions.GetMethod<IComponentContext>(cc => cc.ResolveService(default(Service), default(Parameter[])));
		static MethodInfo _resolveLookupMethod = ReflectionExtensions.GetMethod<IComponentContext>(cc => cc.ResolveLookup(default(Service), default(IComponentRegistration), default(Parameter[])));
		static MethodInfo _invokeFunc = typeof(Func<object>).GetMethod("Invoke");
		static PropertyInfo _factoryFunc = typeof(IInstanceLookup).GetProperty("Factory");

		static FactoryGenerator()
		{
			ServiceMethods[typeof(Func<>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object>>)MakeServiceCall<object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object>>)MakeServiceCall<object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object>>)MakeServiceCall<object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object>>)MakeServiceCall<object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			ServiceMethods[typeof(Func<,,,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeServiceCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object>>)MakeLookupCall<object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object>>)MakeLookupCall<object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object>>)MakeLookupCall<object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object>>)MakeLookupCall<object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
			LookupMethods[typeof(Func<,,,,,,,,,,,,,,,,>)] = ((Func<Service, ParameterInfo[], ParameterMapping, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>>)MakeLookupCall<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object>).Method.GetGenericMethodDefinition();
		}
		/// <summary>
		/// Create a factory generator.
		/// </summary>
		/// <param name="service">The service that will be activated in
		/// order to create the products of the factory.</param>
		/// <param name="delegateType">The delegate to provide as a factory.</param>
		/// <param name="parameterMapping">The parameter mapping mode to use.</param>
		public FactoryGenerator(Type delegateType, Service service, ParameterMapping parameterMapping)
		{
			if (service == null) throw new ArgumentNullException("service");
			//Enforce.ArgumentTypeIsFunction(delegateType);
			MethodInfo typeMethod = null;
			if (delegateType.IsGenericType)
				ServiceMethods.TryGetValue(delegateType.GetGenericTypeDefinition(), out typeMethod);
			if (typeMethod != null)
			{
				_generator = CreateFuncGenerator(
					typeMethod,
					delegateType,
					GetParameterMapping(delegateType, parameterMapping));
			}
			else
			{
				_generator = CreateGenerator((activationServiceParam, activationComponentRegistration, activatorContextParam, resolveParameterArray) =>
					{
						// c, service, [new Parameter(name, (object)dps)]*
						var resolveParams = new[] {
						activatorContextParam,
						Expression.Constant(service, typeof(Service)),
						Expression.NewArrayInit(typeof(Parameter), resolveParameterArray)
					};

						// c.Resolve(...)
						return Expression.Call(_resolveServiceMethod, resolveParams);
					},
					delegateType,
					GetParameterMapping(delegateType, parameterMapping));
			}
		}

		/// <summary>
		/// Create a factory generator.
		/// </summary>
		/// <param name="delegateType">The delegate to provide as a factory.</param>
		/// <param name="parameterMapping">The parameter mapping mode to use.</param>
		public FactoryGenerator(Type delegateType, ParameterMapping parameterMapping)
		{
			//Enforce.ArgumentTypeIsFunction(delegateType);

			MethodInfo typeMethod = null;
			if (delegateType.IsGenericType)
				LookupMethods.TryGetValue(delegateType.GetGenericTypeDefinition(), out typeMethod);
			if (typeMethod != null)
			{
				_generator = CreateFuncGenerator(
					typeMethod,
					delegateType,
					GetParameterMapping(delegateType, parameterMapping));
			}
			else
			{
				_generator = CreateGenerator((activatorServiceParam, activationComponentRegistration, activatorContextParam, resolveParameterArray) =>
					{
						// productRegistration, [new Parameter(name, (object)dps)]*
						var resolveParams = new Expression[] {
							activatorServiceParam,
							activationComponentRegistration,
							Expression.NewArrayInit(typeof(Parameter), resolveParameterArray)
						};
						// c.Resolve(...)
						return
							Expression.Call(
								Expression.Property(
									Expression.Call(
										activatorContextParam,
										_resolveLookupMethod,
										resolveParams),
									_factoryFunc),
								_invokeFunc);
					},
					delegateType,
					GetParameterMapping(delegateType, parameterMapping));
			}
		}

		static ParameterMapping GetParameterMapping(Type delegateType, ParameterMapping configuredParameterMapping)
		{
			if (configuredParameterMapping == ParameterMapping.Adaptive)
			{
				var name = delegateType.Name;
				return name.Length > 5 && name[0] == 'F' && name[1] == 'u' && name[2] == 'n' && name[3] == 'c' && name[4] == '`'
					? ParameterMapping.ByType
					: ParameterMapping.ByName;
			}
			return configuredParameterMapping;
		}

		class FuncPair : IEquatable<FuncPair>
		{
			public readonly Type Type;
			public readonly ParameterMapping Mapping;
			public FuncPair(Type type, ParameterMapping mapping)
			{
				this.Type = type;
				this.Mapping = mapping;
			}

			public override int GetHashCode()
			{
				return Type.GetHashCode();
			}

			public bool Equals(FuncPair other)
			{
				return Type == other.Type
					&& Mapping == other.Mapping;
			}
		}

		static Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate> CreateFuncGenerator(
			MethodInfo methodLookup,
			Type delegateType,
			ParameterMapping pm)
		{
			var kv = new FuncPair(delegateType, pm);
			Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate> result;
			if (_factoryCache.TryGetValue(kv, out result))
				return result;

			var invoke = delegateType.GetMethod("Invoke");
			var invokeParams = invoke.GetParameters();

			var activatorServiceParam = Expression.Parameter(typeof(Service), "s");
			var activatorComponentRegistrationParam = Expression.Parameter(typeof(IComponentRegistration), "r");
			var activatorContextParam = Expression.Parameter(typeof(IComponentContext), "c");
			var activatorParamsParam = Expression.Parameter(typeof(IEnumerable<Parameter>), "p");
			var actualInfoParam = Expression.Constant(invokeParams);
			var actualPmParam = Expression.Constant(pm);
			var activatorParams = new[] { activatorServiceParam, activatorComponentRegistrationParam, activatorContextParam, activatorParamsParam };
			var actualParams = new Expression[] { activatorServiceParam, actualInfoParam, actualPmParam, activatorComponentRegistrationParam, activatorContextParam, activatorParamsParam };

			var types = invokeParams.Select(it => it.ParameterType).Concat(new[] { invoke.ReturnType }).ToArray();
			var methodCall = Expression.Call(methodLookup.MakeGenericMethod(types), actualParams);
			var activator = Expression.Lambda<Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate>>(methodCall, activatorParams);

			result = activator.Compile();
			_factoryCache.TryAdd(kv, result);
			return result;
		}

		static Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate> CreateGenerator(
			Func<Expression, Expression, Expression, Expression[], Expression> makeResolveCall,
			Type delegateType,
			ParameterMapping pm)
		{
			var kv = new FuncPair(delegateType, pm);
			Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate> result;
			if (_factoryCache.TryGetValue(kv, out result))
				return result;

			// (c, p) => ([dps]*) => (drt)Resolve(c, productRegistration, [new NamedParameter(name, (object)dps)]*)

			// (c, p)
			var activatorServiceParam = Expression.Parameter(typeof(Service), "s");
			var activatorComponentRegistrationParam = Expression.Parameter(typeof(IComponentRegistration), "r");
			var activatorContextParam = Expression.Parameter(typeof(IComponentContext), "c");
			var activatorParamsParam = Expression.Parameter(typeof(IEnumerable<Parameter>), "p");
			var activatorParams = new[] { activatorServiceParam, activatorComponentRegistrationParam, activatorContextParam, activatorParamsParam };

			var invoke = delegateType.GetMethod("Invoke");

			// [dps]*
			var creatorParams = invoke
				.GetParameters()
				.Select(pi => Expression.Parameter(pi.ParameterType, pi.Name))
				.ToList();

			var resolveParameterArray = MapParameters(creatorParams, pm);

			var resolveCall = makeResolveCall(activatorServiceParam, activatorComponentRegistrationParam, activatorContextParam, resolveParameterArray);

			// (drt)
			var resolveCast = Expression.Convert(resolveCall, invoke.ReturnType);

			// ([dps]*) => c.Resolve(service, [new Parameter(name, dps)]*)
			var creator = Expression.Lambda(delegateType, resolveCast, creatorParams);

			// (c, p) => (
			var activator = Expression.Lambda<Func<Service, IComponentRegistration, IComponentContext, IEnumerable<Parameter>, Delegate>>(creator, activatorParams);

			result = activator.Compile();
			_factoryCache.TryAdd(kv, result);
			return result;
		}

		static Expression[] MapParameters(IEnumerable<ParameterExpression> creatorParams, ParameterMapping pm)
		{
			switch (pm)
			{
				case ParameterMapping.ByType:
					return creatorParams
							.Select(p => Expression.New(
								typeof(TypedParameter).GetConstructor(new[] { typeof(Type), typeof(object) }),
								Expression.Constant(p.Type, typeof(Type)), Expression.Convert(p, typeof(object))))
							.OfType<Expression>()
							.ToArray();

				case ParameterMapping.ByPosition:
					return creatorParams
						.Select((p, i) => Expression.New(
								typeof(PositionalParameter).GetConstructor(new[] { typeof(int), typeof(object) }),
								Expression.Constant(i, typeof(int)), Expression.Convert(p, typeof(object))))
							.OfType<Expression>()
							.ToArray();

				// ReSharper disable RedundantCaseLabel
				case ParameterMapping.ByName:
				// ReSharper restore RedundantCaseLabel
				default:
					return creatorParams
							.Select(p => Expression.New(
								typeof(NamedParameter).GetConstructor(new[] { typeof(string), typeof(object) }),
								Expression.Constant(p.Name, typeof(string)), Expression.Convert(p, typeof(object))))
							.OfType<Expression>()
							.ToArray();
			}
		}

		static Parameter[] MapParameters(ParameterInfo[] info, ParameterMapping pm, params object[] args)
		{
			var res = new Parameter[info.Length];
			switch (pm)
			{
				case ParameterMapping.ByType:
					for (int i = 0; i < res.Length; i++)
						res[i] = new TypedParameter(info[i].ParameterType, args[i]);
					break;

				case ParameterMapping.ByPosition:
					for (int i = 0; i < res.Length; i++)
						res[i] = new PositionalParameter(i, args[i]);
					break;

				default:
					for (int i = 0; i < res.Length; i++)
						res[i] = new NamedParameter(info[i].Name, args[i]);
					break;
			}
			return res;
		}

		static Func<T> MakeServiceCall<T>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return () => (T)c.ResolveService(s, p);
		}
		static Func<TArg, TResult> MakeServiceCall<TArg, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return arg => (TResult)c.ResolveService(s, MapParameters(info, pm, arg));
		}
		static Func<TArg1, TArg2, TResult> MakeServiceCall<TArg1, TArg2, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2) => (TResult)c.ResolveService(s, MapParameters(info, pm, arg1, arg2));
		}
		static Func<TArg1, TArg2, TArg3, TResult> MakeServiceCall<TArg1, TArg2, TArg3, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TResult> MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15));
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TArg16, TResult>
			MakeServiceCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TArg16, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16) =>
				(TResult)c.ResolveService(
					s,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg16));
		}
		static Func<T> MakeLookupCall<T>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return () => (T)c.ResolveLookup(s, r, p).Factory();
		}
		static Func<TArg, TResult> MakeLookupCall<TArg, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return arg => (TResult)c.ResolveLookup(s, r, MapParameters(info, pm, arg)).Factory();
		}
		static Func<TArg1, TArg2, TResult> MakeLookupCall<TArg1, TArg2, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2) =>
				(TResult)c.ResolveLookup(s, r, MapParameters(info, pm, arg1, arg2)).Factory();
		}
		static Func<TArg1, TArg2, TArg3, TResult> MakeLookupCall<TArg1, TArg2, TArg3, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3) =>
				(TResult)c.ResolveLookup(s, r, MapParameters(info, pm, arg1, arg2, arg3))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TResult> MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4) =>
				(TResult)c.ResolveLookup(s, r, MapParameters(info, pm, arg1, arg2, arg3, arg4))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5) =>
				(TResult)c.ResolveLookup(s, r, MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15))
					.Factory();
		}
		static Func<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TArg16, TResult>
			MakeLookupCall<TArg1, TArg2, TArg3, TArg4, TArg5, TArg6, TArg7, TArg8, TArg9, TArg10, TArg11, TArg12, TArg13, TArg14, TArg15, TArg16, TResult>(
			Service s,
			ParameterInfo[] info,
			ParameterMapping pm,
			IComponentRegistration r,
			IComponentContext c,
			IEnumerable<Parameter> p)
		{
			return (arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16) =>
				(TResult)c.ResolveLookup(
					s,
					r,
					MapParameters(info, pm, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16))
					.Factory();
		}

		/// <summary>
		/// Generates a factory delegate that closes over the provided context.
		/// </summary>
		/// <param name="service">Service which is resolving</param>
		/// <param name="context">The context in which the factory will be used.</param>
		/// <param name="registration">Component registration</param>
		/// <param name="parameters">Parameters provided to the resolve call for the factory itself.</param>
		/// <returns>A factory delegate that will work within the context.</returns>
		public Delegate GenerateFactory(Service service, IComponentRegistration registration, IComponentContext context, IEnumerable<Parameter> parameters)
		{
			if (service == null) throw new ArgumentNullException("service");
			if (context == null) throw new ArgumentNullException("context");
			if (parameters == null) throw new ArgumentNullException("parameters");

			return _generator(service, registration, context, parameters);
		}

		/// <summary>
		/// Generates a factory delegate that closes over the provided context.
		/// </summary>
		/// <param name="service">Service which is resolving</param>
		/// <param name="registration">Component registration</param>
		/// <param name="context">The context in which the factory will be used.</param>
		/// <param name="parameters">Parameters provided to the resolve call for the factory itself.</param>
		/// <returns>A factory delegate that will work within the context.</returns>
		public TDelegate GenerateFactory<TDelegate>(Service service, IComponentRegistration registration, IComponentContext context, IEnumerable<Parameter> parameters)
			where TDelegate : class
		{
			return (TDelegate)(object)GenerateFactory(service, registration, context, parameters);
		}
	}
}
