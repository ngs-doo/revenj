using System.IO;
using System.Runtime.Serialization;
using System.Text;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Xsl;

namespace NGS.Serialization
{
	public class XslTransformation : ITransformation<XElement, XElement>
	{
		[DataMember]
		public string TransformedType { get; protected set; }
		[DataMember]
		public string XmlTransformation { get; protected set; }

		public XElement Transform(ISerialization<XElement> input, ISerialization<XElement> output, XElement value)
		{
			var xslt = new XslCompiledTransform();
			using (var tranValue = new StringReader(XmlTransformation))
			using (var xmlValue = value.CreateReader())
			{
				xslt.Load(XmlReader.Create(tranValue));
				var sbValue = new StringBuilder();
				var xwValue = XmlWriter.Create(sbValue);
				xslt.Transform(xmlValue, xwValue);
				var result = XElement.Parse(sbValue.ToString());
				result.Add(new XAttribute("type", TransformedType));
				return result;
			}
		}

		public static XslTransformation Create(string type, string xml)
		{
			return new XslTransformation { TransformedType = type, XmlTransformation = xml };
		}
	}
}
