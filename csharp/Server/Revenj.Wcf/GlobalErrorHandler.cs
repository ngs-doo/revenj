using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Net;
using System.Security;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using Revenj.Utility;

namespace Revenj.Wcf
{
	public class GlobalErrorHandler : IErrorHandler, IServiceBehavior
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

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
				TraceSource.TraceEvent(TraceEventType.Verbose, 5501, uae.Message);
				if (fault == null)
				{
					fault = CreateError(version, uae.Message, HttpStatusCode.Unauthorized);
					var prop = (HttpResponseMessageProperty)fault.Properties[HttpResponseMessageProperty.Name];
					prop.Headers.Add("WWW-Authenticate", MissingBasicAuth);
				}
			}
			else if (se != null)
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 5502, se.Message);
				if (fault == null)
					fault = CreateError(version, se.Message, HttpStatusCode.Forbidden);
			}
			else if (fe != null)
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 5503, fe.Message);
				if (fault == null)
					fault = CreateError(version, fe.Message, HttpStatusCode.BadRequest);
			}
			else if (anse != null)
			{
				TraceSource.TraceEvent(TraceEventType.Verbose, 5503, anse.Message);
				if (fault == null)
					fault = CreateError(version, anse.Message, HttpStatusCode.NotFound);
			}
			else
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5504, error.GetDetailedExplanation());
				if (fault == null)
					fault = CreateError(version, error.Message, HttpStatusCode.InternalServerError);
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