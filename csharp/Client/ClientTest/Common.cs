using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Revenj;

namespace ClientTest
{
	[TestClass]
	public class Common
	{
		[DataContract(Namespace = "")]
		public class Argument
		{
			[DataMember]
			public Dictionary<string, string> Dsl;
			[DataMember]
			public string Name;
			[DataMember]
			public string[] Targets;
		}

		public static IServiceProvider StartClient()
		{
			return Revenj.Client.Start("https://localhost/", "test", "test");
		}

		[TestMethod]
		public void CanConnectToServer()
		{
			var locator = Revenj.Client.Start();
			var crud = locator.Resolve<ICrudProxy>();
			var usd = crud.Read<Finance.Currency>("USD");
			Assert.IsNotNull(usd.Result);
		}
	}
}
