using System;
using System.Threading;

namespace Revenj.Utility
{
	public class RingBuffer<T> where T : class
	{
		private readonly ManualResetEvent[] Users;
		private readonly int Mask;
		private readonly T[] Pool;
		private int WritePosition;
		private int ReadPosition;

		public RingBuffer(int log2, ManualResetEvent[] users)
		{
			this.Users = users;
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
			for (int i = 0; i < Users.Length; i++)
				Users[i].Set();
		}

		public T Take(ManualResetEvent sync)
		{
			var next = Interlocked.Increment(ref ReadPosition);
			var index = (next - 1) & Mask;
			T work;
			if ((work = Pool[index]) == null)
			{
				sync.Reset();
				do
				{
					Thread.Yield();
					if (next > WritePosition)
					{
						var maxWrite = Thread.VolatileRead(ref WritePosition);
						if (next > maxWrite)
						{
							sync.WaitOne();
							sync.Reset();
						}
					}
				} while ((work = Pool[index]) == null);
				sync.Set();
			}
			Pool[index] = null;
			return work;
		}
	}
}
