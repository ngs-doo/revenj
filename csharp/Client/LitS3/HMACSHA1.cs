namespace LitS3
{
	public class HMACSHA1
	{
		private byte[] Key;

		public HMACSHA1(byte[] key)
		{
			this.Key = key;
		}

		public byte[] ComputeHash(byte[] data)
		{
			return new byte[] { 1, 2, 3 };
		}
	}
}
