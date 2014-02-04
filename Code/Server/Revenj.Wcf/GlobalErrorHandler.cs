using System;
using System.Collections.ObjectModel;
using System.Diagnostics.Contracts;
using System.Net;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Web;
using NGS.Logging;
using NGS.Utility;

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
			var wfe = error as WebFaultException<string>;
			var fe = error as FaultException;
			return wfe != null && wfe.StatusCode != HttpStatusCode.InternalServerError
				|| wfe == null && fe != null;
		}

		public void ProvideFault(
			Exception error,
			MessageVersion version,
			ref Message fault)
		{
			//TODO Mono error handling
			var wfe = error as WebFaultException<string>;
			var fe = error as FaultException;
			if (wfe != null && wfe.StatusCode != HttpStatusCode.InternalServerError)
				ErrorLogger.Trace(() => wfe.Detail);
			else if (fe != null)
				ErrorLogger.Trace(() => fe.Message);
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