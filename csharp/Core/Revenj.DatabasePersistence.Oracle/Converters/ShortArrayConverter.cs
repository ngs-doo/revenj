using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.SHORT_ARR")]
	public class ShortArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public short?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (short?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new ShortArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new short?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new short?[numElems];
		}

		public static ShortArrayConverter Create(IEnumerable<short> collection)
		{
			return new ShortArrayConverter { Value = collection != null ? collection.Select(it => (short?)it).ToArray() : null };
		}

		public static ShortArrayConverter Create(IEnumerable<short?> collection)
		{
			return new ShortArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public short[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : (short)0).ToArray() : null; }
		public short?[] ToArrayNullable() { return Value; }

		public List<short> ToList() { return Value != null ? new List<short>(Value.Select(it => it != null ? it.Value : (short)0)) : null; }
		public List<short?> ToListNullable() { return Value != null ? new List<short?>(Value) : null; }

		public HashSet<short> ToSet() { return Value != null ? new HashSet<short>(Value.Select(it => it != null ? it.Value : (short)0)) : null; }
		public HashSet<short?> ToSetNullable() { return Value != null ? new HashSet<short?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((short?)value);
		}

		public string ToString(short? value)
		{
			return value != null ? value.Value.ToString() : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<short?>();
			return "new \"-DSL-\".SHORT_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Int16, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<short?>()), UdtTypeName = "-DSL-.SHORT_ARR" };
		}
	}
}
