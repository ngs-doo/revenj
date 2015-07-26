using System;

namespace Revenj.Api
{
	/// <summary>
	/// Thread context for current HTTP request
	/// </summary>
	public static class ThreadContext
	{
		/// <summary>
		/// Input request
		/// </summary>
		[ThreadStatic]
		public static IRequestContext Request;
		/// <summary>
		/// Output response
		/// </summary>
		[ThreadStatic]
		public static IResponseContext Response;
	}
}
