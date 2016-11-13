using System.IO;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Revenj.DomainPatterns;
using System;
using Revenj;

namespace ClientTest
{
	[TestClass]
	public class ReportingTests
	{
		private IServiceProvider locator;
		private ITemplaterService tService;
		private IReportingProxy rProxy;

		public ReportingTests()
		{
			locator = Common.StartClient();
			tService = locator.Resolve<ITemplaterService>();
			rProxy = locator.Resolve<IReportingProxy>();
		}

		[TestMethod]
		public void Populate()
		{
			var repo = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var result = tService.Populate("test.docx", repo.FindAll().Result[0]);
			using (var fs = new FileStream("test.docx", FileMode.Create, FileAccess.Write))
			{
				result.Result.CopyTo(fs);
			}
		}

		[TestMethod]
		public void CreateReport()
		{
			var spec = new Test.Foo.searchByBar { name = "fail123" };
			var result = tService.Populate<Test.Foo>("test.docx", spec);
			using (var fs = new FileStream("test.docx", FileMode.Create, FileAccess.Write))
			{
				result.Result.CopyTo(fs);
			}
		}

		[TestMethod]
		public void CreateReportWithoutSpecification()
		{
			var result = tService.Populate<Test.Foo>("test.docx", (ISpecification<Test.Foo>)null);
			using (var fs = new FileStream("test.docx", FileMode.Create, FileAccess.Write))
			{
				result.Result.CopyTo(fs);
			}
		}

		[TestMethod]
		public void CreateReportWithExpressionSpecification()
		{
			var repo = locator.Resolve<ISearchableRepository<Test.Foo>>();
			var spec = ExpressionSpecificationHelper.Builder<Test.Foo>(repo, it => it.bar.Contains("123")).Specification;

			var result = tService.Populate<Test.Foo>("test.docx", spec);
			using (var fs = new FileStream("test.docx", FileMode.Create, FileAccess.Write))
			{
				result.Result.CopyTo(fs);
			}
		}

		// TODO
		[TestMethod]
		public void PopulateReport()
		{
			var result = rProxy.Populate(new Test.FooReport { uri = "fail123" }).Result;
			Assert.AreEqual("fail123", result.foo.URI);
		}

		[TestMethod]
		public void CreateOlapWithSpecification()
		{
			/*
			var order = new Dictionary<string, bool>();
			order.Add("bar", true);
			var OC = rProxy.OlapCube<Test.FooCube, Test.FooCube.findByBar>(new Test.FooCube.findByBar { query = "fail123" }, "test.docx", new[] { "bar", "num" }, null, order);
			var res = OC.Result;
			Assert.IsTrue(res != null);
			*/
			var cube = new Test.FooCube();
			var pdf = cube.CreatePdf(new[] { "bar" }, new[] { "count", "total", "average" });
			var fs = new FileStream("imba.docx", FileMode.Create);
			pdf.CopyTo(fs);
			fs.Close();
			var fi = new FileInfo("imba.docx");
			Assert.IsTrue(fi.Length > 0);
		}
	}
}
