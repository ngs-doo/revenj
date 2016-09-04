// created on 9/6/2002 at 16:56


// Npgsql.NpgsqlStartupPacket.cs
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

using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	/// <summary>
	/// This class represents a StartupPacket message of PostgreSQL
	/// protocol.
	/// </summary>
	///
	internal sealed class NpgsqlStartupPacket : ClientMessage
	{
		// Private fields.
		private readonly int packet_size;
		private readonly string database_name;
		private readonly string user_name;
		private readonly string arguments;
		private readonly string unused;
		private readonly string optional_tty;

		public NpgsqlStartupPacket(
			int packet_size,
			string database_name,
			string user_name,
			string arguments,
			string unused,
			string optional_tty)
		{
			// [FIXME] Validate params? We are the only clients, so, hopefully, we
			// know what to send.

			this.packet_size = packet_size;
			this.database_name = database_name;
			this.user_name = user_name;
			this.arguments = arguments;
			this.unused = unused;
			this.optional_tty = optional_tty;
		}


		public override void WriteToStream(Stream output_stream)
		{
			PGUtil.WriteInt32(output_stream,
							  4 + 4 + 5 + (UTF8Encoding.GetByteCount(user_name) + 1) + 9 +
							  (UTF8Encoding.GetByteCount(database_name) + 1) + 10 + 4 + 1);

			PGUtil.WriteInt32(output_stream, 196608);
			// User name.
			PGUtil.WriteString("user", output_stream);
			// User name.
			PGUtil.WriteString(user_name, output_stream);
			// Database name.
			PGUtil.WriteString("database", output_stream);
			// Database name.
			PGUtil.WriteString(database_name, output_stream);
			// DateStyle.
			PGUtil.WriteString("DateStyle", output_stream);
			// DateStyle.
			PGUtil.WriteString("ISO", output_stream);

			output_stream.WriteByte(0);
			output_stream.Flush();
		}
	}
}
