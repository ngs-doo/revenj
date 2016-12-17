using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Revenj.Utility
{
	/// <summary>
	/// In memory stream with 8192 blocks to avoid LOH issues.
	/// Since .NET places objects larger that 85000 bytes into LOH, 
	/// avoid it as much as possible by using list of smaller blocks.
	/// </summary>
	public sealed class ChunkedMemoryStream : Stream
	{
		private const int BlockSize = 8192;
		private const int BlockShift = 13;
		private const int BlockAnd = 8191;

		private static int SizeLimit = 16 * Environment.ProcessorCount;
		private static ConcurrentStack<ChunkedMemoryStream> MemoryPool = new ConcurrentStack<ChunkedMemoryStream>();
		private static int CurrentEstimate = SizeLimit;

		private static readonly char[] CharMap;
		private static readonly int[] CharLookup;

		/// <summary>
		/// Temporary small char buffer for reuse (64 chars)
		/// </summary>
		public readonly char[] SmallBuffer = new char[64];
		/// <summary>
		/// Temporary char buffer for reuse (8192*4/3 + 2 chars)
		/// </summary>
		public readonly char[] CharBuffer = new char[BlockSize * 4 / 3 + 2];

		static ChunkedMemoryStream()
		{
			for (int i = 0; i < SizeLimit; i++)
				MemoryPool.Push(new ChunkedMemoryStream());
			CharMap = "0123456789abcdef".ToCharArray();
			CharLookup = new int['f' + 1];
			for (int i = 0; i < CharMap.Length; i++)
				CharLookup[CharMap[i]] = i;
		}

		private readonly List<byte[]> Blocks = new List<byte[]>();
		private int CurrentPosition;
		private int TotalSize;
		private CustomWriter Writer;
		private StreamReader Reader;
		private BufferedTextReader BufferedReader;
		private readonly bool IsShared;

		private int BoundToThread;
		private bool UsedReader;
		private bool UsedWriter;
		private bool UsedBuffered;

		/// <summary>
		/// Create or get a new instance of memory stream
		/// Stream is bound to thread and must be released from the same thread
		/// </summary>
		/// <returns>usable memory stream instance</returns>
		public static ChunkedMemoryStream Create()
		{
			ChunkedMemoryStream stream;
			if (MemoryPool.TryPop(out stream))
			{
				CurrentEstimate--;
				stream.Reset();
				stream.BoundToThread = Thread.CurrentThread.ManagedThreadId;
				stream.disposed = false;
				return stream;
			}
			CurrentEstimate = MemoryPool.Count;
			return new ChunkedMemoryStream();
		}

		/// <summary>
		/// Create reusable stream.
		/// Disposing the stream only has the effect of resetting it.
		/// </summary>
		/// <returns></returns>
		public static ChunkedMemoryStream Static()
		{
			var cms = new ChunkedMemoryStream(new byte[BlockSize]);
			cms.GetReader();
			cms.GetWriter();
			cms.UseBufferedReader(string.Empty);
			cms.UsedReader = cms.UsedWriter = cms.UsedBuffered = false;
			return cms;
		}

		/// <summary>
		/// Create new empty stream
		/// Stream is bound to thread and must be released from the same thread
		/// </summary>
		public ChunkedMemoryStream()
		{
			BoundToThread = Thread.CurrentThread.ManagedThreadId;
			IsShared = false;
			Blocks.Add(new byte[BlockSize]);
		}

		private ChunkedMemoryStream(byte[] block)
		{
			IsShared = true;
			Blocks.Add(block);
		}

		class CustomWriter : StreamWriter
		{
			private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

			public CustomWriter(ChunkedMemoryStream cms)
				: base(cms) { }

			public override IFormatProvider FormatProvider { get { return Invariant; } }
		}

		/// <summary>
		/// Create in memory stream based on another stream.
		/// Provided stream will not be disposed.
		/// </summary>
		/// <param name="another">stream to copy</param>
		public ChunkedMemoryStream(Stream another)
			: this(another, false, true) { }
		/// <summary>
		/// Create in memory stream based on another stream.
		/// Specify whether should provided stream be disposed after copying.
		/// </summary>
		/// <param name="another">stream to copy</param>
		/// <param name="dispose">dispose provided stream</param>
		/// <param name="reset">reset provided stream to original position</param>
		public ChunkedMemoryStream(Stream another, bool dispose, bool reset)
			: this()
		{
			var buf = new byte[BlockSize];
			var initialPosition = another.Position;
			int read;
			while ((read = another.Read(buf, 0, BlockSize)) > 0)
				Write(buf, 0, read);
			CurrentPosition = 0;
			if (dispose)
				another.Dispose();
			if (reset && another.CanSeek)
				another.Position = initialPosition;
		}

		/// <summary>
		/// Can this stream be read? Always true
		/// </summary>
		public override bool CanRead { get { return true; } }
		/// <summary>
		/// Can this stream be sought? Always true
		/// </summary>
		public override bool CanSeek { get { return true; } }
		/// <summary>
		/// Can this stream be written to? Always true
		/// </summary>
		public override bool CanWrite { get { return true; } }
		/// <summary>
		/// Flush stream. Does nothing.
		/// </summary>
		public override void Flush() { }
		/// <summary>
		/// Get the length of the stream.
		/// </summary>
		public override long Length { get { return TotalSize; } }
		/// <summary>
		/// Current position in the stream.
		/// </summary>
		public override long Position
		{
			get { return CurrentPosition; }
			set { CurrentPosition = (int)value; }
		}

		/// <summary>
		/// Read a single byte
		/// </summary>
		/// <returns>byte value or -1 for end</returns>
		public override int ReadByte()
		{
			if (CurrentPosition < TotalSize)
			{
				var block = Blocks[CurrentPosition >> BlockShift];
				return block[CurrentPosition++ & BlockAnd];
			}
			return -1;
		}

		/// <summary>
		/// Read buffer from the stream. 
		/// Can return less then specified count if remaining block size is less than specified count
		/// </summary>
		/// <param name="buffer">copy to buffer</param>
		/// <param name="offset">offset in the buffer</param>
		/// <param name="count">maximum size to read</param>
		/// <returns>length of bytes read</returns>
		public override int Read(byte[] buffer, int offset, int count)
		{
			var off = CurrentPosition & BlockAnd;
			var min = BlockSize - off;
			if (count < min)
				min = count;
			if (TotalSize - CurrentPosition < min)
				min = TotalSize - CurrentPosition;
			if (min > 0)
			{
				var pos = CurrentPosition >> BlockShift;
				Buffer.BlockCopy(Blocks[pos], off, buffer, offset, min);
				CurrentPosition += min;
			}
			return min;
		}
		/// <summary>
		/// Seek to position in the stream.
		/// </summary>
		/// <param name="offset">offset at stream</param>
		/// <param name="origin">position type</param>
		/// <returns>current position</returns>
		public override long Seek(long offset, SeekOrigin origin)
		{
			switch (origin)
			{
				case SeekOrigin.Begin:
					CurrentPosition = (int)offset;
					break;
				case SeekOrigin.Current:
					CurrentPosition += (int)offset;
					break;
				default:
					CurrentPosition = TotalSize - (int)offset;
					break;
			}
			if (CurrentPosition > TotalSize)
			{
				var max = CurrentPosition >> BlockShift;
				for (int i = Blocks.Count; i <= max; i++)
					Blocks.Add(new byte[BlockSize]);
				TotalSize = CurrentPosition;
			}
			return CurrentPosition;
		}
		/// <summary>
		/// Set length and position to 0
		/// </summary>
		public void Reset()
		{
			TotalSize = 0;
			CurrentPosition = 0;
			UsedReader = false;
			UsedWriter = false;
			UsedBuffered = false;
		}
		/// <summary>
		/// Set new length of the stream.
		/// Adjusts the current position if new length is larger then it.
		/// </summary>
		/// <param name="value">new length</param>
		public override void SetLength(long value)
		{
			TotalSize = (int)value;
			if (CurrentPosition > TotalSize)
				CurrentPosition = TotalSize;
		}
		/// <summary>
		/// Check if stream starts with provided byte[] and matches it's length
		/// Provided byte[] must be smaller than 8192 bytes
		/// </summary>
		/// <returns>stream matches provided byte[]</returns>
		public bool Matches(byte[] compare)
		{
			if (Length != compare.Length)
				return false;
			var block = Blocks[0];
			for (int i = 0; i < compare.Length; i++)
				if (block[i] != compare[i])
					return false;
			return true;
		}
		/// <summary>
		/// Write byte to stream.
		/// Advances current position by one.
		/// Adds a new block if necessary.
		/// </summary>
		/// <param name="value">byte to write</param>
		public override void WriteByte(byte value)
		{
			var off = CurrentPosition & BlockAnd;
			var pos = CurrentPosition >> BlockShift;
			Blocks[pos][off] = value;
			CurrentPosition += 1;
			if (BlockSize == off + 1 && Blocks.Count == pos + 1)
				Blocks.Add(new byte[BlockSize]);
			if (CurrentPosition > TotalSize)
				TotalSize = CurrentPosition;
		}
		/// <summary>
		/// Write buffer to stream.
		/// Advances current position by count.
		/// Increases length if necessary.
		/// New blocks will be added as required.
		/// It's best to use buffer of size 8192
		/// </summary>
		/// <param name="buffer">provided bytes</param>
		/// <param name="offset">offset in bytes</param>
		/// <param name="count">total length</param>
		public override void Write(byte[] buffer, int offset, int count)
		{
			int cur = count;
			while (cur > 0)
			{
				var pos = CurrentPosition >> BlockShift;
				var off = CurrentPosition & BlockAnd;
				var min = BlockSize - off;
				if (cur < min)
					min = cur;
				Buffer.BlockCopy(buffer, offset + count - cur, Blocks[pos], off, min);
				cur -= min;
				CurrentPosition += min;
				if (min == BlockSize - off && Blocks.Count == pos + 1)
					Blocks.Add(new byte[BlockSize]);
			}
			if (CurrentPosition > TotalSize)
				TotalSize = CurrentPosition;
		}
		/// <summary>
		/// Compare two streams. 
		/// Length and content will be compared.
		/// </summary>
		/// <param name="another">stream to compare</param>
		/// <returns>are streams equal</returns>
		public bool Equals(Stream another)
		{
			var cms = another as ChunkedMemoryStream ?? new ChunkedMemoryStream(another);
			if (cms.TotalSize != TotalSize)
				return false;
			var len = TotalSize >> BlockShift;
			for (int i = 0; i < len; i++)
			{
				var nb = cms.Blocks[i];
				var ob = Blocks[i];
				for (int j = 0; j < BlockSize; j++)
					if (nb[j] != ob[j])
						return false;
			}
			var nlb = cms.Blocks[len];
			var olb = Blocks[len];
			var diff = TotalSize & BlockAnd;
			for (int i = 0; i < diff; i++)
				if (nlb[i] != olb[i])
					return false;
			return true;
		}

		/// <summary>
		/// Convert stream to Base 64 String representation in stream.
		/// </summary>
		/// <returns>created stream</returns>
		public Stream ToBase64Stream()
		{
			var cms = Create();
			ToBase64Writer(cms.GetWriter());
			cms.Position = 0;
			return cms;
		}

		/// <summary>
		/// Convert stream to Base 64 String representation in the provided writer.
		/// </summary>
		public void ToBase64Writer(TextWriter sw)
		{
			if (TotalSize == 0)
				return;
			var tmpBuf = new byte[3];
			var base64 = CharBuffer;
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			int len = 0;
			var off = 0;
			for (int i = 0; i < total; i++)
			{
				var block = Blocks[i];
				len = Convert.ToBase64CharArray(block, off, BlockSize - 2, base64, 0);
				sw.Write(base64, 0, len);
				for (int j = 0; j < 2 - off; j++)
					tmpBuf[j] = block[BlockSize - 2 + j + off];
				block = Blocks[i + 1];
				for (int j = 0; j < 1 + off; j++)
					tmpBuf[2 - off + j] = block[j];
				len = Convert.ToBase64CharArray(tmpBuf, 0, 3, base64, 0);
				sw.Write(base64, 0, len);
				off = (off + 1) & 3;
			}
			len = Convert.ToBase64CharArray(Blocks[total], off, remaining != 0 ? remaining - off : BlockSize, base64, 0);
			sw.Write(base64, 0, len);
			sw.Flush();
		}

		/// <summary>
		/// Convert stream to Postgres representation of bytea
		/// </summary>
		/// <param name="sw"></param>
		public void ToPostgresBytea(TextWriter sw)
		{
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			byte[] block;
			for (int i = 0; i < total; i++)
			{
				block = Blocks[i];
				for (int j = 0; j < block.Length; j++)
				{
					var b = block[j];
					sw.Write(CharMap[b >> 4]);
					sw.Write(CharMap[b & 0xf]);
				}
			}
			block = Blocks[total];
			for (int j = 0; j < remaining; j++)
			{
				var b = block[j];
				sw.Write(CharMap[b >> 4]);
				sw.Write(CharMap[b & 0xf]);
			}
		}

		/// <summary>
		/// Optimized version of copy to stream
		/// </summary>
		/// <param name="stream">destination stream</param>
		public new void CopyTo(Stream stream)
		{
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			for (int i = 0; i < total; i++)
				stream.Write(Blocks[i], 0, BlockSize);
			stream.Write(Blocks[total], 0, remaining);
		}

		public void CopyTo(ChunkedMemoryStream other)
		{
			other.CurrentPosition = CurrentPosition;
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			if (other.TotalSize < total)
			{
				for (int i = other.Blocks.Count; i < total; i++)
					other.Blocks.Add(new byte[BlockSize]);
			}
			for (int i = 0; i < total; i++)
				Buffer.BlockCopy(Blocks[i], 0, other.Blocks[i], 0, BlockSize);
			Buffer.BlockCopy(Blocks[total], 0, other.Blocks[total], 0, remaining);
		}

		/// <summary>
		/// Copy stream to target buffer
		/// </summary>
		/// <param name="target">target array buffer</param>
		/// <param name="offset">start at offset</param>
		public void CopyTo(byte[] target, int offset)
		{
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			for (int i = 0; i < total; i++)
			{
				Buffer.BlockCopy(Blocks[i], 0, target, offset, BlockSize);
				offset += BlockSize;
			}
			Buffer.BlockCopy(Blocks[total], 0, target, offset, remaining);
		}

		/// <summary>
		/// Send entire stream to provided socket.
		/// </summary>
		/// <param name="socket">where to send</param>
		public void Send(Socket socket)
		{
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			for (int i = 0; i < total; i++)
				socket.Send(Blocks[i], SocketFlags.Partial);
			socket.Send(Blocks[total], remaining, SocketFlags.None);
		}

		/// <summary>
		/// Reuse same text writer on this stream.
		/// </summary>
		/// <returns>stream writer</returns>
		public TextWriter GetWriter()
		{
			if (Writer == null)
				Writer = new CustomWriter(this);
			UsedWriter = true;
			return Writer;
		}

		/// <summary>
		/// Reuse same text reader on this stream.
		/// </summary>
		/// <returns>stream reader</returns>
		public TextReader GetReader()
		{
			if (Reader == null)
				Reader = new StreamReader(this);
			UsedReader = true;
			return Reader;
		}

		/// <summary>
		/// Reuse buffered text reader associated with this stream.
		/// Provide input text reader as data source.
		/// Buffered text reader will be initialized with provided input
		/// </summary>
		/// <param name="reader">input for processing</param>
		/// <returns>initialized buffered text reader</returns>
		public BufferedTextReader UseBufferedReader(TextReader reader)
		{
			if (BufferedReader == null)
				return BufferedReader = new BufferedTextReader(reader, SmallBuffer, CharBuffer);
			UsedBuffered = true;
			return BufferedReader.Reuse(reader);
		}

		/// <summary>
		/// Reuse buffered text reader associated with this stream.
		/// Provide input string as data source.
		/// Buffered text reader will be initialized with provided input
		/// </summary>
		/// <param name="value">input for processing</param>
		/// <returns>initialized buffered text reader</returns>
		public BufferedTextReader UseBufferedReader(string value)
		{
			if (BufferedReader == null)
				return BufferedReader = new BufferedTextReader(value, SmallBuffer, CharBuffer);
			UsedBuffered = true;
			return BufferedReader.Reuse(value);
		}

		bool disposed;

		/// <summary>
		/// Close current stream.
		/// Stream will be added to pool if required.
		/// Doesn't release allocated buffers
		/// </summary>
		public override void Close()
		{
			if (IsShared)
			{
				if (UsedReader)
					Reader.DiscardBufferedData();
				UsedBuffered = UsedReader = UsedWriter = false;
			}
			else if (!disposed && BoundToThread == Thread.CurrentThread.ManagedThreadId)
			{
				disposed = true;
				if (Writer != null && UsedWriter)
					Writer.Flush();
				if (Reader != null && UsedReader)
					Reader.DiscardBufferedData();
				UsedBuffered = UsedReader = UsedWriter = false;
				if (CurrentEstimate < SizeLimit || Blocks.Count > 10000)
				{
					MemoryPool.Push(this);
					CurrentEstimate++;
				}
				else CurrentEstimate = MemoryPool.Count;
			}
		}

		/// <summary>
		/// Dispose current stream.
		/// Stream will be added to pool if required.
		/// Used to reset position and length. Doesn't release allocated buffers
		/// </summary>
		/// <param name="disposing"></param>
		protected override void Dispose(bool disposing) { }

		/// <summary>
		/// Show content of the stream as string
		/// </summary>
		/// <returns></returns>
		public override string ToString()
		{
			var sb = new StringBuilder(TotalSize);
			var total = TotalSize >> BlockShift;
			var remaining = TotalSize & BlockAnd;
			for (int i = 0; i < total; i++)
				sb.Append(Encoding.UTF8.GetString(Blocks[i]));
			sb.Append(Encoding.UTF8.GetString(Blocks[total], 0, remaining));
			return sb.ToString();
		}
	}
}