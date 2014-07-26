using System;
using System.Collections.ObjectModel;
using System.Diagnostics.Contracts;
using System.Security;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using Revenj.Logging;
using Revenj.Utility;

namespace Revenj.Wcf
{
	public class GlobalErrorHandler : IErrorHandler, IServiceBehavior
	{
		private readonly ILogger ErrorLogger;

		public GlobalErrorHandler(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			ErrorLogger = logFactory.Create("Revenj.Wcf.Errors");
		}

		public bool HandleError(Exception error)
		{
			var se = error as SecurityException;
			var fe = error as FaultException;
			var anse = error as ActionNotSupportedException;
			return se != null || fe != null || anse != null;
		}

		public void ProvideFault(
			Exception error,
			MessageVersion version,
			ref Message fault)
		{
			var se = error as SecurityException;
			var fe = error as FaultException;
			var anse = error as ActionNotSupportedException;
			if (se != null)
				ErrorLogger.Trace(() => se.Message);
			else if (fe != null)
				ErrorLogger.Trace(() => fe.Message);
			else if (anse != null)
				ErrorLogger.Trace(() => anse.Message);
			else
				ErrorLogger.Error(error.GetDetailedExplanation());
		}

		public void AddBindingParameters(
			ServiceDescription serviceDescription,
			ServiceHostBase serviceHostBase,
			Collection<ServiceEndpoint> endpoints,
			BindingParameterCollection bindingParameters)
		{
		}

		public void ApplyDispatchBehavior(
			ServiceDescription serviceDescription,
			ServiceHostBase serviceHostBase)
		{
			foreach (ChannelDispatcher disp in serviceHostBase.ChannelDispatchers)
				disp.ErrorHandlers.Add(this);
		}

		public void Validate(
			ServiceDescription serviceDescription,
			ServiceHostBase serviceHostBase)
		{
		}
	}
}