using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	//TODO does not work
	[OracleCustomTypeMapping("-DSL-.GUID_ARR")]
	public class GuidArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
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
			return new GuidArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new byte[numElems][];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new OracleUdtStatus[numElems];
		}

		public static GuidArrayConverter Create(IEnumerable<Guid> collection)
		{
			return new GuidArrayConverter { Value = collection != null ? collection.Select(it => it.ToByteArray()).ToArray() : null };
		}

		public static GuidArrayConverter Create(IEnumerable<Guid?> collection)
		{
			return new GuidArrayConverter { Value = collection != null ? collection.Select(it => it != null ? it.Value.ToByteArray() : null).ToArray() : null };
		}

		public Guid[] ToArray() { return Value != null ? Value.Select(it => it != null ? new Guid(it) : Guid.Empty).ToArray() : null; }
		public Guid?[] ToArrayNullable() { return Value != null ? Value.Select(it => it != null ? (Guid?)new Guid(it) : null).ToArray() : null; }

		public List<Guid> ToList() { return Value != null ? new List<Guid>(Value.Select(it => it != null ? new Guid(it) : Guid.Empty)) : null; }
		public List<Guid?> ToListNullable() { return Value != null ? new List<Guid?>(Value.Select(it => it != null ? (Guid?)new Guid(it) : null)) : null; }

		public HashSet<Guid> ToSet() { return Value != null ? new HashSet<Guid>(Value.Select(it => it != null ? new Guid(it) : Guid.Empty)) : null; }
		public HashSet<Guid?> ToSetNullable() { return Value != null ? new HashSet<Guid?>(Value.Select(it => it != null ? (Guid?)new Guid(it) : null)) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((Guid?)value);
		}

		public string ToString(Guid? value)
		{
			return value != null ? "'" + ToOracleString(value.Value) + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<Guid?>();
			return "new \"-DSL-\".GUID_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			if (value is Guid)
				return new OracleParameter { OracleDbType = OracleDbType.Raw, Value = ((Guid)value).ToByteArray() };
			return new OracleParameter { OracleDbType = OracleDbType.Raw, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<Guid?>()), UdtTypeName = "-DSL-.GUID_ARR" };
		}

		public static string ToOracleString(Guid guid)
		{
			return BitConverter.ToString(guid.ToByteArray()).Replace("-", "");
		}

		public static Guid FromOracleString(string text)
		{
			var bytes = new byte[text.Length / 2];
			for (int i = 0; i < bytes.Length; i++)
				bytes[i] = Convert.ToByte(text.Substring(i * 2, 2), 16);
			return new Guid(bytes);
		}
	}
}
