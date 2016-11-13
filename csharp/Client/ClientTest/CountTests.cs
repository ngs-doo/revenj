using Microsoft.VisualStudio.TestTools.UnitTesting;
using Revenj.DomainPatterns;
using Revenj;

namespace ClientTest
{
	[TestClass]
	public class CountTests
	{
		[TestMethod]
		public void CanCountGenericSpecification()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var spec = repository.Specification().StartsWith(foo => foo.bar, "test");
			Assert.IsTrue(repository.Count(spec).Result == 1);
		}

		[TestMethod]
		public void CanCountExpressionSpecification()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var N = ExpressionSpecificationHelper.Count(repository, it => it.bar.StartsWith("test")).Result;
			Assert.IsTrue(N == 1);
		}

		[TestMethod]
		public void CanCountSpecification()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var N = repository.Count(new Test.Foo.searchByBar("test")).Result;
			Assert.IsTrue(N == 1);
		}

		[TestMethod]
		public void CanCountAll()
		{
			var locator = Common.StartClient();
			var repository = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var N = repository.CountAll().Result;
			Assert.IsTrue(N == 2);
		}
	}
}
