using System;
using System.IO;
using System.Text;
using Revenj.Common;

namespace Revenj.Utility
{
	public class BufferedTextReader : TextReader
	{
		private TextReader Reader;
		private char[] Buffer = new char[4096];
		private int InBuffer;
		private int BufferEnd;
		private int NextChar;
		private int TotalBuffersRead;
		public readonly char[] SmallBuffer = new char[64];
		public readonly char[] TempBuffer = new char[4096];
		public readonly char[] LargeTempBuffer = new char[65536];
		private readonly StringBuilder Builder = new StringBuilder(65536);
		private char[] WorkingBuffer = new char[4096];
		private int WorkingPosition;

		public BufferedTextReader(TextReader reader)
		{
			this.Reader = reader;
			Initialize();
		}

		public BufferedTextReader Reuse(TextReader reader)
		{
			this.Reader = reader;
			Initialize();
			return this;
		}

		private void Initialize()
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
				NextChar = Buffer[InBuffer];
			return result;
		}

		public int Read(int total)
		{
			var result = NextChar;
			if (total == 0)
				return Builder[InBuffer];
			InBuffer += total;
			if (InBuffer >= BufferEnd)
			{
				do
				{
					if (BufferEnd != 0)
					{
						result = Buffer[BufferEnd - 1];
						TotalBuffersRead += BufferEnd;
						InBuffer -= BufferEnd;
					}
					BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				} while (InBuffer >= BufferEnd && BufferEnd != 0);
				NextChar = BufferEnd > InBuffer ? Buffer[InBuffer] : -1;
			}
			else if (NextChar != -1)
				NextChar = Buffer[InBuffer];
			if (InBuffer == 0)
				return result;
			return Buffer[InBuffer - 1];
		}

		public void InitBuffer()
		{
			WorkingPosition = 0;
		}

		public void InitBuffer(char c)
		{
			WorkingPosition = 1;
			WorkingBuffer[0] = c;
		}

		public int FillUntil(char match)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new FrameworkException("At the end of input");
			}
			var j = WorkingPosition;
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length && j < WorkingBuffer.Length; i++, j++)
				{
					ch = Buffer[i];
					if (ch == match)
					{
						InBuffer = i;
						WorkingPosition = j;
						return WorkingPosition;
					}
					WorkingBuffer[j] = ch;
				}
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
				if (j == WorkingBuffer.Length)
				{
					var tmp = new char[WorkingBuffer.Length * 2];
					Array.Copy(WorkingBuffer, tmp, WorkingBuffer.Length);
					WorkingBuffer = tmp;
				}
			} while (NextChar != -1);
			throw new FrameworkException("At the end of input");
		}

		public int FillUntil(char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new FrameworkException("At the end of input");
			}
			var j = WorkingPosition;
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length && j < WorkingBuffer.Length; i++, j++)
				{
					ch = Buffer[i];
					if (ch == match1 || ch == match2)
					{
						InBuffer = i;
						WorkingPosition = j;
						return NextChar = ch;
					}
					WorkingBuffer[j] = ch;
				}
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
				if (j == WorkingBuffer.Length)
				{
					var tmp = new char[WorkingBuffer.Length * 2];
					Array.Copy(WorkingBuffer, tmp, WorkingBuffer.Length);
					WorkingBuffer = tmp;
				}
			} while (NextChar != -1);
			throw new FrameworkException("At the end of input");
		}

		public int FillUntil(TextWriter writer, char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new FrameworkException("At the end of input");
			}
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length; i++)
				{
					ch = Buffer[i];
					if (ch == match1 || ch == match2)
					{
						writer.Write(Buffer, InBuffer, i);
						InBuffer = i;
						return NextChar = ch;
					}
				}
				writer.Write(Buffer, InBuffer, i);
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
			} while (NextChar != -1);
			throw new FrameworkException("At the end of input");
		}

		public void AddToBuffer(char ch)
		{
			if (WorkingPosition == WorkingBuffer.Length)
			{
				var tmp = new char[WorkingBuffer.Length * 2];
				Array.Copy(WorkingBuffer, tmp, WorkingBuffer.Length);
				WorkingBuffer = tmp;
			}
			WorkingBuffer[WorkingPosition++] = ch;
		}

		public bool BufferMatches(string reference)
		{
			if (reference.Length != WorkingPosition)
				return false;
			for (int i = 0; i < WorkingPosition && i < WorkingBuffer.Length; i++)
				if (WorkingBuffer[i] != reference[i])
					return false;
			return true;
		}

		public string BufferToString()
		{
			var len = WorkingPosition;
			WorkingPosition = 0;
			if (len == 0)
				return string.Empty;
			return new string(WorkingBuffer, 0, len);
		}

		public int ReadUntil(char[] target, int from, char match)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new FrameworkException("At the end of input");
			}
			var j = from;
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length && j < target.Length; i++, j++)
				{
					ch = Buffer[i];
					if (ch == match)
					{
						NextChar = ch;
						InBuffer = i;
						return j - from;
					}
					target[j] = ch;
				}
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
				if (j == target.Length)
					return j - from;
			} while (NextChar != -1);
			throw new FrameworkException("At the end of input");
		}

		public int ReadUntil(char[] target, int from, char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new FrameworkException("At the end of input");
			}
			var j = from;
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length && j < target.Length; i++, j++)
				{
					ch = Buffer[i];
					if (ch == match1 || ch == match2)
					{
						NextChar = ch;
						InBuffer = i;
						return j - from;
					}
					target[j] = ch;
				}
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
				if (j == target.Length)
					return j - from;
			} while (NextChar != -1);
			throw new FrameworkException("At the end of input");
		}

		public override int Read(char[] buffer, int index, int count)
		{
			if (InBuffer >= BufferEnd)
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
