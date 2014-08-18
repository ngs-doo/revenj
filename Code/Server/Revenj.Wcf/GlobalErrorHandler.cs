using System;
using System.Collections.ObjectModel;
using System.Diagnostics.Contracts;
using System.Net;
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
			return error is UnauthorizedAccessException
				|| error is SecurityException
				|| error is FaultException
				|| error is ActionNotSupportedException;
		}

		private static readonly string MissingBasicAuth = "Basic realm=\"" + Environment.MachineName + "\"";

		public void ProvideFault(
			Exception error,
			MessageVersion version,
			ref Message fault)
		{
			var uae = error as UnauthorizedAccessException;
			var se = error as SecurityException;
			var fe = error as FaultException;
			var anse = error as ActionNotSupportedException;
			if (uae != null)
			{
				ErrorLogger.Trace(() => uae.Message);
				if (fault == null)
				{
					fault = CreateError(version, uae.Message, HttpStatusCode.Unauthorized);
					var prop = (HttpResponseMessageProperty)fault.Properties[HttpResponseMessageProperty.Name];
					prop.Headers.Add("WWW-Authenticate", MissingBasicAuth);
				}
			}
			else if (se != null)
			{
				ErrorLogger.Trace(() => se.Message);
				if (fault == null)
					fault = CreateError(version, se.Message, HttpStatusCode.Forbidden);
			}
			else if (fe != null)
			{
				ErrorLogger.Trace(() => fe.Message);
				if (fault == null)
					fault = CreateError(version, se.Message, HttpStatusCode.BadRequest);
			}
			else if (anse != null)
			{
				ErrorLogger.Trace(() => anse.Message);
				if (fault == null)
					fault = CreateError(version, se.Message, HttpStatusCode.NotFound);
			}
			else
			{
				ErrorLogger.Error(error.GetDetailedExplanation());
				if (fault == null)
					fault = CreateError(version, se.Message, HttpStatusCode.InternalServerError);
			}
		}

		private static Message CreateError(MessageVersion version, string error, HttpStatusCode status)
		{
			var fault = Message.CreateMessage(version, (string)null, (object)error);
			var prop = new HttpResponseMessageProperty();
			prop.Headers[HttpResponseHeader.ContentType] = "application/xml";// "plain/text; charset=utf-8";
			prop.StatusCode = status;
			fault.Properties.Add(HttpResponseMessageProperty.Name, prop);
			//fault.Properties.Add(WebBodyFormatMessageProperty.Name, new WebBodyFormatMessageProperty(WebContentFormat.Raw));
			return fault;
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