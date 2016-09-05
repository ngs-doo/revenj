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
using Revenj.Extensibility.Autofac.Features.Collections;
using Revenj.Extensibility.Autofac.Features.GeneratedFactories;
using Revenj.Extensibility.Autofac.Util;


#if !(NET35 || WINDOWS_PHONE)
using Revenj.Extensibility.Autofac.Features.LazyDependencies;


#endif

#if WINDOWS_PHONE
using Revenj.Extensibility.Autofac.Util.WindowsPhone;
#endif

namespace Revenj.Extensibility.Autofac
{
	/// <summary>
	/// Used to build an <see cref="IContainer"/> from component registrations.
	/// </summary>
	/// <example>
	/// <code>
	/// var builder = new ContainerBuilder();
	/// 
	/// builder.RegisterType&lt;Logger&gt;()
	///     .As&lt;ILogger&gt;()
	///     .SingleInstance();
	/// 
	/// builder.Register(c => new MessageHandler(c.Resolve&lt;ILogger&gt;()));
	/// 
	/// var container = builder.Build();
	/// // resolve components from container...
	/// </code>
	/// </example>
	/// <remarks>Most <see cref="ContainerBuilder"/> functionality is accessed
	/// via extension methods in <see cref="RegistrationExtensions"/>.</remarks>
	/// <seealso cref="IContainer"/>
	/// <see cref="RegistrationExtensions"/>
	public class ContainerBuilder : IObjectFactoryBuilder
	{
		private readonly List<Action<IComponentRegistry>> _configurationCallbacks = new List<Action<IComponentRegistry>>();
		private bool _wasBuilt;

		/// <summary>
		/// Register a callback that will be invoked when the container is configured.
		/// </summary>
		/// <remarks>This is primarily for extending the builder syntax.</remarks>
		/// <param name="configurationCallback">Callback to execute.</param>
		public virtual void RegisterCallback(Action<IComponentRegistry> configurationCallback)
		{
			_configurationCallbacks.Add(Enforce.ArgumentNotNull(configurationCallback, "configurationCallback"));
		}

		/// <summary>
		/// Create a new container with the component registrations that have been made.
		/// </summary>
		/// <param name="options">Options that influence the way the container is initialised.</param>
		/// <remarks>
		/// Build can only be called once per <see cref="ContainerBuilder"/>
		/// - this prevents ownership issues for provided instances.
		/// Build enables support for the relationship types that come with Autofac (e.g.
		/// Func, Owned, Meta, Lazy, IEnumerable.) To exclude support for these types,
		/// first create the container, then call Update() on the builder.
		/// </remarks>
		/// <returns>A new container with the configured component registrations.</returns>
		public IContainer Build(ContainerBuildOptions options = ContainerBuildOptions.Default)
		{
			var result = new Container();
			Build(result.ComponentRegistry, (options & ContainerBuildOptions.ExcludeDefaultModules) != ContainerBuildOptions.None);
			if ((options & ContainerBuildOptions.IgnoreStartableComponents) == ContainerBuildOptions.None)
				StartStartableComponents(result);
			return result;
		}

		static void StartStartableComponents(IComponentContext componentContext)
		{
			var ts = new TypedService(typeof(IStartable));
			foreach (var startable in componentContext.ComponentRegistry.RegistrationsFor(ts))
			{
				var instance = (IStartable)componentContext.ResolveComponent(ts, startable, Enumerable.Empty<Parameter>());
				instance.Start();
			}
		}

		internal void Update(IComponentRegistry componentRegistry)
		{
			if (componentRegistry == null) throw new ArgumentNullException("componentRegistry");
			Build(componentRegistry, true);
		}

		void Build(IComponentRegistry componentRegistry, bool excludeDefaultModules)
		{
			if (componentRegistry == null) throw new ArgumentNullException("componentRegistry");

			if (_wasBuilt)
				throw new InvalidOperationException("Build() or Update() can only be called once on a ContainerBuilder.");

			_wasBuilt = true;
			AutofacObjectFactory.RegisterToContainer(this, this, null);

			if (!excludeDefaultModules)
				RegisterDefaultAdapters(componentRegistry);

			foreach (var callback in _configurationCallbacks)
				callback(componentRegistry);
		}

		void RegisterDefaultAdapters(IComponentRegistry componentRegistry)
		{
			//this.RegisterGeneric(typeof(KeyedServiceIndex<,>)).As(typeof(IIndex<,>)).InstancePerLifetimeScope();
			componentRegistry.AddRegistrationSource(new CollectionRegistrationSource());
			//componentRegistry.AddRegistrationSource(new OwnedInstanceRegistrationSource());
			//componentRegistry.AddRegistrationSource(new MetaRegistrationSource());
#if !(NET35 || WINDOWS_PHONE)
			componentRegistry.AddRegistrationSource(new LazyRegistrationSource());
			componentRegistry.AddRegistrationSource(new LazyWithMetadataRegistrationSource());
			//componentRegistry.AddRegistrationSource(new StronglyTypedMetaRegistrationSource());
#endif
			componentRegistry.AddRegistrationSource(new GeneratedFactoryRegistrationSource());
		}

		private List<IFactoryBuilderInstance> instances = new List<IFactoryBuilderInstance>();
		private List<IFactoryBuilderType> types = new List<IFactoryBuilderType>();
		private List<IFactoryBuilderFunc> funcs = new List<IFactoryBuilderFunc>();

		public IEnumerable<IFactoryBuilderInstance> Instances { get { return instances; } }
		public IEnumerable<IFactoryBuilderType> Types { get { return types; } }
		public IEnumerable<IFactoryBuilderFunc> Funcs { get { return funcs; } }
		public void Add(IFactoryBuilderInstance item) { instances.Add(item); }
		public void Add(IFactoryBuilderType item) { types.Add(item); }
		public void Add(IFactoryBuilderFunc item) { funcs.Add(item); }
	}
}