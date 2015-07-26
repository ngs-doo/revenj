// created on 12/7/2003 at 18:36

// Npgsql.NpgsqlError.cs
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
using System.IO;
using System.Text;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	/// <summary>
	/// EventArgs class to send Notice parameters, which are just NpgsqlError's in a lighter context.
	/// </summary>
	public class NpgsqlNoticeEventArgs : EventArgs
	{
		/// <summary>
		/// Notice information.
		/// </summary>
		public NpgsqlError Notice = null;

		internal NpgsqlNoticeEventArgs(NpgsqlError eNotice)
		{
			Notice = eNotice;
		}
	}

	/// <summary>
	/// This class represents the ErrorResponse and NoticeResponse
	/// message sent from PostgreSQL server.
	/// </summary>
	[Serializable]
	public sealed class NpgsqlError
	{
		private readonly string _severity = string.Empty;
		private readonly string _code = string.Empty;
		private readonly string _message = string.Empty;
		private readonly string _detail = string.Empty;
		private readonly string _hint = string.Empty;
		private readonly string _position = string.Empty;
		private readonly string _internalPosition = string.Empty;
		private readonly string _internalQuery = string.Empty;
		private readonly string _where = string.Empty;
		private readonly string _file = string.Empty;
		private readonly string _line = string.Empty;
		private readonly string _routine = string.Empty;
		private readonly string _schemaName = string.Empty;
		private readonly string _tableName = string.Empty;
		private readonly string _columnName = string.Empty;
		private readonly string _datatypeName = string.Empty;
		private readonly string _constraintName = string.Empty;

		/// <summary>
		/// Severity code.  All versions.
		/// </summary>
		public string Severity
		{
			get { return _severity; }
		}

		/// <summary>
		/// Error code.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Code
		{
			get { return _code; }
		}

		/// <summary>
		/// Terse error message.  All versions.
		/// </summary>
		public string Message
		{
			get { return _message; }
		}

		/// <summary>
		/// Detailed error message.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Detail
		{
			get { return _detail; }
		}

		/// <summary>
		/// Suggestion to help resolve the error.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Hint
		{
			get { return _hint; }
		}

		/// <summary>
		/// Position (one based) within the query string where the error was encounterd.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Position
		{
			get { return _position; }
		}

		/// <summary>
		/// Position (one based) within the query string where the error was encounterd.  This position refers to an internal command executed for example inside a PL/pgSQL function. PostgreSQL 7.4 and up.
		/// </summary>
		public string InternalPosition
		{
			get { return _internalPosition; }
		}

		/// <summary>
		/// Internal query string where the error was encounterd.  This position refers to an internal command executed for example inside a PL/pgSQL function. PostgreSQL 7.4 and up.
		/// </summary>
		public string InternalQuery
		{
			get { return _internalQuery; }
		}
		/// <summary>
		/// Trace back information.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Where
		{
			get { return _where; }
		}

		/// <summary>
		/// Source file (in backend) reporting the error.  PostgreSQL 7.4 and up.
		/// </summary>
		public string File
		{
			get { return _file; }
		}

		/// <summary>
		/// Source file line number (in backend) reporting the error.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Line
		{
			get { return _line; }
		}

		/// <summary>
		/// Source routine (in backend) reporting the error.  PostgreSQL 7.4 and up.
		/// </summary>
		public string Routine
		{
			get { return _routine; }
		}

		/// <summary>
		/// Schema name which relates to the error. PostgreSQL 9.3 and up.
		/// </summary>
		public string SchemaName
		{
			get { return _schemaName; }
		}

		/// <summary>
		/// Table name which relates to the error. PostgreSQL 9.3 and up.
		/// </summary>
		public string TableName
		{
			get { return _tableName; }
		}

		/// <summary>
		/// Column name which relates to the error. PostgreSQL 9.3 and up.
		/// </summary>
		public string ColumnName
		{
			get { return _columnName; }
		}

		/// <summary>
		/// Data type of column which relates to the error. PostgreSQL 9.3 and up.
		/// </summary>
		public string DataTypeName
		{
			get { return _datatypeName; }
		}

		/// <summary>
		/// Constraint name which relates to the error. PostgreSQL 9.3 and up.
		/// </summary>
		public string ConstraintName
		{
			get { return _constraintName; }
		}

		/// <summary>
		/// String containing the sql sent which produced this error.
		/// </summary>
		public string ErrorSql { get; set; }

		/// <summary>
		/// Return a string representation of this error object.
		/// </summary>
		public override String ToString()
		{
			StringBuilder B = new StringBuilder();

			if (Severity.Length > 0)
			{
				B.AppendFormat("{0}: ", Severity);
			}
			if (Code.Length > 0)
			{
				B.AppendFormat("{0}: ", Code);
			}
			B.AppendFormat("{0}", Message);
			// CHECKME - possibly multi-line, that is yucky
			//            if (Hint.Length > 0) {
			//                B.AppendFormat(" ({0})", Hint);
			//            }

			return B.ToString();
		}

		internal NpgsqlError(Stream stream, byte[] buffer, ByteBuffer queue)
		{
			// Check the messageLength value. If it is 1178686529, this would be the
			// "FATA" string, which would mean a protocol 2.0 error string.
			if (PGUtil.ReadInt32(stream, buffer) == 1178686529)
			{
				string[] v2Parts = ("FATA" + PGUtil.ReadString(stream, queue)).Split(new char[] { ':' }, 2);
				if (v2Parts.Length == 2)
				{
					_severity = v2Parts[0].Trim();
					_message = v2Parts[1].Trim();
				}
				else
				{
					_severity = string.Empty;
					_message = v2Parts[0].Trim();
				}
			}
			else
			{
				for (char field = (char)stream.ReadByte(); field != 0; field = (char)stream.ReadByte())
				{
					switch (field)
					{
						case 'S':
							_severity = PGUtil.ReadString(stream, queue);
							break;
						case 'C':
							_code = PGUtil.ReadString(stream, queue);
							break;
						case 'M':
							_message = PGUtil.ReadString(stream, queue);
							break;
						case 'D':
							_detail = PGUtil.ReadString(stream, queue);
							break;
						case 'H':
							_hint = PGUtil.ReadString(stream, queue);
							break;
						case 'P':
							_position = PGUtil.ReadString(stream, queue);
							break;
						case 'p':
							_internalPosition = PGUtil.ReadString(stream, queue);
							break;
						case 'q':
							_internalQuery = PGUtil.ReadString(stream, queue);
							break;
						case 'W':
							_where = PGUtil.ReadString(stream, queue);
							break;
						case 'F':
							_file = PGUtil.ReadString(stream, queue);
							break;
						case 'L':
							_line = PGUtil.ReadString(stream, queue);
							break;
						case 'R':
							_routine = PGUtil.ReadString(stream, queue);
							break;
						case 's':
							_schemaName = PGUtil.ReadString(stream, queue);
							break;
						case 't':
							_tableName = PGUtil.ReadString(stream, queue);
							break;
						case 'c':
							_columnName = PGUtil.ReadString(stream, queue);
							break;
						case 'd':
							_datatypeName = PGUtil.ReadString(stream, queue);
							break;
						case 'n':
							_constraintName = PGUtil.ReadString(stream, queue);
							break;
						default:
							// Unknown error field; consume and discard.
							PGUtil.ReadString(stream, queue);
							break;
					}
				}
			}
		}

		internal NpgsqlError(string errorMessage)
		{
			_message = errorMessage;
		}
	}
}
