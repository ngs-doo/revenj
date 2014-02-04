using System;
using System.Net.Mime;

namespace NGS.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableContentDisposition
	{
		private readonly DateTime CreationDate;
		private readonly string DispositionType;
		private readonly string FileName;
		private readonly bool Inline;
		private readonly DateTime ModificationDate;
		private readonly SerializableCollection Parameters;
		private readonly DateTime ReadDate;
		private readonly long Size;

		public SerializableContentDisposition(ContentDisposition contentDisposition)
		{
			CreationDate = contentDisposition.CreationDate;
			DispositionType = contentDisposition.DispositionType;
			FileName = contentDisposition.FileName;
			Inline = contentDisposition.Inline;
			ModificationDate = contentDisposition.ModificationDate;
			Parameters = new SerializableCollection(contentDisposition.Parameters);
			ReadDate = contentDisposition.ReadDate;
			Size = contentDisposition.Size;
		}

		public void CopyTo(ContentDisposition contentDisposition)
		{
			contentDisposition.CreationDate = CreationDate;
			contentDisposition.DispositionType = DispositionType;
			contentDisposition.FileName = FileName;
			contentDisposition.Inline = Inline;
			contentDisposition.ModificationDate = ModificationDate;
			contentDisposition.ReadDate = ReadDate;
			contentDisposition.Size = Size;

			Parameters.CopyTo(contentDisposition.Parameters);
		}
	}
}