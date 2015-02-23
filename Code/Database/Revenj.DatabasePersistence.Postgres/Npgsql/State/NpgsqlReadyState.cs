// Npgsql.NpgsqlReadyState.cs
//
// Author:
// 	Dave Joyner <d4ljoyn@yahoo.com>
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


using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	internal sealed class NpgsqlReadyState : NpgsqlState
	{
		public static readonly NpgsqlReadyState Instance = new NpgsqlReadyState();

		// Flush and Sync messages. It doesn't need to be created every time it is called.
		private static readonly NpgsqlFlush _flushMessage = new NpgsqlFlush();

		private static readonly NpgsqlSync _syncMessage = new NpgsqlSync();

		private NpgsqlReadyState() : base() { }

		public override IEnumerable<IServerResponseObject> QueryEnum(NpgsqlConnector context, NpgsqlCommand command)
		{
			// Send the query request to backend.

			NpgsqlQuery.Send(command, context.Stream);
			context.Stream.Flush();

			return ProcessBackendResponsesEnum(context, false);
		}

		public override void Parse(NpgsqlConnector context, NpgsqlParse parse)
		{
			Stream stream = context.Stream;
			parse.WriteToStream(stream);
			//stream.Flush();
		}

		public override IEnumerable<IServerResponseObject> SyncEnum(NpgsqlConnector context)
		{
			_syncMessage.WriteToStream(context.Stream);
			context.Stream.Flush();
			return ProcessBackendResponsesEnum(context, false);
		}

		public override void Flush(NpgsqlConnector context)
		{
			_flushMessage.WriteToStream(context.Stream);
			context.Stream.Flush();
			ProcessBackendResponses(context);
		}

		public override void Bind(NpgsqlConnector context, NpgsqlBind bind)
		{
			Stream stream = context.Stream;

			bind.WriteToStream(stream);
			//stream.Flush();
		}

		public override void Describe(NpgsqlConnector context, NpgsqlDescribe describe)
		{
			describe.WriteToStream(context.Stream);
			//context.Stream.Flush();
		}

		public override void Execute(NpgsqlConnector context, NpgsqlExecute execute)
		{
			Stream stream = context.Stream;
			NpgsqlDescribe.Send('P', execute.PortalName, stream);
			execute.WriteToStream(stream);
			//stream.Flush();
			Sync(context);
		}

		public override IEnumerable<IServerResponseObject> ExecuteEnum(NpgsqlConnector context, NpgsqlExecute execute)
		{
			Stream stream = context.Stream;
			NpgsqlDescribe.Send('P', execute.PortalName, stream);
			execute.WriteToStream(stream);
			//stream.Flush();
			return SyncEnum(context);
		}

		public override void Close(NpgsqlConnector context)
		{
			Stream stream = context.Stream;
			try
			{
				stream.WriteByte((byte)FrontEndMessageCode.Termination);
				PGUtil.WriteInt32(stream, 4);
				stream.Flush();
			}
			catch
			{
				//Error writting termination message to stream, nothing we can do.
			}

			try { stream.Close(); }
			catch { }

			context.Stream = null;
			ChangeState(context, NpgsqlClosedState.Instance);
		}
	}
}
