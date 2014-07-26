using System;
using System.IO;
using System.Net.Mail;
using System.Net.Mime;
using System.Text;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableAttachment
	{
		private readonly string ContentId;
		private readonly SerializableContentDisposition ContentDisposition;
		private readonly SerializableContentType ContentType;
		private readonly Stream ContentStream;
		private readonly TransferEncoding TransferEncoding;
		private readonly string Name;
		private readonly Encoding NameEncoding;

		public SerializableAttachment(Attachment attachment)
		{
			ContentId = attachment.ContentId;
			ContentDisposition = new SerializableContentDisposition(attachment.ContentDisposition);
			ContentType = new SerializableContentType(attachment.ContentType);
			Name = attachment.Name;
			TransferEncoding = attachment.TransferEncoding;
			NameEncoding = attachment.NameEncoding;

			if (attachment.ContentStream != null)
			{
				byte[] bytes = new byte[attachment.ContentStream.Length];
				attachment.ContentStream.Read(bytes, 0, bytes.Length);

				ContentStream = new MemoryStream(bytes);
			}
		}

		public Attachment GetAttachment()
		{
			var attachment = new Attachment(ContentStream, Name)
			{
				ContentId = ContentId,
				ContentType = ContentType.GetContentType(),
				Name = Name,
				TransferEncoding = TransferEncoding,
				NameEncoding = NameEncoding,
			};

			ContentDisposition.CopyTo(attachment.ContentDisposition);

			return attachment;
		}
	}
}