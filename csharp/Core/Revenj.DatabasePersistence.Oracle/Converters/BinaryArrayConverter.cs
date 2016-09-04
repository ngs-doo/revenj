using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	//TODO this is actually not supported in Oracle. Should convert to TABLE or do some other magic
	[OracleCustomTypeMapping("-DSL-.BLOB_ARR")]
	public class BinaryArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		[OracleArrayMappingAttribute]
		public byte[][] Value { get; set; }

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (byte[][])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new BinaryArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new byte[numElems][];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new byte[numElems][];
		}

		public static BinaryArrayConverter Create(IEnumerable<byte[]> collection)
		{
			return new BinaryArrayConverter { Value = collection != null ? collection.ToArray() : null };
		}

		public byte[][] ToArray() { return Value != null ? Value.Select(it => it != null ? it : new byte[0]).ToArray() : null; }
		public byte[][] ToArrayNullable() { return Value != null ? Value : null; }

		public List<byte[]> ToList() { return Value != null ? new List<byte[]>(Value.Select(it => it != null ? it : new byte[0])) : null; }
		public List<byte[]> ToListNullable() { return Value != null ? new List<byte[]>(Value) : null; }

		public HashSet<byte[]> ToSet() { return Value != null ? new HashSet<byte[]>(Value.Select(it => it != null ? it : new byte[0])) : null; }
		public HashSet<byte[]> ToSetNullable() { return Value != null ? new HashSet<byte[]>(Value) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((byte[])value);
		}

		public string ToString(byte[] value)
		{
			//TODO
			return null;
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<byte[]>();
			return "new \"-DSL-\".BLOB_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Blob, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<byte[]>()), UdtTypeName = "-DSL-.BLOB_ARR" };
		}
	}
}
