using Revenj.AspNetCore;

namespace Microsoft.AspNetCore.Hosting
{
	public static class RevenjWebHostBuilderExtension
	{
		public static IRevenjConfig UseRevenj(this IWebHostBuilder builder)
		{
			return new RevenjConfig(builder);
		}
	}
}
