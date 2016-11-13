using System;

namespace Microsoft.VisualStudio.TestTools.UnitTesting
{
	public class TestClassAttribute : Attribute
	{
	}

	public class TestMethodAttribute : Attribute
	{
	}

	public static class Assert
	{
		public static bool IsNotNull(object obj)
		{
			if (obj == null)
				throw new ArgumentException("argument can't be null");
			return true;
		}

		public static bool AreEqual(object objA, object objB)
		{
			if (objA != objB)
				throw new ArgumentException("arguments are not equal");
			return objA == objB;
		}

		public static bool AreNotEqual(object objA, object objB)
		{
			if (objA == objB)
				throw new ArgumentException("arguments are equal");
			return objA != objB;
		}
	}

	public static class Common
	{
		public static IServiceProvider StartClient()
		{
			return Revenj.Client.Start("https://localhost/", "test", "test");
		}
	}
}
