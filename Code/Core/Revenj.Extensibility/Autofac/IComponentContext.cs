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

using System.Collections.Generic;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Core.Registration;
using Revenj.Extensibility.Autofac.Core.Resolving;

namespace Revenj.Extensibility.Autofac
{
	/// <summary>
	/// The context in which a service can be accessed or a component's
	/// dependencies resolved. Disposal of a context will dispose any owned
	/// components.
	/// </summary>
	public interface IComponentContext
	{
		/// <summary>
		/// Associates services with the components that provide them.
		/// </summary>
		IComponentRegistry ComponentRegistry { get; }

		/// <summary>
		/// Resolve an factory of the provided registration within the context.
		/// </summary>
		/// <param name="service">Service which is trying to be resolved</param>
		/// <param name="registration">The registration.</param>
		/// <param name="parameters">Parameters for the instance.</param>
		/// <returns>
		/// The component factory.
		/// </returns>
		/// <exception cref="ComponentNotRegisteredException"/>
		/// <exception cref="Revenj.Extensibility.Autofac.Core.DependencyResolutionException"/>
		IInstanceLookup ResolveLookup(Service service, IComponentRegistration registration, IEnumerable<Parameter> parameters);
	}

	/// <summary>
	/// Helper for ResolveComponent
	/// </summary>
	public static class ComponentContextHelper
	{
		/// <summary>
		/// Resolve an instance of the provided registration within the context.
		/// </summary>
		/// <param name="context">provided context</param>
		/// <param name="service">Service which is trying to be resolved</param>
		/// <param name="registration">The registration.</param>
		/// <param name="parameters">Parameters for the instance.</param>
		/// <returns>
		/// The component instance.
		/// </returns>
		/// <exception cref="ComponentNotRegisteredException"/>
		/// <exception cref="Revenj.Extensibility.Autofac.Core.DependencyResolutionException"/>
		public static object ResolveComponent(this IComponentContext context, Service service, IComponentRegistration registration, IEnumerable<Parameter> parameters)
		{
			var lookup = context.ResolveLookup(service, registration, parameters);
			try
			{
				return lookup.Factory();
			}
			catch (DependencyResolutionException dre)
			{
				dre.Lookups.Push(lookup);
				throw;
			}
		}
	}
}


