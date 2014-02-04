using System;
using System.IO;
using System.Text;

namespace NGS.Serialization
{
	internal class StringBuilderReader : TextReader
	{
		private readonly StringBuilder Buffer;
		private readonly int Length;
		private int Position;

		public StringBuilderReader(StringBuilder buffer)
		{
			this.Buffer = buffer;
			this.Position = 0;
			this.Length = Buffer.Length;
		}

		internal void Reset()
		{
			Position = 0;
		}

		public override int Peek()
		{
			return Buffer[Position];
		}

		public override int Read()
		{
			return Buffer[Position++];
		}

		public override int Read(char[] buffer, int index, int count)
		{
			var min = Math.Min(Length - Position, count);
			Buffer.CopyTo(Position, buffer, index, min);
			Position += min;
			return min;
		}

		public override int ReadBlock(char[] buffer, int index, int count)
		{
			var min = Math.Min(count, Length - Position);
			var res = Read(buffer, index, min);
			Position += res;
			return res;
		}

		public override string ReadLine()
		{
			int pos = Position;
			while (pos < Length)
			{
				var c = Buffer[pos];
				if (c != '\r' && c != '\n')
					pos++;
			}
			if (pos < Length - 1 && Buffer[pos] == '\r' && Buffer[pos + 1] == '\n')
				pos++;
			Position = pos;
			return Buffer.ToString(pos, Position - pos);
		}

		public override string ReadToEnd()
		{
			return Buffer.ToString(Position, Length - Position);
		}
	}
}
