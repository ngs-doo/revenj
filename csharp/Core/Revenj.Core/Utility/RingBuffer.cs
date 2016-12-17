using System.Threading;

namespace Revenj.Utility
{
	public class RingBuffer<T> where T : class
	{
		private readonly int Mask;
		private readonly T[] Pool;
		private int WritePosition;
		private int ReadPosition;

		public RingBuffer(int log2)
		{
			var size = 2;
			for (int i = 0; i < log2; i++)
				size *= 2;
			Pool = new T[size];
			Mask = Pool.Length - 1;
		}

		public int Size { get { return WritePosition - ReadPosition; } }

		public void Add(T work)
		{
			var next = Interlocked.Increment(ref WritePosition);
			var index = (next - 1) & Mask;
			while (Pool[index] != null)
				Thread.Yield();
			Pool[index] = work;
		}

		public T Take(ManualResetEvent sync)
		{
			var next = Interlocked.Increment(ref ReadPosition);
			var index = (next - 1) & Mask;
			T work;
			while ((work = Pool[index]) == null)
			{
				Thread.Yield();
				Thread.MemoryBarrier();
				if (next > WritePosition)
				{
					sync.Reset();
					sync.WaitOne();
				}
			}
			Pool[index] = null;
			return work;
		}
	}
}
