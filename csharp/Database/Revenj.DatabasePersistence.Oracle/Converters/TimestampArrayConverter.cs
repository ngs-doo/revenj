using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using System.Text;
using Oracle.DataAccess.Client;
using Oracle.DataAccess.Types;

namespace Revenj.DatabasePersistence.Oracle.Converters
{
	[OracleCustomTypeMapping("-DSL-.TWTZ_ARR")]
	public class TimestampArrayConverter : IOracleCustomType, IOracleCustomTypeFactory, IOracleArrayTypeFactory, INullable, IOracleTypeConverter, IOracleVarrayConverter
	{
		private readonly static string TimeZoneWithDaylightSaving;
		private readonly static string TimeZoneWithoutDaylightSaving;
		private readonly static TimeZoneInfo LocalZoneInfo;
		private readonly static TimeZone CurrentZone;

		static TimestampArrayConverter()
		{
			LocalZoneInfo = TimeZoneInfo.Local;
			CurrentZone = TimeZone.CurrentTimeZone;
			var offset = LocalZoneInfo.BaseUtcOffset;
			var sbWithout = new StringBuilder();
			if (offset.TotalSeconds >= 0)
				sbWithout.Append('+');
			sbWithout.Append(offset.Hours.ToString("00"));
			sbWithout.Append(':');
			sbWithout.Append(offset.Minutes.ToString("00"));
			//tough luck if you have seconds in timezone offset
			TimeZoneWithoutDaylightSaving = sbWithout.ToString();
			var rules = LocalZoneInfo.GetAdjustmentRules();
			if (rules.Length == 1 && rules[0].DateStart == DateTime.MinValue && rules[0].DateEnd == DateTime.MinValue)
			{
				var sbWith = new StringBuilder();
				var totalOffset = offset.Add(rules[0].DaylightDelta);
				if (totalOffset.TotalSeconds >= 0)
					sbWith.Append('+');
				sbWith.Append(totalOffset.Hours.ToString("00"));
				sbWith.Append(':');
				sbWith.Append(totalOffset.Minutes.ToString("00"));
				TimeZoneWithDaylightSaving = sbWith.ToString();
			}
		}

		[OracleArrayMappingAttribute]
		public OracleTimeStampTZ[] Value { get; set; }

		public static DateTime ToDateTime(OracleTimeStampTZ value)
		{
			if (value.IsNull)
				return DateTime.Now;
			if (value.TimeZone == "UTC" || value.TimeZone == "+00:00")
				return DateTime.SpecifyKind(value.Value, DateTimeKind.Utc);
			var utc = value.Value.Subtract(value.GetTimeZoneOffset());
			return DateTime.SpecifyKind(utc, DateTimeKind.Utc).ToLocalTime();
		}

		public static DateTime? ToNullableDateTime(OracleTimeStampTZ value)
		{
			if (value.IsNull)
				return null;
			if (value.TimeZone == "UTC" || value.TimeZone == "+00:00")
				return DateTime.SpecifyKind(value.Value, DateTimeKind.Utc);
			var utc = value.Value.Subtract(value.GetTimeZoneOffset());
			return DateTime.SpecifyKind(utc, DateTimeKind.Utc).ToLocalTime();
		}

		public static OracleTimeStampTZ ToOracleTimestamp(DateTime value)
		{
			if (value.Kind == DateTimeKind.Utc)
				return new OracleTimeStampTZ(value, "UTC");
			if (TimeZoneWithDaylightSaving == null)
				return new OracleTimeStampTZ(value, CurrentZone.GetUtcOffset(value).ToString());
			else if (LocalZoneInfo.IsDaylightSavingTime(value))
				return new OracleTimeStampTZ(value, TimeZoneWithDaylightSaving);
			return new OracleTimeStampTZ(value, TimeZoneWithoutDaylightSaving);
		}

		public static OracleTimeStampTZ ToOracleTimestamp(DateTime? value)
		{
			if (value == null)
				return OracleTimeStampTZ.Null;
			return ToOracleTimestamp(value.Value);
		}

		public void FromCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (Value != null)
				OracleUdt.SetValue(con, pUdt, 0, Value);
		}

		public void ToCustomObject(OracleConnection con, IntPtr pUdt)
		{
			if (!OracleUdt.IsDBNull(con, pUdt, 0))
				Value = (OracleTimeStampTZ[])OracleUdt.GetValue(con, pUdt, 0);
		}

		public IOracleCustomType CreateObject()
		{
			return new TimestampArrayConverter();
		}

		public Array CreateArray(int numElems)
		{
			return new OracleTimeStampTZ[numElems];
		}

		public Array CreateStatusArray(int numElems)
		{
			return new OracleTimeStampTZ[numElems];
		}

		public static TimestampArrayConverter Create(IEnumerable<DateTime> collection)
		{
			return new TimestampArrayConverter
			{
				Value = collection != null ? collection.Select(it => ToOracleTimestamp(it)).ToArray() : null
			};
		}

		public static TimestampArrayConverter Create(IEnumerable<DateTime?> collection)
		{
			return new TimestampArrayConverter
			{
				Value = collection != null ? collection.Select(it => ToOracleTimestamp(it)).ToArray() : null
			};
		}

		public DateTime[] ToArray()
		{
			return Value != null ? Value.Select(it => ToDateTime(it)).ToArray() : null;
		}
		public DateTime?[] ToArrayNullable()
		{
			return Value != null ? Value.Select(it => ToNullableDateTime(it)).ToArray() : null;
		}

		public List<DateTime> ToList()
		{
			return Value != null ? new List<DateTime>(Value.Select(it => ToDateTime(it))) : null;
		}
		public List<DateTime?> ToListNullable()
		{
			return Value != null ? new List<DateTime?>(Value.Select(it => ToNullableDateTime(it))) : null;
		}

		public HashSet<DateTime> ToSet()
		{
			return Value != null ? new HashSet<DateTime>(Value.Select(it => ToDateTime(it))) : null;
		}
		public HashSet<DateTime?> ToSetNullable()
		{
			return Value != null ? new HashSet<DateTime?>(Value.Select(it => ToNullableDateTime(it))) : null;
		}

		public bool IsNull { get { return Value == null; } }

		public string ToString(object value)
		{
			return ToString((DateTime?)value);
		}

		public string ToString(DateTime? value)
		{
			return value != null ? "'" + value.Value.ToString("dd-MM-yyyy hh:mm:ss.ffffffK") + "'" : "null";
		}

		public string ToStringVarray(IEnumerable value)
		{
			var values = value.Cast<DateTime?>();
			return "new \"-DSL-\".TWTZ_ARR(" + string.Join(",", values.Select(it => ToString(it))) + ")";
		}

		public DbParameter ToParameter(object value)
		{
			//TODO: DateTime.MinValue results in ORA-1841: (full) year must be between -4713 and +9999, and not be 0
			//TODO: check latest drivers
			if (value is DateTime?)
				return new OracleParameter { OracleDbType = OracleDbType.TimeStampTZ, Value = ToOracleTimestamp((DateTime?)value) };
			return new OracleParameter { OracleDbType = OracleDbType.TimeStampTZ, Value = value };
		}

		public DbParameter ToParameterVarray(IEnumerable value)
		{
			return new OracleParameter { OracleDbType = OracleDbType.Array, Value = Create(value.Cast<DateTime?>()), UdtTypeName = "-DSL-.TWTZ_ARR" };
		}
	}
}
