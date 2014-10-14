using System.IO;
using System.Text;

namespace Revenj.Utility
{
	public class BufferedTextReader : TextReader
	{
		private readonly TextReader Reader;
		private char[] Buffer = new char[4096];
		private int InBuffer;
		private int BufferEnd;
		private int NextChar;

		public BufferedTextReader(TextReader reader)
		{
			this.Reader = reader;
		}

		public void Initialize()
		{
			InBuffer = 0;
			BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
			NextChar = BufferEnd > 0 ? Buffer[0] : -1;
		}

		public override int Peek()
		{
			return NextChar;
		}

		public override int Read()
		{
			var result = NextChar;
			InBuffer++;
			if (InBuffer == BufferEnd)
			{
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				NextChar = BufferEnd > 0 ? Buffer[0] : -1;
			}
			else if (NextChar != -1)
			{
				NextChar = Buffer[InBuffer];
			}
			return result;
		}

		public int ReadUntil(char[] target, int from, char match)
		{
			if (InBuffer == BufferEnd)
			{
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					return NextChar = -1;
			}
			var i = InBuffer;
			var j = from;
			for (; i < BufferEnd && j < target.Length && Buffer[i] != match; i++, j++)
				target[j] = Buffer[i];
			if (i == BufferEnd)
			{
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				NextChar = BufferEnd > 0 ? Buffer[0] : -1;
			}
			else
			{
				NextChar = Buffer[i];
				InBuffer = i;
			}
			return j - from;
		}

		public override int Read(char[] buffer, int index, int count)
		{
			if (count == 0)
				return 0;
			var result = Read();
			if (result == -1)
				return -1;
			buffer[index] = (char)result;
			return 1;
		}

		public override int ReadBlock(char[] buffer, int index, int count)
		{
			for (int i = 0, result = Read(); result != -1 && i < count; i++, result = Read())
				buffer[index + i] = (char)result;
			return count;
		}

		public override string ReadLine()
		{
			var sb = new StringBuilder();
			int next = Read();
			while (next != -1 && next != '\r' && next != '\n')
			{
				sb.Append((char)next);
				next = Read();
			}
			if (next == '\r' && NextChar == '\n')
				Read();
			return sb.ToString();
		}

		public override string ReadToEnd()
		{
			return new string(Buffer, InBuffer, BufferEnd) + Reader.ReadToEnd();
		}

		public override void Close()
		{
			Reader.Close();
		}

		protected override void Dispose(bool disposing)
		{
			if (disposing)
				Reader.Dispose();
		}
	}
}
