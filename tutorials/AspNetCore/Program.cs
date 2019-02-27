using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;

namespace AspNetTutorial
{
	public class Program
	{
		public static void Main(string[] args)
		{
			CreateWebHostBuilder(args).Build().Run();
		}

		public static IWebHostBuilder CreateWebHostBuilder(string[] args) =>
			WebHost.CreateDefaultBuilder(args)
				.UseRevenj()
					.UseRevenjServiceProvider()
					.WithCommands()
					.Configure("server=localhost;database=tutorial;user=revenj;password=revenj")
				.UseStartup<Startup>();
	}
}
