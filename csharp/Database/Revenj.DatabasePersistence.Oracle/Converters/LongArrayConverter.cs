using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.LONG_ARR")]
	public class LongArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public long?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (long?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new LongArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new long?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new long?[numElems];
		}

		public static LongArrayConverter Create(IEnumerable<long> collection)
		{
			return new LongArrayConverter { Value = collection != null ? collection.Select(it => (long?)it).ToArray() : null };
		}

		public static LongArrayConverter Create(IEnumerable<long?> collection)
		{
			return new LongArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public long[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : 0).ToArray() : null; }
		public long?[] ToArrayNullable() { return Value; }

		public List<long> ToList() { return Value != null ? new List<long>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public List<long?> ToListNullable() { return Value != null ? new List<long?>(Value) : null; }

		public HashSet<long> ToSet() { return Value != null ? new HashSet<long>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public HashSet<long?> ToSetNullable() { return Value != null ? new HashSet<long?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((long?)value);
		}

		public string ToString(long? value)
		{
			return value != null ? value.Value.ToString() : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<long?>();
			return "new \"-DSL-\".LONG_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Int64, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<long?>()), UdtTypeName = "-DSL-.LONG_ARR" };
		}
	}
}
