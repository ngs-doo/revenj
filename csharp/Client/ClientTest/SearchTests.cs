using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Linq;
using Revenj;
using Revenj.DomainPatterns;

namespace ClientTest
{
	[TestClass]
	public class SearchTests
	{
		[TestMethod]
		public void CanSerializeAndDeserializeGenericSearch()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Finance.Currency>>();
			var specification = repository.Specification();
			var usd = specification.StartsWith(c => c.Code, "USD").Take(1).Search().Result[0];
			Assert.AreEqual("USD", usd.Code);
		}

		[TestMethod]
		public void CanSerializeAndDeserializeExpressionSearch()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Finance.Currency>>();
			var usd = ExpressionSpecificationHelper.Search(repository, c => c.Code == "USD", null, null).Result[0];
			Assert.AreEqual("USD", usd.Code);
		}

		[TestMethod]
		public void CanSerializeAndDeserializeSpecificationSearch()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var foo = repository.Search(new Test.Foo.searchByBar("test")).Result[0];
			Assert.IsTrue(foo.bar.StartsWith("test") && foo.num == 1337);
		}

		[TestMethod]
		public void CanSerializeAndDeserializeFindAll()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var foos = repository.FindAll().Result;
			Assert.IsTrue(foos.Any(it => it.bar.StartsWith("test") && it.num == 1337));
		}

		[TestMethod]
		public void CanSerializeAndDeserializeFind()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<IRepository<Test.Foo>>();
			var foo = repository.Find("test123").Result;
			Assert.IsTrue(foo.URI == "test123");
		}
	}
}
