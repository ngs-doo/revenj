﻿// This software is part of the Autofac IoC container
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using Revenj.Extensibility.Autofac.Builder;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Util;

namespace Revenj.Extensibility.Autofac.Features.Metadata
{
	/// <summary>
	/// Support the <see cref="Meta{T}"/>
	/// types automatically whenever type T is registered with the container.
	/// Metadata values come from the component registration's metadata.
	/// </summary>
	class MetaRegistrationSource : IRegistrationSource
	{
		static readonly MethodInfo CreateMetaRegistrationMethod = typeof(MetaRegistrationSource).GetMethod(
			"CreateMetaRegistration", BindingFlags.Static | BindingFlags.NonPublic);

		public IEnumerable<IComponentRegistration> RegistrationsFor(Service service, Func<Service, IEnumerable<IComponentRegistration>> registrationAccessor)
		{
			var swt = service as IServiceWithType;
			if (swt == null || !swt.ServiceType.IsGenericTypeDefinedBy(typeof(Meta<>)))
				return Enumerable.Empty<IComponentRegistration>();

			var valueType = swt.ServiceType.GetGenericArguments()[0];

			var valueService = swt.ChangeType(valueType);

			var registrationCreator = CreateMetaRegistrationMethod.MakeGenericMethod(valueType);

			return registrationAccessor(valueService)
				.Select(v => registrationCreator.Invoke(null, new object[] { service, v }))
				.Cast<IComponentRegistration>();
		}

		public bool IsAdapterForIndividualComponents
		{
			get { return true; }
		}

		public override string ToString()
		{
			return "Meta<T> Support";
		}

		static IComponentRegistration CreateMetaRegistration<T>(Service providedService, IComponentRegistration valueRegistration)
		{
			var rb = RegistrationBuilder
				.ForDelegate((c, p) => new Meta<T>(
					(T)c.ResolveComponent(providedService, valueRegistration, p),
					valueRegistration.Target.Metadata))
				.As(providedService)
				.Targeting(valueRegistration)
				.InheritRegistrationOrderFrom(valueRegistration);

			return rb.CreateRegistration();
		}
	}
}
