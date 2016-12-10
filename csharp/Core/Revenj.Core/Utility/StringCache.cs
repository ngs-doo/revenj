namespace Revenj.Utility
{
	public class StringCache
	{
		private readonly string[] Cache;
		private readonly int Mask;

		public StringCache() : this(8) { }
		public StringCache(int log2)
		{
			var size = 2;
			for (int i = 0; i < log2; i++)
				size *= 2;
			Cache = new string[size];
			Mask = size - 1;
		}

		public string Get(char[] buffer, int len)
		{
			var hash = CalcHash(buffer, len);
			var index = hash & Mask;
			var value = Cache[index];
			if (value == null)
				return CreateAndPut(index, buffer, len);
			if (value.Length != len)
				return CreateAndPut(index, buffer, len);
			for (int i = 0; i < value.Length; i++)
				if (value[i] != buffer[i]) return CreateAndPut(index, buffer, len);
			return value;
		}

		private string CreateAndPut(int index, char[] buffer, int len)
		{
			var value = new string(buffer, 0, len);
			Cache[index] = value;
			return value;
		}

		public static int CalcHash(string prefix, string name)
		{
			var hash = 0x811C9DC5;
			for (int i = 0; i < prefix.Length; i++)
				hash = (hash ^ prefix[i]) * 0x1000193;
			for (int i = 0; i < name.Length; i++)
				hash = (hash ^ name[i]) * 0x1000193;
			return (int)hash;
		}

		public static int CalcHash(string value)
		{
			var hash = 0x811C9DC5;
			for (int i = 0; i < value.Length; i++)
				hash = (hash ^ value[i]) * 0x1000193;
			return (int)hash;
		}

		public static int CalcHash(string prefix, char[] buffer, int len)
		{
			var hash = 0x811C9DC5;
			for (int i = 0; i < prefix.Length; i++)
				hash = (hash ^ prefix[i]) * 0x1000193;
			for (int i = 0; i < len; i++)
				hash = (hash ^ buffer[i]) * 0x1000193;
			return (int)hash;
		}

		public static int CalcHash(char[] buffer, int len)
		{
			var hash = 0x811C9DC5;
			for (int i = 0; i < len; i++)
				hash = (hash ^ buffer[i]) * 0x1000193;
			return (int)hash;
		}

	}
}
