// created on 13/6/2002 at 21:06

// Npgsql.NpgsqlAsciiRow.cs
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
using System.Data;
using System.IO;
using System.Text;
using Revenj.DatabasePersistence.Postgres.NpgsqlTypes;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	/// <summary>
	/// Implements <see cref="RowReader"/> for version 3 of the protocol.
	/// </summary>
	internal sealed class StringRowReader : RowReader
	{
		private readonly int _messageSize;
		private int? _nextFieldSize = null;

		public StringRowReader(NpgsqlRowDescription rowDesc, Stream inputStream, byte[] buffer, ByteBuffer bytes)
			: base(rowDesc, inputStream, buffer, bytes)
		{
			_messageSize = PGUtil.ReadInt32(inputStream, buffer);
			if (PGUtil.ReadInt16(inputStream, buffer) != rowDesc.NumFields)
			{
				throw new DataException();
			}
		}

		protected override object ReadNext()
		{
			int fieldSize = GetThisFieldCount();
			if (fieldSize >= _messageSize)
			{
				AbandonShip();
			}
			_nextFieldSize = null;

			// Check if this field is null
			if (fieldSize == -1) // Null value
			{
				return DBNull.Value;
			}

			NpgsqlRowDescription.FieldData field_descr = FieldData;

			if (fieldSize >= 32768)
				return ReadLargeObject(field_descr, fieldSize);

			try
			{
				if (field_descr.FormatCode == FormatCode.Text)
				{
					PGUtil.CheckedStreamRead(Stream, bytes.Large, 0, fieldSize);
					var str = UTF8Encoding.GetString(bytes.Large, 0, fieldSize);
					return
						NpgsqlTypesHelper.ConvertBackendStringToSystemType(
							field_descr.TypeInfo,
							str,
							field_descr.TypeSize,
							field_descr.TypeModifier);
				}
				else
				{
					var buffer = new byte[fieldSize];
					PGUtil.CheckedStreamRead(Stream, buffer, 0, fieldSize);
					return
						NpgsqlTypesHelper.ConvertBackendBytesToSystemType(
							field_descr.TypeInfo,
							buffer,
							fieldSize,
							field_descr.TypeModifier);
				}
			}
			catch (IOException)
			{
				throw;
			}
			catch (InvalidCastException ice)
			{
				return ice;
			}
			catch (Exception ex)
			{
				return new InvalidCastException(ex.Message, ex);
			}
		}

		private object ReadLargeObject(NpgsqlRowDescription.FieldData field_descr, int field_value_size)
		{
			var cms = new LargeMemoryStream(Stream, field_value_size);
			try
			{
				return
					NpgsqlTypesHelper.ConvertBackendStringToSystemType(
						field_descr.TypeInfo,
						new StreamReader(cms, Encoding.UTF8),
						field_descr.TypeSize,
						field_descr.TypeModifier);
			}
			catch (InvalidCastException ice)
			{
				return ice;
			}
			catch (Exception ex)
			{
				return new InvalidCastException(ex.Message, ex);
			}
		}

		private void AbandonShip()
		{
			//field size will always be smaller than message size
			//but if we fall out of sync with the stream due to an error then we will probably hit
			//such a situation soon as bytes from elsewhere in the stream get interpreted as a size.
			//so if we see this happens, we know we've lost the stream - our best option is to just give up on it,
			//and have the connector recovered later.
			try
			{
				Stream.WriteByte((byte)FrontEndMessageCode.Termination);
				PGUtil.WriteInt32(Stream, 4);
				Stream.Flush();
			}
			catch
			{
			}
			try
			{
				Stream.Close();
			}
			catch
			{
			}
			throw new DataException();
		}

		protected override void SkipOne()
		{
			int fieldSize = GetThisFieldCount();
			if (fieldSize >= _messageSize)
			{
				AbandonShip();
			}
			_nextFieldSize = null;
			PGUtil.EatStreamBytes(Stream, fieldSize);
		}

		public override bool IsNextDBNull
		{
			get { return GetThisFieldCount() == -1; }
		}

		private int GetThisFieldCount()
		{
			return (_nextFieldSize = _nextFieldSize ?? PGUtil.ReadInt32(Stream, buffer)).Value;
		}

		protected override int GetNextFieldCount()
		{
			int ret = GetThisFieldCount();
			_nextFieldSize = null;
			return ret;
		}
	}
}