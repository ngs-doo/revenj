using System;
using Revenj;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Revenj.DomainPatterns;

namespace ClientTest
{
	[TestClass]
	public class CrudTests
	{
		[TestMethod]
		public void CanCreateAggregateRoot()
		{
			var locator = Common.StartClient();
			var rnd = new Random();
			var root = new Finance.Currency { Code = rnd.Next(100, 999).ToString(), Name = "Euro" };
			root.Create();
			root.Delete();
		}

		[TestMethod]
		public void CanGetDomainObject()
		{
			var locator = Common.StartClient();
			var crud = locator.Resolve<ICrudProxy>();
			var obj = crud.Read<Finance.Currency>("EUR");
			Assert.IsNotNull(obj.Result);
		}

		[TestMethod]
		public void CanChangeDomainObject()
		{
			var locator = Common.StartClient();
			var crud = locator.Resolve<ICrudProxy>();
			var obj = crud.Read<Finance.Currency>("EUR").Result;
			obj.Name = "eurO";
			obj.Create();
			Assert.AreEqual(obj.Name, "eurO");
		}

		[TestMethod]
		public void CanDeleteDomainObject()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<IPersistableRepository<Finance.Currency>>();
			var newCurr = repository.Insert(new Finance.Currency { Code = "ABC", Name = "abeceda" }).Result;
			var saved = Finance.Currency.Find("ABC");
			Assert.IsNotNull(saved);
			saved.Delete();

			var spec = repository.Specification().Equal(it => it.URI, "ABC");
			Assert.AreEqual(repository.Count(spec).Result, 0);
		}
	}
}
