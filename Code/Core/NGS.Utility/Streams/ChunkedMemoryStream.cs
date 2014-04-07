using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;

namespace NGS.Utility
{
	/// <summary>
	/// In memory stream with 8192 blocks to avoid LOH issues.
	/// Since .NET places objects larger that 85000 bytes into LOH, 
	/// avoid it as much as possible by using list of smaller blocks.
	/// </summary>
	public class ChunkedMemoryStream : Stream
	{
		private List<byte[]> Blocks = new List<byte[]>();
		private int CurrentPosition;
		private int TotalSize;
		private const int BlockSize = 8192;
		private readonly StreamWriter Writer;
		private readonly StreamReader Reader;

		private static int SizeLimit = 16 * Environment.ProcessorCount;
		private static ConcurrentQueue<ChunkedMemoryStream> PoolQueue = new ConcurrentQueue<ChunkedMemoryStream>();
		private static int CurrentEstimate = SizeLimit;

		static ChunkedMemoryStream()
		{
			for (int i = 0; i < SizeLimit; i++)
				PoolQueue.Enqueue(new ChunkedMemoryStream());
		}

		/// <summary>
		/// Create or get a new instance of memory stream
		/// </summary>
		/// <returns>usable memory stream instance</returns>
		public static ChunkedMemoryStream Create()
		{
			ChunkedMemoryStream stream;
			if (PoolQueue.TryDequeue(out stream))
			{
				CurrentEstimate--;
				stream.disposed = false;
				return stream;
			}
			CurrentEstimate = PoolQueue.Count;
			return new ChunkedMemoryStream();
		}

		/// <summary>
		/// Create new empty stream
		/// </summary>
		public ChunkedMemoryStream()
		{
			Blocks.Add(new byte[BlockSize]);
			Writer = new StreamWriter(this);
			Reader = new StreamReader(this);
		}
		/// <summary>
		/// Create in memory stream based on another stream.
		/// Provided stream will not be disposed.
		/// </summary>
		/// <param name="another">stream to copy</param>
		public ChunkedMemoryStream(Stream another)
			: this(another, false) { }
		/// <summary>
		/// Create in memory stream based on another stream.
		/// Specify wheater should provided stream be disposed after copying.
		/// </summary>
		/// <param name="another">stream to copy</param>
		/// <param name="dispose">dispose provided stream</param>
		public ChunkedMemoryStream(Stream another, bool dispose)
			: this()
		{
			var buf = new byte[BlockSize];
			int read;
			while ((read = another.Read(buf, 0, BlockSize)) > 0)
				Write(buf, 0, read);
			CurrentPosition = 0;
			if (dispose)
				another.Dispose();
		}

		/// <summary>
		/// Can this stream be read? Always true
		/// </summary>
		public override bool CanRead { get { return true; } }
		/// <summary>
		/// Can this stream be seeked? Always true
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
		/// Read buffer from the stream. 
		/// Can return less then specified count if remaining block size is less than specified count
		/// </summary>
		/// <param name="buffer">copy to buffer</param>
		/// <param name="offset">offset in the buffer</param>
		/// <param name="count">maximum size to read</param>
		/// <returns>length of bytes read</returns>
		public override int Read(byte[] buffer, int offset, int count)
		{
			var off = CurrentPosition % BlockSize;
			var min = BlockSize - off;
			if (count < min)
				min = count;
			if (TotalSize - CurrentPosition < min)
				min = TotalSize - CurrentPosition;
			if (min > 0)
			{
				var pos = CurrentPosition / BlockSize;
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
		/// Adds a new block if necesessary.
		/// </summary>
		/// <param name="value">byte to write</param>
		public override void WriteByte(byte value)
		{
			var off = CurrentPosition % BlockSize;
			var pos = CurrentPosition / BlockSize;
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
				var off = CurrentPosition % BlockSize;
				var pos = CurrentPosition / BlockSize;
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
			var len = TotalSize / BlockSize;
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
			var diff = TotalSize % BlockSize;
			for (int j = 0; j < diff; j++)
				if (nlb[j] != olb[j])
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
			var sw = new StreamWriter(cms);
			var tmpBuf = new byte[3];
			var base64 = new char[BlockSize * 4];
			var total = TotalSize > BlockSize ? TotalSize / BlockSize : 0;
			int len;
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
			len = Convert.ToBase64CharArray(Blocks[total], off, TotalSize != BlockSize ? TotalSize % BlockSize - off : BlockSize, base64, 0);
			sw.Write(base64, 0, len);
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		/// <summary>
		/// Reuse same stream writer on this stream.
		/// </summary>
		/// <returns>stream writer</returns>
		public StreamWriter GetWriter() { return Writer; }

		/// <summary>
		/// Reuse same stream reader on this stream.
		/// </summary>
		/// <returns>stream reader</returns>
		public StreamReader GetReader() { return Reader; }

		bool disposed;

		protected override void Dispose(bool disposing)
		{
			base.Dispose(disposing);
			if (disposing && !disposed)
			{
				disposed = true;
				CurrentPosition = 0;
				TotalSize = 0;
				if (CurrentEstimate < SizeLimit)
				{
					PoolQueue.Enqueue(this);
					CurrentEstimate++;
				}
				else
				{
					CurrentEstimate = PoolQueue.Count;
				}
			}
		}
	}
}