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
	[OracleCustomTypeMapping("-DSL-.DOUBLE_ARR")]
	public class DoubleArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public double?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (double?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new DoubleArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new double?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new double?[numElems];
		}

		public static DoubleArrayConverter Create(IEnumerable<double> collection)
		{
			return new DoubleArrayConverter { Value = collection != null ? collection.Select(it => (double?)it).ToArray() : null };
		}

		public static DoubleArrayConverter Create(IEnumerable<double?> collection)
		{
			return new DoubleArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public double[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : 0).ToArray() : null; }
		public double?[] ToArrayNullable() { return Value; }

		public List<double> ToList() { return Value != null ? new List<double>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public List<double?> ToListNullable() { return Value != null ? new List<double?>(Value) : null; }

		public HashSet<double> ToSet() { return Value != null ? new HashSet<double>(Value.Select(it => it != null ? it.Value : 0)) : null; }
		public HashSet<double?> ToSetNullable() { return Value != null ? new HashSet<double?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((double?)value);
		}

		public string ToString(double? value)
		{
			return value != null ? value.Value.ToString(CultureInfo.InvariantCulture) : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<double?>();
			return "new \"-DSL-\".DOUBLE_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Double, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<double?>()), UdtTypeName = "-DSL-.DOUBLE_ARR" };
		}
	}
}
