using System.Collections.Generic;

namespace Revenj
{
	internal class Configuration
	{
		private readonly Dictionary<string, string> Settings;

		public Configuration(Dictionary<string, string> settings)
		{
			this.Settings = settings;
		}

		public string this[string key]
		{
			get
			{
				string value;
				if (Settings.TryGetValue(key, out value))
					return value;
				return null;
			}
		}
	}
}
