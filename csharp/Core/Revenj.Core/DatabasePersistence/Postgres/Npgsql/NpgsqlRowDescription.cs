// created on 12/6/2002 at 20:29

// Npgsql.NpgsqlRowDescription.cs
//
// Author:
//	Francisco Jr. (fxjrlists@yahoo.com.br)
//
//	Copyright (C) 2002 The Npgsql Development Team
//	npgsql-general@gborg.postgresql.org
//	http://gborg.postgresql.org/project/npgsql/projdisplay.php
//
// Permission to use, copy, modify, and distribute this software and its
// documentation for any purpose, without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph and the following two paragraphs appear in all copies.
// 
// IN NO EVENT SHALL THE NPGSQL DEVELOPMENT TEAM BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
// INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
// DOCUMENTATION, EVEN IF THE NPGSQL DEVELOPMENT TEAM HAS BEEN ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
// 
// THE NPGSQL DEVELOPMENT TEAM SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS
// ON AN "AS IS" BASIS, AND THE NPGSQL DEVELOPMENT TEAM HAS NO OBLIGATIONS
// TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using Revenj.DatabasePersistence.Postgres.NpgsqlTypes;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	/// <summary>
	/// This class represents a RowDescription message sent from
	/// the PostgreSQL.
	/// </summary>
	///
	internal class NpgsqlRowDescription : IServerResponseObject
	{
		private abstract class KanaWidthConverter
		{
			protected static readonly CompareInfo COMPARE_INFO = System.Globalization.CultureInfo.InvariantCulture.CompareInfo;
		}
		private sealed class KanaWidthInsensitiveComparer : KanaWidthConverter, IEqualityComparer<string>
		{
			public static readonly KanaWidthInsensitiveComparer INSTANCE = new KanaWidthInsensitiveComparer();
			private KanaWidthInsensitiveComparer() { }
			public bool Equals(string x, string y)
			{
				return COMPARE_INFO.Compare(x, y, CompareOptions.IgnoreWidth) == 0;
			}
			public int GetHashCode(string obj)
			{
				return COMPARE_INFO.GetSortKey(obj, CompareOptions.IgnoreWidth).GetHashCode();
			}
		}
		private sealed class KanaWidthCaseInsensitiveComparator : KanaWidthConverter, IEqualityComparer<string>
		{
			public static readonly KanaWidthCaseInsensitiveComparator INSTANCE = new KanaWidthCaseInsensitiveComparator();
			private KanaWidthCaseInsensitiveComparator() { }
			public bool Equals(string x, string y)
			{
				return COMPARE_INFO.Compare(x, y, CompareOptions.IgnoreWidth | CompareOptions.IgnoreCase) == 0;
			}
			public int GetHashCode(string obj)
			{
				return COMPARE_INFO.GetSortKey(obj, CompareOptions.IgnoreWidth | CompareOptions.IgnoreCase).GetHashCode();
			}
		}
		/// <summary>
		/// This struct represents the internal data of the RowDescription message.
		/// </summary>
		internal struct FieldData
		{
			public readonly string Name;
			public readonly int TypeOID;
			public readonly short TypeSize;
			public readonly int TypeModifier;
			public readonly int TableOID;
			public readonly short ColumnAttributeNumber;
			public readonly FormatCode FormatCode;
			public readonly NpgsqlBackendTypeInfo TypeInfo;

			public FieldData(Stream stream, NpgsqlBackendTypeMapping typeMapping, byte[] buffer, ByteBuffer queue)
			{
				Name = PGUtil.ReadString(stream, queue);
				TableOID = PGUtil.ReadInt32(stream, buffer);
				ColumnAttributeNumber = PGUtil.ReadInt16(stream, buffer);
				TypeOID = PGUtil.ReadInt32(stream, buffer);
				TypeInfo = typeMapping[TypeOID];
				TypeSize = PGUtil.ReadInt16(stream, buffer);
				TypeModifier = PGUtil.ReadInt32(stream, buffer);
				FormatCode = (FormatCode)PGUtil.ReadInt16(stream, buffer);
			}
		}

		private readonly FieldData[] fields_data;
		private Dictionary<string, int> _field_name_index_table;
		private Dictionary<string, int> _caseInsensitiveNameIndexTable;
		private readonly Version _compatVersion;

		private readonly static Version KANA_FIX_VERSION = new Version(2, 0, 2, 1);
		private readonly static Version GET_ORDINAL_THROW_EXCEPTION = KANA_FIX_VERSION;

		public NpgsqlRowDescription(Stream stream, NpgsqlBackendTypeMapping type_mapping, Version compatVersion, byte[] buffer, ByteBuffer queue)
		{
			_compatVersion = compatVersion;
			PGUtil.EatStreamBytes(stream, 4);
			var num = PGUtil.ReadInt16(stream, buffer);
			fields_data = new FieldData[num];
			for (int i = 0; i < fields_data.Length; i++)
				fields_data[i] = new FieldData(stream, type_mapping, buffer, queue);
		}

		public FieldData this[int index]
		{
			get { return fields_data[index]; }
		}

		public int NumFields
		{
			get { return (Int16)fields_data.Length; }
		}

		private void InitDictionary()
		{
			if (_field_name_index_table != null)
				return;
			if (_compatVersion < KANA_FIX_VERSION)
			{
				_field_name_index_table = new Dictionary<string, int>(fields_data.Length, StringComparer.InvariantCulture);
				_caseInsensitiveNameIndexTable = new Dictionary<string, int>(fields_data.Length, StringComparer.InvariantCultureIgnoreCase);
			}
			else
			{
				_field_name_index_table = new Dictionary<string, int>(fields_data.Length, KanaWidthInsensitiveComparer.INSTANCE);
				_caseInsensitiveNameIndexTable = new Dictionary<string, int>(fields_data.Length, KanaWidthCaseInsensitiveComparator.INSTANCE);
			}
			for (int i = 0; i < fields_data.Length; i++)
			{
				var fd = fields_data[i];
				_field_name_index_table[fd.Name] = i;
				if (!_caseInsensitiveNameIndexTable.ContainsKey(fd.Name))
					_caseInsensitiveNameIndexTable.Add(fd.Name, i);
			}
		}

		public bool HasOrdinal(string fieldName)
		{
			InitDictionary();
			return _caseInsensitiveNameIndexTable.ContainsKey(fieldName);
		}
		public int TryFieldIndex(string fieldName)
		{
			return HasOrdinal(fieldName) ? FieldIndex(fieldName) : -1;
		}
		public int FieldIndex(String fieldName)
		{
			InitDictionary();
			int ret = -1;
			if (_field_name_index_table.TryGetValue(fieldName, out ret) || _caseInsensitiveNameIndexTable.TryGetValue(fieldName, out ret))
				return ret;
			else if (_compatVersion < GET_ORDINAL_THROW_EXCEPTION)
				return -1;
			else
				throw new IndexOutOfRangeException("Field not found");
		}
	}
}
