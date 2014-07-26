using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Mail;
using System.Net.Mime;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableAlternateView
	{
		private readonly Uri BaseUri;
		private readonly string ContentId;
		private readonly Stream ContentStream;
		private readonly SerializableContentType ContentType;
		private readonly List<SerializableLinkedResource> LinkedResources = new List<SerializableLinkedResource>();
		private readonly TransferEncoding TransferEncoding;

		public SerializableAlternateView(AlternateView alternativeView)
		{
			BaseUri = alternativeView.BaseUri;
			ContentId = alternativeView.ContentId;
			ContentType = new SerializableContentType(alternativeView.ContentType);
			TransferEncoding = alternativeView.TransferEncoding;

			if (alternativeView.ContentStream != null)
			{
				byte[] bytes = new byte[alternativeView.ContentStream.Length];
				alternativeView.ContentStream.Read(bytes, 0, bytes.Length);
				ContentStream = new MemoryStream(bytes);
			}

			foreach (var lr in alternativeView.LinkedResources)
				LinkedResources.Add(new SerializableLinkedResource(lr));
		}

		public AlternateView GetAlternateView()
		{
			var sav = new AlternateView(ContentStream)
			{
				BaseUri = BaseUri,
				ContentId = ContentId,
				ContentType = ContentType.GetContentType(),
				TransferEncoding = TransferEncoding,
			};

			foreach (var linkedResource in LinkedResources)
				sav.LinkedResources.Add(linkedResource.GetLinkedResource());

			return sav;
		}
	}
}