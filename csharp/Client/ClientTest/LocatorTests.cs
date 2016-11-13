using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Revenj;
using Revenj.DomainPatterns;

namespace ClientTest
{
	[TestClass]
	public class LocatorTests
	{
		[TestMethod]
		public void CanResolveProxies()
		{
			var locator = Common.StartClient();
			Assert.IsNotNull(locator.Resolve<IApplicationProxy>());
			Assert.IsNotNull(locator.Resolve<ICrudProxy>());
			Assert.IsNotNull(locator.Resolve<IDomainProxy>());
			Assert.IsNotNull(locator.Resolve<IStandardProxy>());
			Assert.IsNotNull(locator.Resolve<IReportingProxy>());
		}

		[TestMethod]
		public void CanResolveRepositories()
		{
			var locator = Common.StartClient();
			Assert.IsNotNull(locator.Resolve<ISearchableRepository<Finance.Currency>>());
			Assert.IsNotNull(locator.Resolve<IRepository<Finance.Currency>>());
			Assert.IsNotNull(locator.Resolve<IPersistableRepository<Finance.Currency>>());
			Assert.IsNotNull(locator.Resolve<ISearchableRepository<Struct.UnrolledList>>());
			Assert.IsNotNull(locator.Resolve<IRepository<Struct.UnrolledList>>());
		}

		public class CustomObject
		{
			public readonly IRepository<Finance.Currency> repo;
			public readonly IServiceProvider locator;
			public readonly ICrudProxy proxy;

			public CustomObject(IRepository<Finance.Currency> repo, IServiceProvider locator, ICrudProxy proxy)
			{
				this.repo = repo;
				this.locator = locator;
				this.proxy = proxy;
			}
		}

		[TestMethod]
		public void CanResolveCustomObject()
		{
			var locator = Common.StartClient();
			var custom = locator.Resolve<CustomObject>();
			Assert.IsNotNull(custom);
			Assert.IsNotNull(custom.repo);
			Assert.IsNotNull(custom.locator);
			Assert.IsNotNull(custom.proxy);
			Assert.AreEqual(locator, custom.locator);
		}

		[TestMethod]
		public void WillCacheResolvedObjects()
		{
			var locator = Common.StartClient();
			var custom1 = locator.Resolve<CustomObject>();
			Assert.IsNotNull(custom1);
			var custom2 = locator.Resolve<CustomObject>();
			Assert.AreEqual(custom1, custom2);
			locator = Common.StartClient();
			var custom3 = locator.Resolve<CustomObject>();
			Assert.AreNotEqual(custom1, custom3);
		}

		public class Disposable : IDisposable
		{
			public void Dispose() { }
		}

		[TestMethod]
		public void WillNotCacheDisposableObjects()
		{
			var locator = Common.StartClient();
			var d1 = locator.Resolve<Disposable>();
			Assert.IsNotNull(d1);
			var d2 = locator.Resolve<Disposable>();
			Assert.AreNotEqual(d1, d2);
		}

		[TestMethod]
		public void CanResolveS3()
		{
			var locator = Common.StartClient();
			var s3 = locator.Resolve<S3>();
			Assert.IsNotNull(s3);
			var s3_another = locator.Resolve<S3>();
			Assert.AreNotEqual(s3, s3_another);
		}
	}
}
