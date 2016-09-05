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
using System.Collections.Generic;
using System.Linq;
using Revenj.Extensibility.Autofac.Builder;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Util;

namespace Revenj.Extensibility.Autofac.Features.GeneratedFactories
{
	class GeneratedFactoryRegistrationSource : IRegistrationSource
	{
		/// <summary>
		/// Retrieve registrations for an unregistered service, to be used
		/// by the container.
		/// </summary>
		/// <param name="service">The service that was requested.</param>
		/// <param name="registrationAccessor">A function that will return existing registrations for a service.</param>
		/// <returns>Registrations providing the service.</returns>
		public IEnumerable<IComponentRegistration> RegistrationsFor(Service service, Func<Service, IEnumerable<IComponentRegistration>> registrationAccessor)
		{
			if (service == null) throw new ArgumentNullException("service");
			if (registrationAccessor == null) throw new ArgumentNullException("registrationAccessor");

			var ts = service as IServiceWithType;
			if (ts != null && ts.ServiceType.IsDelegate())
			{
				var resultType = ts.ServiceType.FunctionReturnType();
				var resultTypeService = ts.ChangeType(resultType);
				var hasArguments = ts.ServiceType.IsGenericType && ts.ServiceType.GetGenericArguments().Length > 1;

				return registrationAccessor(resultTypeService)
					.Where(r => r.Target != null && (hasArguments || r.Target.Sharing == InstanceSharing.None))
					.Select(r =>
					{
#if WINDOWS_PHONE
						var factory = new Util.WindowsPhone.Wp7FactoryGenerator(ts.ServiceType, r, ParameterMapping.Adaptive);
#else
						var factory = new Lazy<FactoryGenerator>(() => new FactoryGenerator(ts.ServiceType, ParameterMapping.Adaptive));
#endif
						var rb = RegistrationBuilder.ForDelegate(ts.ServiceType, (c, p) => factory.Value.GenerateFactory(resultTypeService, r.Target, c, p))
							.InstancePerLifetimeScope()
							.ExternallyOwned()
							.As(service)
							.Targeting(r);

						return rb.CreateRegistration();
					});
			}

			return Enumerable.Empty<IComponentRegistration>();
		}

		public bool IsAdapterForIndividualComponents
		{
			get { return true; }
		}

		public override string ToString()
		{
			return "Delegate Support (Func<T>and Custom Delegates)";
		}
	}
}
