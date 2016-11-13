using System;
using ClientTest;
using Microsoft.Phone.Controls;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace PhoneTests
{
	public partial class MainPage : PhoneApplicationPage
	{
		// Constructor
		public MainPage()
		{
			InitializeComponent();

			this.Loaded += MainPage_Loaded;
		}

		void MainPage_Loaded(object sender, System.Windows.RoutedEventArgs e)
		{
			foreach (var type in new[] { typeof(LocatorTests) })
			{
				var atTC = type.GetCustomAttributes(typeof(TestClassAttribute), false);
				if (atTC != null && atTC.Length == 1)
				{
					var tc = Activator.CreateInstance(type);
					var methods = type.GetMethods();
					int passed = 0;
					int total = 0;
					foreach (var m in methods)
					{
						var atTM = m.GetCustomAttributes(typeof(TestMethodAttribute), false);
						if (atTM != null && atTM.Length == 1)
						{
							total++;
							try
							{
								m.Invoke(tc, null);
								passed++;
							}
							catch (Exception ex)
							{
								System.Diagnostics.Debug.WriteLine(ex.Message);
							}
						}
					}
					if (passed < total && total > 0)
						lbTests.Items.Add(type.FullName + ": FAILED (" + passed + " of " + total + ")");
					else if (total > 0)
						lbTests.Items.Add(type.FullName + ": OK (" + passed + ")");
				}
			}
		}
	}
}