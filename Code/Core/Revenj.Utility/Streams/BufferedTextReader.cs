using System;
using System.IO;
using System.Runtime.Serialization;
using System.Text;

namespace Revenj.Utility
{
	/// <summary>
	/// Performant text reader.
	/// Should be reused whenever possible.
	/// </summary>
	public sealed class BufferedTextReader : TextReader
	{
		private TextReader Reader;
		private readonly char[] Buffer = new char[4096];
		private int InBuffer;
		private int BufferEnd;
		private int NextChar;
		private int TotalBuffersRead;
		/// <summary>
		/// Temporary small char buffer for reuse
		/// </summary>
		public readonly char[] SmallBuffer;
		/// <summary>
		/// Temporary char buffer for reuse
		/// </summary>
		public readonly char[] CharBuffer;
		/// <summary>
		/// Temporary byte buffer for reuse (1024 bytes total)
		/// </summary>
		public readonly byte[] ByteBuffer = new byte[1024];
		/// <summary>
		/// Temporary large temp buffer for reuse (32768 chars total)
		/// </summary>
		public readonly char[] LargeTempBuffer = new char[32768];
		private char[] WorkingBuffer = new char[4096];
		private int WorkingPosition;

		/// <summary>
		/// Allocate reader by reusing part of the buffers and providing original reader object.
		/// </summary>
		/// <param name="reader"></param>
		/// <param name="smallBuffer"></param>
		/// <param name="tempBuffer"></param>
		public BufferedTextReader(TextReader reader, char[] smallBuffer, char[] tempBuffer)
		{
			this.SmallBuffer = smallBuffer;
			this.CharBuffer = tempBuffer;
			Reuse(reader);
		}

		/// <summary>
		/// Allocate reader by reusing part of the buffers and providing original string object
		/// </summary>
		/// <param name="value"></param>
		/// <param name="smallBuffer"></param>
		/// <param name="tempBuffer"></param>
		public BufferedTextReader(string value, char[] smallBuffer, char[] tempBuffer)
		{
			this.SmallBuffer = smallBuffer;
			this.CharBuffer = tempBuffer;
			Reuse(value);
		}

		/// <summary>
		/// Reuse existing instance with a new reader
		/// </summary>
		/// <param name="reader">new reader to process</param>
		/// <returns>itself</returns>
		public BufferedTextReader Reuse(TextReader reader)
		{
			this.Reader = reader;
			Initialize();
			return this;
		}

		private static readonly TextReader EmptyReader = new StringReader(string.Empty);

		/// <summary>
		/// Reuse existing instance with a new string
		/// </summary>
		/// <param name="value">new string to process</param>
		/// <returns>itself</returns>
		public BufferedTextReader Reuse(string value)
		{
			if (value.Length > Buffer.Length)
				return Reuse(new StringReader(value));
			TotalBuffersRead = 0;
			InBuffer = 0;
			value.CopyTo(0, Buffer, 0, value.Length);
			this.Reader = EmptyReader;
			BufferEnd = value.Length;
			NextChar = BufferEnd > 0 ? Buffer[0] : -1;
			return this;
		}

		private void Initialize()
		{
			TotalBuffersRead = 0;
			InBuffer = 0;
			BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
			NextChar = BufferEnd > 0 ? Buffer[0] : -1;
		}

		/// <summary>
		/// Current position.
		/// Sum of total processed buffers and position in the current buffer.
		/// </summary>
		public int Position { get { return TotalBuffersRead + InBuffer; } }

		/// <summary>
		/// Read next char without changing position.
		/// Will return -1 on end of input
		/// </summary>
		/// <returns>next char</returns>
		public override int Peek()
		{
			return NextChar;
		}

		/// <summary>
		/// Read next char and move a single position
		/// Will return -1 on end of input
		/// </summary>
		/// <returns>next char</returns>
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

		/// <summary>
		/// Skip several chars and read only the last one.
		/// Don't use return value if total == 0
		/// Moves position by specified argument
		/// </summary>
		/// <param name="total">total chars to read</param>
		/// <returns>last char read</returns>
		public int Read(int total)
		{
			var result = NextChar;
			if (total == 0)
				return 0;
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
				if (InBuffer == 0)
					return result;
				return InBuffer >= BufferEnd ? -1 : Buffer[InBuffer - 1];
			}
			else if (NextChar != -1)
				NextChar = Buffer[InBuffer];
			if (InBuffer == 0)
				return result;
			return Buffer[InBuffer - 1];
		}

		/// <summary>
		/// Reset buffer to starting position. 
		/// Buffer should be used to fill in small values which will be reconstructed after
		/// </summary>
		public void InitBuffer()
		{
			WorkingPosition = 0;
		}

		/// <summary>
		/// Reset buffer to starting position and specify initial value.
		/// Buffer should be used to fill in small values which will be reconstructed after
		/// </summary>
		/// <param name="c"></param>
		public void InitBuffer(char c)
		{
			WorkingPosition = 1;
			WorkingBuffer[0] = c;
		}

		/// <summary>
		/// Fill buffer until specified char is found.
		/// If end of input is detected, but char is not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="match">char to found</param>
		/// <returns>how many chars were processed</returns>
		public int FillUntil(char match)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
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
			throw new SerializationException("At the end of input. Unable to match: " + match);
		}

		/// <summary>
		/// Fill buffer until any of the specified chars is found.
		/// If end of input is detected, but chars are not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="match1">char to found</param>
		/// <param name="match2">char to found</param>
		/// <returns>how many chars were processed</returns>
		public int FillUntil(char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
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
			throw new SerializationException("At the end of input. Unable to match: " + match1 + " or " + match2);
		}

		/// <summary>
		/// Fill provided writer until any of the specified chars is found.
		/// If end of input is detected, but chars are not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="writer">text writer to populate</param>
		/// <param name="match1">char to found</param>
		/// <param name="match2">char to found</param>
		/// <returns>how many chars were processed</returns>
		public int FillUntil(TextWriter writer, char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
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
			throw new SerializationException("At the end of input. Unable to match: " + match1 + " or " + match2);
		}

		/// <summary>
		/// Append char to internal buffer.
		/// </summary>
		/// <param name="ch">char to add</param>
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

		/// <summary>
		/// Check if buffer matches provided string.
		/// Used as string comparison without new string allocation.
		/// </summary>
		/// <param name="reference">compare to</param>
		/// <returns>buffer matches string</returns>
		public bool BufferMatches(string reference)
		{
			if (reference.Length != WorkingPosition)
				return false;
			for (int i = 0; i < WorkingPosition && i < WorkingBuffer.Length; i++)
				if (WorkingBuffer[i] != reference[i])
					return false;
			return true;
		}

		/// <summary>
		/// Current buffer hash.
		/// Used as string hashcode without allocating new string.
		/// Doesn't match string hashcode algorithm.
		/// </summary>
		/// <returns></returns>
		public int BufferHash()
		{
			var len = WorkingPosition;
			var hash = 0x811C9DC5;
			for (int i = 0; i < len && i < WorkingBuffer.Length; i++)
				hash = (hash ^ WorkingBuffer[i]) * 0x1000193;
			return (int)hash;
		}

		/// <summary>
		/// Convert buffer to new string
		/// </summary>
		/// <returns>new string from populated buffer</returns>
		public string BufferToString()
		{
			var len = WorkingPosition;
			WorkingPosition = 0;
			if (len == 0)
				return string.Empty;
			return new string(WorkingBuffer, 0, len);
		}

		/// <summary>
		/// Convert buffer to an instance
		/// </summary>
		/// <param name="factory">converter</param>
		/// <returns>instance</returns>
		public T BufferToValue<T>(Func<char[], int, T> factory)
		{
			var len = WorkingPosition;
			WorkingPosition = 0;
			return factory(WorkingBuffer, len);
		}

		/// <summary>
		/// Convert buffer to an instance
		/// </summary>
		/// <param name="factory">converter</param>
		/// <returns>instance</returns>
		public T BufferToValue<T>(Func<char[], int, BufferedTextReader, T> factory)
		{
			var len = WorkingPosition;
			WorkingPosition = 0;
			return factory(WorkingBuffer, len, this);
		}

		/// <summary>
		/// Fill target char[] until specified char is found.
		/// If end of input is detected, but char is not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="target">target array to fill</param>
		/// <param name="from">fill target array starting from specified position</param>
		/// <param name="match">char to found</param>
		/// <returns>how many chars were processed</returns>
		public int ReadUntil(char[] target, int from, char match)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
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
			throw new SerializationException("At the end of input. Unable to match: " + match);
		}

		/// <summary>
		/// Fill target char[] until any of the specified chars is found.
		/// If end of input is detected, but chars are not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="target">target array to fill</param>
		/// <param name="from">fill target array starting from specified position</param>
		/// <param name="match1">char to found</param>
		/// <param name="match2">char to found</param>
		/// <returns>how many chars were processed</returns>
		public int ReadUntil(char[] target, int from, char match1, char match2)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
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
			throw new SerializationException("At the end of input. Unable to match: " + match1 + " or " + match2);
		}

		/// <summary>
		/// Fill target char[] while number chars are found.
		/// If end of input is detected, but chars are not found, SerializationException will be thrown.
		/// </summary>
		/// <param name="target">target array to fill</param>
		/// <param name="from">fill target array starting from specified position</param>
		/// <returns>how many chars were processed</returns>
		public int ReadNumber(char[] target, int from)
		{
			if (InBuffer >= BufferEnd)
			{
				TotalBuffersRead += BufferEnd;
				BufferEnd = Reader.Read(Buffer, 0, Buffer.Length);
				InBuffer = 0;
				if (BufferEnd == 0)
					throw new SerializationException("At the end of input");
			}
			var j = from;
			do
			{
				var i = InBuffer;
				char ch;
				for (; i < BufferEnd && i < Buffer.Length && j < target.Length; i++, j++)
				{
					ch = target[j] = Buffer[i];
					if (ch >= '0' && ch <= '9' || ch == '.' || ch == '+' || ch == '-' || ch == 'e' || ch == 'E')
						continue;
					NextChar = ch;
					InBuffer = i;
					return j - from;
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
			throw new SerializationException("At the end of input. Unable read number.");
		}

		/// <summary>
		/// Fill target char[] starting at specified index and up to specified count.
		/// Returns how many chars were copied or -1 on end of input.
		/// Method is allowed to stop early if current buffer end is encountered.
		/// </summary>
		/// <param name="buffer">target array to fill</param>
		/// <param name="index">fill target array starting from specified position</param>
		/// <param name="count">maximum number</param>
		/// <returns>how many chars were copied or -1 for immediate end of input</returns>
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

		/// <summary>
		/// Fill target char[] starting at specified index and up to specified count.
		/// Returns how many chars were copied or -1 on end of input.
		/// Method is not allowed to stop early if current buffer end is encountered.
		/// Must read all available chars up to specified count
		/// </summary>
		/// <param name="buffer">target array to fill</param>
		/// <param name="index">fill target array starting from specified position</param>
		/// <param name="count">maximum number</param>
		/// <returns>how many chars were copied or -1 for immediate end of input</returns>
		public override int ReadBlock(char[] buffer, int index, int count)
		{
			for (int i = 0, result = Read(); result != -1 && i < count; i++, result = Read())
				buffer[index + i] = (char)result;
			return count;
		}

		/// <summary>
		/// Read a single line from input.
		/// </summary>
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

		/// <summary>
		/// Read input to end and convert to string
		/// </summary>
		public override string ReadToEnd()
		{
			return new string(Buffer, InBuffer, BufferEnd) + Reader.ReadToEnd();
		}

		/// <summary>
		/// Close provided reader
		/// </summary>
		public override void Close()
		{
			Reader.Close();
		}

		/// <summary>
		/// Dispose provided reader
		/// </summary>
		/// <param name="disposing"></param>
		protected override void Dispose(bool disposing)
		{
			if (disposing)
				Reader.Dispose();
		}
	}
}
