using Microsoft.Extensions.Hosting;
using Revenj.AspNetCore;

namespace Microsoft.AspNetCore.Hosting
{
	public static class RevenjWebHostBuilderExtension
	{
		public static IRevenjConfig UseRevenj(this IWebHostBuilder builder)
		{
			return new RevenjConfig(builder);
		}

		public static IRevenjConfig UseRevenj(this IHostBuilder builder)
		{
			return new RevenjConfig(builder);
		}
	}
}