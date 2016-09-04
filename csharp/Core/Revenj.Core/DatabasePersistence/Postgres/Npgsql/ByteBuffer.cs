using System;
using System.Text;

namespace Revenj.DatabasePersistence.Postgres.Npgsql
{
	internal class ByteBuffer
	{
		private static readonly Encoding UTF8 = Encoding.UTF8;

		private byte[] Buffer = new byte[32];
		private int Position;

		public readonly byte[] Large = new byte[65536];

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

		public void Reset() { Position = 0; }
		public int GetPosition() { return Position; }
		public bool AreSame(byte[] compare)
		{
			for (int i = 0; i < compare.Length && i < Buffer.Length; i++)
				if (compare[i] != Buffer[i])
					return false;
			return compare.Length == Position;
		}

		public string GetUtf8String()
		{
			return UTF8.GetString(Buffer, 0, Position);
		}

		public int? TryGetInt()
		{
			int value = 0;
			if (Position == 0 || Buffer[0] < '0' || Buffer[0] > '9') return null;
			for (int i = 0; i < Buffer.Length; i++)
			{
				if (i == Position) return value;
				value = (value << 3) + (value << 1) + Buffer[i] - '0';
			}
			return value;
		}

		public long GetLong()
		{
			long value = 0;
			for (int i = 0; i < Buffer.Length; i++)
			{
				if (i == Position) return value;
				value = (value << 3) + (value << 1) + Buffer[i] - '0';
			}
			return value;
		}
	}
}
