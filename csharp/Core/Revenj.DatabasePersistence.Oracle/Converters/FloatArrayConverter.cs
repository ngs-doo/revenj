using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Globalization;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.FLOAT_ARR")]
	public class FloatArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public float?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (float?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new FloatArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new float?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new float?[numElems];
		}

		public static FloatArrayConverter Create(IEnumerable<float> collection)
		{
			return new FloatArrayConverter { Value = collection != null ? collection.Select(it => (float?)it).ToArray() : null };
		}

		public static FloatArrayConverter Create(IEnumerable<float?> collection)
		{
			return new FloatArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public float[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : 0).ToArray() : null; }
		public float?[] ToArrayNullable() { return Value; }

		public List<float> ToList() { return Value != null ? new List<float>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public List<float?> ToListNullable() { return Value != null ? new List<float?>(Value) : null; }

		public HashSet<float> ToSet() { return Value != null ? new HashSet<float>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public HashSet<float?> ToSetNullable() { return Value != null ? new HashSet<float?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((float?)value);
		}

		public string ToString(float? value)
		{
			return value != null ? value.Value.ToString(CultureInfo.InvariantCulture) : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<float?>();
			return "new \"-DSL-\".FLOAT_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Double, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<float?>()), UdtTypeName = "-DSL-.FLOAT_ARR" };
		}
	}
}
