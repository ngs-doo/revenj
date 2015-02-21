using System;
using System.Text;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	internal class ByteBuffer
	{
		private static readonly Encoding UTF8 = Encoding.UTF8;

		private byte[] Buffer = new byte[32];
		private int Position;

		public void Add(byte value)
		{
			if (Position == Buffer.Length)
			{
				var newBuffer = new byte[Buffer.Length * 2];
				Array.Copy(Buffer, newBuffer, Position);
				Buffer = newBuffer;
			}
			Buffer[Position++] = value;
		}

		public void Reset()
		{
			Position = 0;
		}

		public string GetUtf8String()
		{
			return UTF8.GetString(Buffer, 0, Position);
		}
	}
}
