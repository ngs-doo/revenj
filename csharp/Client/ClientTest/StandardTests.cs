using System.Collections.Generic;
using System.Runtime.Serialization;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Test;
using System;
using Revenj;
using Revenj.DomainPatterns;

namespace ClientTest
{
	[TestClass]
	public class StandardTests
	{
		private IServiceProvider locator;
		private IStandardProxy sProxy;

		public StandardTests()
		{
			locator = Common.StartClient();
			sProxy = locator.Resolve<IStandardProxy>();
		}

		[TestMethod]
		public void CanInsertAggregateRootData()
		{
			var repo = locator.Resolve<IRepository<Finance.Currency>>();

			var newVal = new Finance.Currency { Code = "ZZZ", Name = "Kuna" }.Create();
			var XXX = new Finance.Currency { Code = "XXX", Name = "USD" };
			var USD = repo.Find("USD").Result;
			var oldUSD = USD.Clone();
			var ZZZ = repo.Find("ZZZ").Result;
			USD.Name = "United States $";

			var pair = new KeyValuePair<Finance.Currency, Finance.Currency>(oldUSD, USD);
			var uri = sProxy.Persist<Finance.Currency>(new[] { XXX }, new[] { pair }, new[] { ZZZ }).Result;
			Assert.AreEqual("XXX", uri[0]);
			Finance.Currency.Find("XXX").Delete();
		}

		[DataContract]
		public class OlapResult
		{
			[DataMember]
			public string bar;
			[DataMember]
			public int count;
			[DataMember]
			public int total;
			[DataMember]
			public decimal average;
		}

		[TestMethod]
		public void OlapCube()
		{
			var order = new Dictionary<string, bool>();
			order.Add("total", true);
			var task = sProxy.OlapCube<FooCube, OlapResult>(new[] { "bar" }, new[] { "count", "total", "average" }, order);
			var result = task.Result;
			Assert.IsTrue(result != null);
		}
	}
}
