using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using System.Net;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	//TODO does not work
	[OracleCustomTypeMapping("-DSL-.IP_ADDR_ARR")]
	public class IPAddressArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
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
			return new IPAddressArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new byte[numElems][];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new OracleUdtStatus[numElems];
		}

		public static IPAddressArrayConverter Create(IEnumerable<IPAddress> collection)
		{
			return new IPAddressArrayConverter { Value = collection != null ? collection.Select(it => it != null ? it.GetAddressBytes() : null).ToArray() : null };
		}

		public IPAddress[] ToArray() { return Value != null ? Value.Select(it => it != null ? new IPAddress(it) : IPAddress.Loopback).ToArray() : null; }
		public IPAddress[] ToArrayNullable() { return Value != null ? Value.Select(it => it != null ? new IPAddress(it) : null).ToArray() : null; }

		public List<IPAddress> ToList() { return Value != null ? new List<IPAddress>(Value.Select(it => it != null ? new IPAddress(it) : IPAddress.Loopback)) : null; }
		public List<IPAddress> ToListNullable() { return Value != null ? new List<IPAddress>(Value.Select(it => it != null ? new IPAddress(it) : null)) : null; }

		public HashSet<IPAddress> ToSet() { return Value != null ? new HashSet<IPAddress>(Value.Select(it => it != null ? new IPAddress(it) : IPAddress.Loopback)) : null; }
		public HashSet<IPAddress> ToSetNullable() { return Value != null ? new HashSet<IPAddress>(Value.Select(it => it != null ? new IPAddress(it) : null)) : null; }

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((IPAddress)value);
		}

		public string ToString(IPAddress value)
		{
			return value != null ? "'" + ToOracleString(value) + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<IPAddress>();
			return "new \"-DSL-\".IP_ADDR_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Raw, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<IPAddress>()), UdtTypeName = "-DSL-.IP_ADDR_ARR" };
		}

		public static string ToOracleString(IPAddress ip)
		{
			return BitConverter.ToString(ip.GetAddressBytes());
		}
	}
}
