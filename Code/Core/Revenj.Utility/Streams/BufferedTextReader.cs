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
		private int TotalBuffersRead;
		public readonly char[] TempBuffer = new char[4096];
		public readonly char[] LargeTempBuffer = new char[65536];
		private readonly StringBuilder Builder = new StringBuilder(65536);

		public BufferedTextReader(TextReader reader)
		{
			this.Reader = reader;
		}

		public void Initialize()
		{
			TotalBuffersRead = 0;
			InBuffer = 0;
			BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
			NextChar = BufferEnd > 0 ? Buffer[0] : -1;
		}

		public StringBuilder GetBuilder()
		{
			Builder.Length = 0;
			return Builder;
		}

		public int Position { get { return TotalBuffersRead + InBuffer; } }

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
				TotalBuffersRead += BufferEnd;
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
				TotalBuffersRead += BufferEnd;
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
				TotalBuffersRead += BufferEnd;
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

		public int ReadUntil(char[] target, int from, char match, out bool found)
		{
			var read = ReadUntil(target, from, match);
			found = NextChar == match;
			return read;
		}

		public override int Read(char[] buffer, int index, int count)
		{
			if (InBuffer == BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					return NextChar = -1;
			}
			var i = InBuffer;
			var j = index;
			for (var c = 0; i < BufferEnd && j < buffer.Length && c < count; i++, j++, c++)
				buffer[j] = Buffer[i];
			if (i == BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				NextChar = BufferEnd > 0 ? Buffer[0] : -1;
			}
			else
			{
				NextChar = Buffer[i];
				InBuffer = i;
			}
			return j - index;
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
