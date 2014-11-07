using System;
using System.Collections.Concurrent;
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

		private static readonly ConcurrentBag<char[]> Buffers = new ConcurrentBag<char[]>();

		static XmlConverter()
		{
			for (int i = 0; i < Environment.ProcessorCount / 2 + 1; i++)
				Buffers.Add(new char[4096]);
		}

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
					char[] buf;
					var took = Buffers.TryTake(out buf);
					if (!took) buf = new char[4096];
					int len;
					sw.Write('"');
					while ((len = reader.Read(buf, 0, 4096)) > 0)
						StringConverter.SerializePart(buf, len, sw);
					sw.Write('"');
					Buffers.Add(buf);
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

		public static XElement Deserialize(TextReader sr, char[] buffer, ref int nextToken)
		{
			if (nextToken == '"')
			{
				var value = StringConverter.Deserialize(sr, buffer, nextToken);
				nextToken = sr.Read();
				return XElement.Parse(value);
			}
			using (var cms = JsonSerialization.Memorize(sr, ref nextToken))
				return (XElement)JsonNet.Deserialize(cms.GetReader(), typeof(XElement));
		}

		public static XElement DeserializeNullable(TextReader sr, char[] buffer, ref int nextToken)
		{
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					return null;
				throw new SerializationException("Invalid null value found at " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return Deserialize(sr, buffer, ref nextToken);
		}

		public static List<XElement> DeserializeCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<XElement>();
			res.Add(Deserialize(sr, buffer, ref nextToken));
			while ((nextToken = JsonSerialization.MoveToNextToken(sr, nextToken)) == ',')
			{
				nextToken = JsonSerialization.GetNextToken(sr);
				res.Add(Deserialize(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}

		public static List<XElement> DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			var res = new List<XElement>();
			if (nextToken == 'n')
			{
				if (sr.Read() == 'u' && sr.Read() == 'l' && sr.Read() == 'l')
					res.Add(null);
				else throw new SerializationException("Invalid value found at position " + JsonSerialization.PositionInStream(sr) + " for xml value. Expecting string, object or null");
				nextToken = sr.Read();
			}
			else res.Add(Deserialize(sr, buffer, ref nextToken));
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
				else res.Add(Deserialize(sr, buffer, ref nextToken));
			}
			if (nextToken != ']')
			{
				if (nextToken == -1) throw new SerializationException("Unexpected end of json in collection.");
				else throw new SerializationException("Expecting ']' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			}
			return res;
		}
	}
}
