using System;
using System.IO;
using System.Net.Mail;
using System.Net.Mime;

namespace NGS.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableLinkedResource
	{
		private readonly string ContentId;
		private readonly Uri ContentLink;
		private readonly Stream ContentStream;
		private readonly SerializableContentType ContentType;
		private readonly TransferEncoding TransferEncoding;

		public SerializableLinkedResource(LinkedResource linkedResource)
		{
			ContentId = linkedResource.ContentId;
			ContentLink = linkedResource.ContentLink;
			ContentType = new SerializableContentType(linkedResource.ContentType);
			TransferEncoding = linkedResource.TransferEncoding;

			if (linkedResource.ContentStream != null)
			{
				var bytes = new byte[linkedResource.ContentStream.Length];
				linkedResource.ContentStream.Read(bytes, 0, bytes.Length);
				ContentStream = new MemoryStream(bytes);
			}
		}

		public LinkedResource GetLinkedResource()
		{
			return new LinkedResource(ContentStream)
			{
				ContentId = ContentId,
				ContentLink = ContentLink,
				ContentType = ContentType.GetContentType(),
				TransferEncoding = TransferEncoding
			};
		}
	}
}