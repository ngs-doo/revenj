using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.INT_ARR")]
	public class IntegerArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public int?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (int?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new IntegerArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new int?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new int?[numElems];
		}

		public static IntegerArrayConverter Create(IEnumerable<int> collection)
		{
			return new IntegerArrayConverter { Value = collection != null ? collection.Select(it => (int?)it).ToArray() : null };
		}

		public static IntegerArrayConverter Create(IEnumerable<int?> collection)
		{
			return new IntegerArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public int[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : 0).ToArray() : null; }
		public int?[] ToArrayNullable() { return Value; }

		public List<int> ToList() { return Value != null ? new List<int>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public List<int?> ToListNullable() { return Value != null ? new List<int?>(Value) : null; }

		public HashSet<int> ToSet() { return Value != null ? new HashSet<int>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public HashSet<int?> ToSetNullable() { return Value != null ? new HashSet<int?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((int?)value);
		}

		public string ToString(int? value)
		{
			return value != null ? value.Value.ToString() : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<int?>();
			return "new \"-DSL-\".INT_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Int32, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<int?>()), UdtTypeName = "-DSL-.INT_ARR" };
		}
	}
}
