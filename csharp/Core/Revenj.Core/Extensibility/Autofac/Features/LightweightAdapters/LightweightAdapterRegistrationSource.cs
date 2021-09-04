﻿// This software is part of the Autofac IoC container
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

namespace Revenj.Extensibility.Autofac.Features.LightweightAdapters
{
	class LightweightAdapterRegistrationSource : IRegistrationSource
	{
		readonly RegistrationData _registrationData;
		readonly LightweightAdapterActivatorData _activatorData;

		public LightweightAdapterRegistrationSource(
			RegistrationData registrationData,
			LightweightAdapterActivatorData activatorData)
		{
			if (registrationData == null) throw new ArgumentNullException("registrationData");
			if (activatorData == null) throw new ArgumentNullException("activatorData");

			_registrationData = registrationData;
			_activatorData = activatorData;

			if (registrationData.Services.Contains(activatorData.FromService))
				throw new ArgumentException(string.Format(
					"The service {0} cannot be both the adapter's from and to parameters - these must differ.", activatorData.FromService));
		}

		public IEnumerable<IComponentRegistration> RegistrationsFor(Service service, Func<Service, IEnumerable<IComponentRegistration>> registrationAccessor)
		{
			if (service == null) throw new ArgumentNullException("service");
			if (registrationAccessor == null) throw new ArgumentNullException("registrationAccessor");

			if (_registrationData.Services.Contains(service))
			{
				return registrationAccessor(_activatorData.FromService)
					.Select(r =>
					{
						var rb = RegistrationBuilder
							.ForDelegate((c, p) => _activatorData.Adapter(c, p, c.ResolveComponent(service, r, Enumerable.Empty<Parameter>())))
							.Targeting(r)
							.InheritRegistrationOrderFrom(r);

						rb.RegistrationData.CopyFrom(_registrationData, true);

						return rb.CreateRegistration();
					});
			}

			return new IComponentRegistration[0];
		}

		public bool IsAdapterForIndividualComponents
		{
			get { return true; }
		}

		public override string ToString()
		{
			return string.Format("Lightweight Adapter from {0} to {1}",
				_activatorData.FromService.Description,
				string.Join(", ", _registrationData.Services.Select(s => s.Description).ToArray()));
		}
	}
}
