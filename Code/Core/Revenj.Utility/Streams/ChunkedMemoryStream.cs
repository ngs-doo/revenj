using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Globalization;
using System.IO;

namespace Revenj.Utility
{
	/// <summary>
	/// In memory stream with 8192 blocks to avoid LOH issues.
	/// Since .NET places objects larger that 85000 bytes into LOH, 
	/// avoid it as much as possible by using list of smaller blocks.
	/// </summary>
	public class ChunkedMemoryStream : Stream
	{
		private const int BlockSize = 8192;
		private const int BlockShift = 13;
		private const int BlockAnd = 8191;

		private static int SizeLimit = 16 * Environment.ProcessorCount;
		private static ConcurrentStack<ChunkedMemoryStream> MemoryPool = new ConcurrentStack<ChunkedMemoryStream>();
		private static int CurrentEstimate = SizeLimit;

		private static readonly char[] CharMap;
		private static readonly int[] CharLookup;

		public readonly char[] TmpBuffer = new char[38];

		static ChunkedMemoryStream()
		{
			for (int i = 0; i < SizeLimit; i++)
				MemoryPool.Push(new ChunkedMemoryStream());
			CharMap = "0123456789abcdef".ToCharArray();
			CharLookup = new int['f' + 1];
			for (int i = 0; i < CharMap.Length; i++)
				CharLookup[CharMap[i]] = i;
		}

		private List<byte[]> Blocks = new List<byte[]>();
		private int CurrentPosition;
		private int TotalSize;
		private CustomWriter Writer;
		private BufferedTextReader Reader;

		private char[] CharBuffer;

		/// <summary>
		/// Create or get a new instance of memory stream
		/// </summary>
		/// <returns>usable memory stream instance</returns>
		public static ChunkedMemoryStream Create()
		{
			ChunkedMemoryStream stream;
			if (MemoryPool.TryPop(out stream))
			{
				CurrentEstimate--;
				stream.disposed = false;
				return stream;
			}
			CurrentEstimate = MemoryPool.Count;
			return new ChunkedMemoryStream();
		}

		/// <summary>
		/// Create new empty stream
		/// </summary>
		public ChunkedMemoryStream()
		{
			Blocks.Add(new byte[BlockSize]);
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
			else
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
		/// Check if stream is of length 4 with bytes = NULL.
		/// </summary>
		/// <returns>is stream content { 'N','U','L','L' }</returns>
		public bool IsNull()
		{
			if (Length != 4)
				return false;
			var block = Blocks[0];
			return block[0] == 'N' && block[1] == 'U' && block[2] == 'L' && block[3] == 'L';
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
			if (CharBuffer == null)
				CharBuffer = new char[BlockSize * 4 / 3 + 2];
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

		/// <summary>
		/// Reuse same text writer on this stream.
		/// </summary>
		/// <returns>stream writer</returns>
		public TextWriter GetWriter()
		{
			if (Writer == null)
				Writer = new CustomWriter(this);
			return Writer;
		}

		/// <summary>
		/// Reuse same text reader on this stream.
		/// </summary>
		/// <returns>stream reader</returns>
		public TextReader GetReader()
		{
			if (Reader == null)
				Reader = new BufferedTextReader(new StreamReader(this));
			Reader.Initialize();
			return Reader;
		}

		bool disposed;

		protected override void Dispose(bool disposing)
		{
			base.Dispose(disposing);
			if (disposing && !disposed)
			{
				disposed = true;
				CurrentPosition = 0;
				TotalSize = 0;
				if (CurrentEstimate < SizeLimit || Blocks.Count > 10000)
				{
					MemoryPool.Push(this);
					CurrentEstimate++;
				}
				else CurrentEstimate = MemoryPool.Count;
			}
		}

		public override string ToString()
		{
			return GetReader().ReadToEnd();
		}
	}
}