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

		private readonly char[] Chars = new char[256];

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
			if (compare.Length != Position) return false;
			for (int i = 0; i < compare.Length && i < Buffer.Length; i++)
				if (compare[i] != Buffer[i])
					return false;
			return true;
		}

		public string GetUtf8String()
		{
			if (Position == 0) return string.Empty;
			else if (Position < 256)
			{
				for (var i = 0; i < Position; i++)
				{
					var ch = Buffer[i];
					if (ch > 126) return UTF8.GetString(Buffer, 0, Position);
					Chars[i] = (char)ch;
				}
				return new string(Chars, 0, Position);
			}
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
