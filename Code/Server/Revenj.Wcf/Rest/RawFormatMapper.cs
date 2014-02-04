using System.ServiceModel.Channels;

namespace Revenj.Wcf
{
	public class RawFormatMapper : WebContentTypeMapper
	{
		public override WebContentFormat GetMessageFormatForContentType(string contentType)
		{
			return WebContentFormat.Raw;
		}
	}
}