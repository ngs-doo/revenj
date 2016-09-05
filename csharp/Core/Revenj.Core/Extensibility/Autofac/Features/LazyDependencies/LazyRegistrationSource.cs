// This software is part of the Autofac IoC container
// Copyright (c) 2007 - 2008 Autofac Contributors
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
#if !WINDOWS_PHONE
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using Revenj.Extensibility.Autofac.Builder;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Util;

namespace Revenj.Extensibility.Autofac.Features.LazyDependencies
{
	/// <summary>
	/// Support the <see cref="System.Lazy{T}"/> 
	/// type automatically whenever type T is registered with the container.
	/// When a dependency of a lazy type is used, the instantiation of the underlying
	/// component will be delayed until the Value property is first accessed.
	/// </summary>
	class LazyRegistrationSource : IRegistrationSource
	{
		static Func<Service, IComponentRegistration, IComponentRegistration> CreateRegistration = CreateLazyRegistration<object>;
		static MethodInfo CreateRegistrationMethod = CreateRegistration.Method.GetGenericMethodDefinition();

		static ConcurrentDictionary<Type, Func<Service, IComponentRegistration, IComponentRegistration>> Cache =
			new ConcurrentDictionary<Type, Func<Service, IComponentRegistration, IComponentRegistration>>(1, 127);

		private static readonly ParameterExpression paramService = Expression.Parameter(typeof(Service), "providedService");
		private static readonly ParameterExpression paramCR = Expression.Parameter(typeof(IComponentRegistration), "valueRegistration");

		static Func<Service, IComponentRegistration, IComponentRegistration> GetOrCreate(Type type)
		{
			Func<Service, IComponentRegistration, IComponentRegistration> func;
			if (!Cache.TryGetValue(type, out func))
			{
				var targetType = CreateRegistrationMethod.MakeGenericMethod(type);
				var body = Expression.Call(targetType, paramService, paramCR);
				var lambda = Expression.Lambda<Func<Service, IComponentRegistration, IComponentRegistration>>(body, new[] { paramService, paramCR });
				func = lambda.Compile();
				Cache.TryAdd(type, func);
			}
			return func;
		}

		public IEnumerable<IComponentRegistration> RegistrationsFor(Service service, Func<Service, IEnumerable<IComponentRegistration>> registrationAccessor)
		{
			var swt = service as IServiceWithType;
			if (swt == null || !swt.ServiceType.IsGenericTypeDefinedBy(typeof(Lazy<>)))
				return Enumerable.Empty<IComponentRegistration>();

			var valueType = swt.ServiceType.GetGenericArguments()[0];
			var valueService = swt.ChangeType(valueType);

			var registrationCreator = GetOrCreate(valueType);

			return registrationAccessor(valueService).Select(v => registrationCreator(service, v));
		}

		public bool IsAdapterForIndividualComponents
		{
			get { return true; }
		}

		public override string ToString()
		{
			return "Lazy<T> Support";
		}

		static IComponentRegistration CreateLazyRegistration<T>(Service providedService, IComponentRegistration valueRegistration)
		{
			var rb = RegistrationBuilder.ForDelegate(
				(c, p) => new Lazy<T>(() => (T)c.ResolveComponent(providedService, valueRegistration, p)))
				.As(providedService)
				.Targeting(valueRegistration);

			return rb.CreateRegistration();
		}
	}
}
#endif