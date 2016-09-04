using System;
using System.Collections.Generic;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.NOTIFY_INFO_TYPE")]
	public class OracleNotifyInfoConverter : IOracleCustomType, IOracleCustomTypeFactory, INullable
	{
		[OracleObjectMapping("SOURCE")]
		public string Source;
		[OracleObjectMapping("OPERATION")]
		public string Operation;
		[OracleObjectMapping("URIS")]
		public UriArrayConverter Uris;

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			OracleUdt.SetValue(con, pUdt, "SOURCE", this.Source);
			OracleUdt.SetValue(con, pUdt, "OPERATION", this.Operation);
			OracleUdt.SetValue(con, pUdt, "URIS", this.Uris);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			this.Source = (string)OracleUdt.GetValue(con, pUdt, "SOURCE");
			this.Operation = (string)OracleUdt.GetValue(con, pUdt, "OPERATION");
			this.Uris = (UriArrayConverter)OracleUdt.GetValue(con, pUdt, "URIS");
		}

		IOracleCustomType IOracleCustomTypeFactory.CreateObject()
		{
			return new OracleNotifyInfoConverter();
		}

		public static OracleNotifyInfoConverter[] Create<T>(IEnumerable<T> insert)
			where T : class, IIdentifiable
		{
			return Create<T>(insert, null, null);
		}

		public static OracleNotifyInfoConverter[] Create<T>(
			IEnumerable<T> insert,
			IEnumerable<KeyValuePair<T, T>> update,
			IEnumerable<T> delete)
			where T : class, IIdentifiable
		{
			var source = typeof(T).FullName;
			var list = new List<OracleNotifyInfoConverter>();
			if (insert != null && insert.Any())
				list.Add(new OracleNotifyInfoConverter
				{
					Source = source,
					Operation = "Insert",
					Uris = new UriArrayConverter { Value = insert.Select(it => it.URI).ToArray() }
				});
			if (update != null && update.Any())
			{
				var changedUri =
					(from u in update
					 where u.Key.URI != u.Value.URI
					 select u.Key).ToList();
				if (changedUri.Count != 0)
					list.Add(new OracleNotifyInfoConverter
					{
						Source = source,
						Operation = "Change",
						Uris = new UriArrayConverter { Value = changedUri.Select(it => it.URI).ToArray() }
					});
				list.Add(new OracleNotifyInfoConverter
				{
					Source = source,
					Operation = "Update",
					Uris = new UriArrayConverter { Value = update.Select(it => it.Value.URI).ToArray() }
				});
			}
			if (delete != null && delete.Any())
				list.Add(new OracleNotifyInfoConverter
				{
					Source = source,
					Operation = "Delete",
					Uris = new UriArrayConverter { Value = delete.Select(it => it.URI).ToArray() }
				});
			return list.ToArray();
		}

		public bool IsNull { get { return Uris == null; } }
	}
}
