using System;

namespace AspNetTutorial
{
	public class CustomException : Exception
	{
		public CustomException(string error)
			: base(error) { }
	}
}
