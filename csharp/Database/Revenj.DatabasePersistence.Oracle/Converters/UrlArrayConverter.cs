using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.URL_ARR")]
	public class UrlArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public string[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (string[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new UrlArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new string[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new string[numElems];
		}

		public static UrlArrayConverter Create(IEnumerable<Uri> collection)
		{
			return new UrlArrayConverter { Value = collection != null ? collection.Select(it => it != null ? it.ToString() : null).ToArray() : null };
		}

		public Uri[] ToArray() { return Value != null ? Value.Select(it => new Uri(it)).ToArray() : null; }
		public Uri[] ToArrayNullable() { return Value != null ? Value.Select(it => it != null ? new Uri(it) : null).ToArray() : null; }

		public List<Uri> ToList() { return Value != null ? new List<Uri>(Value.Select(it => new Uri(it))) : null; }
		public List<Uri> ToListNullable() { return Value != null ? new List<Uri>(Value.Select(it => it != null ? new Uri(it) : null)) : null; }

		public HashSet<Uri> ToSet() { return Value != null ? new HashSet<Uri>(Value.Select(it => new Uri(it))) : null; }
		public HashSet<Uri> ToSetNullable() { return Value != null ? new HashSet<Uri>(Value.Select(it => it != null ? new Uri(it) : null)) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			if (value == null)
				return "null";
			return Convert(value as Uri);
		}

		public string ToString(Uri value)
		{
			return Convert(value);
		}

		private static string Convert(Uri value)
		{
			return value != null ? "'" + value.ToString().Replace("'", "''") + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<Uri>();
			return "new \"-DSL-\".URL_ARR(" + string.Join(",", values.Select(it => Convert(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Varchar2, Value = value != null ? value.ToString() : null };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<Uri>()), UdtTypeName = "-DSL-.URL_ARR" };
		}
	}
}
