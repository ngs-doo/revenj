using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.DATE_ARR")]
	public class DateArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public DateTime?[] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (DateTime?[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new DateArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new DateTime?[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new DateTime?[numElems];
		}

		public static DateArrayConverter Create(IEnumerable<DateTime> collection)
		{
			return new DateArrayConverter { Value = collection != null ? collection.Select(it => (DateTime?)it).ToArray() : null };
		}

		public static DateArrayConverter Create(IEnumerable<DateTime?> collection)
		{
			return new DateArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public DateTime[] ToArray() { return Value != null ? Value.Select(it => it != null ? it.Value : DateTime.Today).ToArray() : null; }
		public DateTime?[] ToArrayNullable() { return Value; }

		public List<DateTime> ToList() { return Value != null ? new List<DateTime>(Value.Select(it => it != null ? it.Value : DateTime.Today)) : null; }
		public List<DateTime?> ToListNullable() { return Value != null ? new List<DateTime?>(Value) : null; }

		public HashSet<DateTime> ToSet() { return Value != null ? new HashSet<DateTime>(Value.Select(it => it != null ? it.Value : DateTime.Today)) : null; }
		public HashSet<DateTime?> ToSetNullable() { return Value != null ? new HashSet<DateTime?>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((DateTime?)value);
		}

		public string ToString(DateTime? value)
		{
			return value != null ? "'" + value.Value.ToString("dd-MM-yyyy") + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<DateTime?>();
			return "new \"-DSL-\".DATE_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Date, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<DateTime?>()), UdtTypeName = "-DSL-.DATE_ARR" };
		}
	}
}
