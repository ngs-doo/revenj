using Billing;
using Finance;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace ClientTest
{
	[TestClass]
	public class SubmitTests
	{
		[TestMethod]
		public void CallInternalEvent()
		{
			Revenj.Client.Start();
			var ev = new Currency.ChangeName { Name = "test" };
			var cur = Currency.Find("EUR");
			if (cur == null)
				cur = new Currency { Code = "EUR", Name = "Euro" }.Create();
			Assert.AreNotEqual("test", cur.Name);
			cur = ev.Submit(cur);
			Assert.AreEqual("test", cur.Name);
			cur = Currency.Find("EUR");
			Assert.AreEqual("test", cur.Name);
			cur.Name = "Euro";
			cur.Update();
		}

		[TestMethod]
		public void CallExternalEvent()
		{
			var locator = Revenj.Client.Start();
			var invoice = new Invoice().Create();
			var coffee = new Invoice.AddItem { Product = "coffee", Price = 3.14m };
			var milk = new Invoice.AddItem { Product = "milk", Price = 2.71m };

			coffee.Submit(invoice);
			milk.Submit(invoice);
		}
	}
}
