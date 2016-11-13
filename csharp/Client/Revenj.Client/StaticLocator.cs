using System;

namespace Revenj
{
	public static class Static
	{
		private static IServiceProvider staticLocator;
		public static IServiceProvider Locator
		{
			get
			{
				if (staticLocator == null)
					throw new InvalidOperationException(@"Revenj C# client not initialized. 
Run Revenj.Client.Start to initialize dependencies.");
				return staticLocator;
			}
			internal set { staticLocator = value; }
		}
	}
}
