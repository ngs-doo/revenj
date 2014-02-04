using System;
using DSL;

namespace Revenj.Http
{
	static class Program
	{
		static void Main(string[] args)
		{
			var server = Platform.Start<HttpServer>();
			Console.WriteLine("Starting server");
			server.Run();
		}
	}
}
