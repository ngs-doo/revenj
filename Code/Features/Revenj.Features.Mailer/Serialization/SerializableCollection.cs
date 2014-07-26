using System;
using System.Collections.Generic;
using System.Collections.Specialized;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableCollection
	{
		private readonly Dictionary<string, string> Collection = new Dictionary<string, string>();

		public SerializableCollection() { }

		public SerializableCollection(NameValueCollection coll)
		{
			foreach (string key in coll.Keys)
				Collection.Add(key, coll[key]);
		}

		public SerializableCollection(StringDictionary coll)
		{
			foreach (string key in coll.Keys)
				Collection.Add(key, coll[key]);
		}

		public void CopyTo(NameValueCollection scol)
		{
			foreach (string key in Collection.Keys)
				scol.Add(key, this.Collection[key]);
		}

		public void CopyTo(StringDictionary scol)
		{
			foreach (string key in Collection.Keys)
			{
				if (scol.ContainsKey(key))
					scol[key] = Collection[key];
				else
					scol.Add(key, Collection[key]);
			}
		}
	}
}