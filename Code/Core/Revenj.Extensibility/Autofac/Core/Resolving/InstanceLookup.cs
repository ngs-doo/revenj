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

namespace Revenj.Extensibility.Autofac.Core.Resolving
{
	// Is a component context that pins resolution to a point in the context hierarchy
	class InstanceLookup : IInstanceLookup
	{
		readonly IEnumerable<Parameter> _parameters;
		readonly IComponentRegistration _componentRegistration;
		readonly IComponentContext _scope;
		readonly ISharingLifetimeScope _activationScope;
		readonly Service _service;
		//internal readonly int ThreadID;

		public InstanceLookup(
			Service service,
			IComponentRegistration registration,
			IComponentContext scope,
			ISharingLifetimeScope mostNestedVisibleScope,
			IEnumerable<Parameter> parameters)
		{
			_service = service;
			_parameters = parameters;
			_componentRegistration = registration;
			_scope = scope;
			_activationScope = _componentRegistration.Lifetime.FindScope(mostNestedVisibleScope);
			//ThreadID = Thread.CurrentThread.ManagedThreadId;
		}

		public Service Service { get { return _service; } }

		Func<object> _factory;
		public Func<object> Factory
		{
			get
			{
				if (_factory == null)
					_factory = Prepare(Parameters);
				return _factory;
			}
		}

		public bool Preparing { get; private set; }
		public bool Activating { get; private set; }
		public event EventHandler SharedInstanceActivation;

		private Func<object> Prepare(IEnumerable<Parameter> parameters)
		{
			if (_componentRegistration.Sharing == InstanceSharing.None || parameters.Any())
				return Activate(_scope, parameters, true);
			bool firstInstance = false;
			var instance =
				_activationScope.GetOrCreateAndShare(
				_componentRegistration.Id,
				() =>
				{
					firstInstance = true;
					return Activate(_activationScope, parameters, false)();
				});
			if (firstInstance)
			{
				_componentRegistration.RaiseActivated(_service, _scope, parameters, instance);
				var ev = SharedInstanceActivation;
				if (ev != null)
					ev(instance, EventArgs.Empty);
			}
			return () => instance;
		}

		Func<object> Activate(IComponentContext scope, IEnumerable<Parameter> parameters, bool raiseActivated)
		{
			Func<object> instanceFactory;
			try
			{
				Preparing = true;
				instanceFactory = _componentRegistration.Activator.GetFactory(scope, parameters);
			}
			finally
			{
				Preparing = false;
			}

			return () =>
			{
				_componentRegistration.RaisePreparing(_service, scope, ref parameters);

				object instance;
				try
				{
					Activating = true;
					instance = instanceFactory();
				}
				finally
				{
					Activating = false;
				}

				if (_componentRegistration.Ownership == InstanceOwnership.OwnedByLifetimeScope)
				{
					var instanceAsDisposable = instance as IDisposable;
					if (instanceAsDisposable != null)
						_activationScope.Disposer.AddInstanceForDisposal(instanceAsDisposable);
				}

				_componentRegistration.RaiseActivating(_service, _scope, parameters, ref instance);

				if (raiseActivated)
					_componentRegistration.RaiseActivated(_service, _scope, parameters, instance);
				return instance;
			};
		}

		public IComponentRegistration ComponentRegistration { get { return _componentRegistration; } }

		public ILifetimeScope ActivationScope { get { return _activationScope; } }

		public IEnumerable<Parameter> Parameters { get { return _parameters; } }
	}
}
