using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.BOOL_ARR")]
	public class BoolArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
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
			return new BoolArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new string[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new string[numElems];
		}

		public static BoolArrayConverter Create(IEnumerable<bool> collection)
		{
			return new BoolArrayConverter { Value = collection != null ? collection.Select(it => it ? "Y" : "N").ToArray() : null };
		}

		public static BoolArrayConverter Create(IEnumerable<bool?> collection)
		{
			return new BoolArrayConverter { Value = collection != null ? collection.Select(it => it != null ? (it.Value ? "Y" : "N") : null).ToArray() : null };
		}

		public bool[] ToArray() { return Value != null ? Value.Select(it => it == "Y").ToArray() : null; }
		public bool?[] ToArrayNullable() { return Value != null ? Value.Select(it => it == "Y" ? (bool?)true : it != null ? (bool?)false : null).ToArray() : null; }

		public List<bool> ToList() { return Value != null ? new List<bool>(Value.Select(it => it == "Y")) : null; }
		public List<bool?> ToListNullable() { return Value != null ? new List<bool?>(Value.Select(it => it == "Y" ? (bool?)true : (it != null ? (bool?)false : null))) : null; }

		public HashSet<bool> ToSet() { return Value != null ? new HashSet<bool>(Value.Select(it => it == "Y")) : null; }
		public HashSet<bool?> ToSetNullable() { return Value != null ? new HashSet<bool?>(Value.Select(it => it == "Y" ? (bool?)true : it != null ? (bool?)false : null)) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((bool?)value);
		}

		public string ToString(bool? value)
		{
			return value == true ? "'Y'" : value == false ? "'N'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<bool?>();
			return "new \"-DSL-\".BOOL_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Char, Value = (bool?)value == true ? "Y" : (bool?)value == false ? "N" : null };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<bool?>()), UdtTypeName = "-DSL-.BOOL_ARR" };
		}
	}
}
