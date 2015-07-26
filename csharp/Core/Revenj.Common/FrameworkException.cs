using System;

namespace Revenj.Common
{
	/// <summary>
	/// Generic framework exception.
	/// For internal errors:
	/// * incorrect plugin configuration
	/// * framework asserts
	/// ...
	/// </summary>
	[Serializable]
	public class FrameworkException : Exception
	{
		/// <summary>
		/// Please provide meaningful message for exception.
		/// </summary>
		public FrameworkException() { }
		/// <summary>
		/// Error with small meaningful description.
		/// </summary>
		/// <param name="message">description</param>
		public FrameworkException(string message) : base(message) { }
		/// <summary>
		/// Bubble up exception with additional description.
		/// </summary>
		/// <param name="message">description</param>
		/// <param name="inner">exception that caused this error</param>
		public FrameworkException(string message, Exception inner) : base(message, inner) { }
		/// <summary>
		/// Constructor for deserialization
		/// </summary>
		/// <param name="info">serialization info</param>
		/// <param name="context">serialization context</param>
		protected FrameworkException(
		  System.Runtime.Serialization.SerializationInfo info,
		  System.Runtime.Serialization.StreamingContext context)
			: base(info, context) { }
	}
}
