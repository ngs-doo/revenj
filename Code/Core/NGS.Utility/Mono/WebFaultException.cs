#if MONO
using System.Net;

namespace System.ServiceModel.Web
{
	[Serializable]
	public class WebFaultException<T> : Exception
	{
		public T Detail { get; private set; }
		public HttpStatusCode StatusCode { get; private set; }

		public WebFaultException(T message, HttpStatusCode code)
			: base(message.ToString())
		{
			Detail = message;
			this.StatusCode = code;
		}
		protected WebFaultException(
		  System.Runtime.Serialization.SerializationInfo info,
		  System.Runtime.Serialization.StreamingContext context)
			: base(info, context) { }
	}
}
#endif