using System;
using System.Collections.ObjectModel;
using System.Configuration;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Activation;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using Revenj.Api;

namespace Revenj.Wcf
{
	public class ContainerWcfHost : ServiceHostFactory
	{
		public static Func<Type, object> Resolver { get; internal set; }
		public static T Resolve<T>() { return (T)Resolver(typeof(T)); }

		private static readonly ThreadStaticServiceBehavior ContextBehavior = new ThreadStaticServiceBehavior();

		public override ServiceHostBase CreateServiceHost(string name, Uri[] baseAddresses)
		{
			var serviceType = Type.GetType(name, false);
			if (serviceType == null)
				throw new ConfigurationErrorsException("Can't find service " + name);


			ServiceHost host;
			var att = (ServiceBehaviorAttribute[])serviceType.GetCustomAttributes(typeof(ServiceBehaviorAttribute), false);
			if (att.Length == 1 && att[0].InstanceContextMode == InstanceContextMode.Single)
			{
				var instance = Resolver(serviceType);
				host = new ServiceHost(instance, baseAddresses);
			}
			else
			{
				host = new ServiceHost(serviceType, baseAddresses);
				host.Opening += (_, __) => host.Description.Behaviors.Add(new ResolveBehavior(serviceType));
			}
			host.Description.Behaviors.Add(Resolve<GlobalErrorHandler>());
			foreach (var ep in host.Description.Endpoints)
				ep.Behaviors.Add(ContextBehavior);
			return host;
		}

		class ThreadStaticServiceBehavior : IEndpointBehavior, IDispatchMessageInspector
		{
			private static readonly WcfRequest Request = new WcfRequest();
			private static readonly WcfResponse Response = new WcfResponse();

			public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
			{
				ThreadContext.Request = Request;
				ThreadContext.Response = Response;
				return null;
			}

			public void BeforeSendReply(ref Message reply, object correlationState) { }
			public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters) { }
			public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime) { }
			public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
			{
				endpointDispatcher.DispatchRuntime.MessageInspectors.Add(this);
			}
			public void Validate(ServiceEndpoint endpoint) { }
		}

		class ResolveBehavior : IServiceBehavior, IInstanceProvider
		{
			private readonly Type Target;

			public ResolveBehavior(Type target)
			{
				this.Target = target;
			}

			public void AddBindingParameters(
				ServiceDescription serviceDescription,
				ServiceHostBase serviceHostBase,
				Collection<ServiceEndpoint> endpoints,
				BindingParameterCollection bindingParameters)
			{
			}

			public void ApplyDispatchBehavior(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
			{
				var implementedContracts =
					(from ep in serviceDescription.Endpoints
					 where ep.Contract.ContractType.IsAssignableFrom(Target)
					 select ep.Contract.Name).ToArray();

				var endpointDispatchers =
					from cd in serviceHostBase.ChannelDispatchers.OfType<ChannelDispatcher>()
					from ed in cd.Endpoints
					where implementedContracts.Contains(ed.ContractName)
					select ed;

				foreach (var ed in endpointDispatchers)
					ed.DispatchRuntime.InstanceProvider = this;
			}

			public void Validate(ServiceDescription serviceDescription, ServiceHostBase serviceHostBase)
			{
			}

			public object GetInstance(InstanceContext instanceContext, Message message)
			{
				return Resolver(Target);
			}

			public object GetInstance(InstanceContext instanceContext)
			{
				return Resolver(Target);
			}

			public void ReleaseInstance(InstanceContext instanceContext, object instance)
			{
				var disp = instance as IDisposable;
				if (disp != null)
					disp.Dispose();
			}
		}
	}
}