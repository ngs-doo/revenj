using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	//TODO merge with string array converter
	[OracleCustomTypeMapping("-DSL-.TEXT_ARR")]
	public class TextArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
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

		public virtual IOracleCustomType CreateObject()
		{
			return new TextArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new string[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new string[numElems];
		}

		public static TextArrayConverter Create(IEnumerable<string> collection)
		{
			return new TextArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public string[] ToArray() { return Value != null ? Value.Select(it => it != null ? it : string.Empty).ToArray() : null; }
		public string[] ToArrayNullable() { return Value; }

		public List<string> ToList() { return Value != null ? new List<string>(Value.Select(it => it != null ? it : string.Empty)) : null; }
		public List<string> ToListNullable() { return Value != null ? new List<string>(Value) : null; }

		public HashSet<string> ToSet() { return Value != null ? new HashSet<string>(Value.Select(it => it != null ? it : string.Empty)) : null; }
		public HashSet<string> ToSetNullable() { return Value != null ? new HashSet<string>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString(value as string);
		}

		public string ToString(string value)
		{
			return !string.IsNullOrEmpty(value) ? "'" + value.ToString().Replace("'", "''") + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<string>();
			return "new \"-DSL-\".TEXT_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			var str = value as string;
			return str != null && str.Length > 2000
				? new OracleParameter { OracleDbType = OracleDbType.Clob, Value = value }
				: new OracleParameter { OracleDbType = OracleDbType.Varchar2, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<string>()), UdtTypeName = "-DSL-.TEXT_ARR" };
		}
	}
}
