using System;
using System.Configuration;
using DSL;

namespace Revenj.Http
{
	static class Program
	{
		static void Main(string[] args)
		{
			foreach (var arg in args)
			{
				var i = arg.IndexOf('=');
				if (i != -1)
				{
					var name = arg.Substring(0, i);
					var value = arg.Substring(i + 1);
					ConfigurationManager.AppSettings[name] = value;
				}
			}
			var server = Platform.Start<HttpServer>();
			Console.WriteLine("Starting server");
			server.Run();
		}
	}
}
