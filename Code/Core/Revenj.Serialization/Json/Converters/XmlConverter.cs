using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using System.Xml.Linq;
using Newtonsoft.Json;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class XmlConverter
	{
		public static bool StringFormat;
		private static readonly JsonSerializer JsonNet = new JsonSerializer();

		public static void Serialize(XElement value, TextWriter sw, bool minimal)
		{
			if (StringFormat || minimal)
			{
				using (var cms = ChunkedMemoryStream.Create())
				{
					var writer = cms.GetWriter();
					value.Save(writer, SaveOptions.DisableFormatting);
					writer.Flush();
					cms.Position = 0;
					var reader = cms.GetReader();
					var buf = cms.CharBuffer;
					int len;
					sw.Write('"');
					while ((len = reader.Read(buf, 0, 4096)) > 0)
						StringConverter.SerializePart(buf, len, sw);
					sw.Write('"');
				}
			}
			else
				JsonNet.Serialize(sw, value, typeof(XElement));
		}
		public static void SerializeNullable(XElement value, TextWriter sw, bool minimal)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw, minimal);
		}

		public static XElement Deserialize(BufferedTextReader sr, ref int nextToken)
		{
			if (nextToken == '"')
			{
				var value = StringConverter.Deserialize(sr, nextToken);
				nextToken = sr.Read();
				try
				{
					return XElement.Parse(value);
				}
				catch (Exception ex)
				{
					throw new SerializationException("Error parsing XML at " + JsonSerialization.PositionInStream(sr) + ". " + ex.Message, ex);
				}
			}
			using (var cms = JsonSerialization.Memorize(sr, ref nextToken))
				return (XElement)JsonNet.Deserialize(cms.GetReader(), typeof(XElement));
		}

		public static XElement DeserializeNullable(BufferedTextReader sr, ref int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, ref nextToken);
		}

		public static List<XElement> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<XElement>();
			DeserializeCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<XElement> res)
		{
			res.Add(Deserialize(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
		public static List<XElement> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			var res = new List<XElement>();
			DeserializeNullableCollection(sr, nextToken, res);
			return res;
		}
		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<XElement> res)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for xml value. Expecting string, object or null");
				nextToken = sr.Read();
			}
			else res.Add(Deserialize(sr, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				if (nextToken == 'n')
				{
					if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
						res.Add(null);
					else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for xml value. Expecting string, object or null");
					nextToken = sr.Read();
				}
				else res.Add(Deserialize(sr, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
		}
	}
}
