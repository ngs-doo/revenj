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
	[OracleCustomTypeMapping("-DSL-.NUMBER_ARR")]
	public class DecimalArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public decimal?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (decimal?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public virtual IOracleCustomType CreateObject()
		{
			return new DecimalArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new decimal?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new decimal?[numElems];
		}

		public static DecimalArrayConverter Create(IEnumerable<decimal> collection)
		{
			return new DecimalArrayConverter { Value = collection != null ? collection.Select(it => (decimal?)it).ToArray() : null };
		}

		public static DecimalArrayConverter Create(IEnumerable<decimal?> collection)
		{
			return new DecimalArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public decimal[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : 0m).ToArray() : null; }
		public decimal?[] ToArrayNullable() { return Value; }

		public List<decimal> ToList() { return Value != null ? new List<decimal>(Value.Select(it => it != null ? it.Value : 0m)) : null; }
		public List<decimal?> ToListNullable() { return Value != null ? new List<decimal?>(Value) : null; }

		public HashSet<decimal> ToSet() { return Value != null ? new HashSet<decimal>(Value.Select(it => it != null ? it.Value : 0m)) : null; }
		public HashSet<decimal?> ToSetNullable() { return Value != null ? new HashSet<decimal?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((decimal?)value);
		}

		public string ToString(decimal? value)
		{
			return value != null ? value.Value.ToString(CultureInfo.InvariantCulture) : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<decimal?>();
			return "new \"-DSL-\".NUMBER_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Decimal, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<decimal?>()), UdtTypeName = "-DSL-.NUMBER_ARR" };
		}
	}
}
