using System;
using System.Net.Mime;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	internal class SerializableContentType
	{
		private readonly string Boundary;
		private readonly string CharSet;
		private readonly string MediaType;
		private readonly string Name;
		private readonly SerializableCollection Parameters;

		public SerializableContentType(ContentType contentType)
		{
			Boundary = contentType.Boundary;
			CharSet = contentType.CharSet;
			MediaType = contentType.MediaType;
			Name = contentType.Name;
			Parameters = new SerializableCollection(contentType.Parameters);
		}

		public ContentType GetContentType()
		{
			var sct = new ContentType()
			{
				Boundary = Boundary,
				CharSet = CharSet,
				MediaType = MediaType,
				Name = Name,
			};

			Parameters.CopyTo(sct.Parameters);

			return sct;
		}
	}
}