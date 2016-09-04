using System.Collections.Generic;
using System.IO;
using Revenj.Common;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class StringConverter
	{
		public static void Skip(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return;
			if (cur != '"' && cur != '\\')
			{
				reader.InitBuffer();
				reader.FillUntil(',', ')');
				reader.Read();
			}
			else
			{
				cur = reader.Read(context);
				while (cur != -1)
				{
					if (cur == '\\' || cur == '"')
					{
						cur = reader.Read(context);
						if (cur == ',' || cur == ')')
							return;
						cur = reader.Read(context);
					}
					else cur = reader.Read();
				}
				throw new FrameworkException("Unable to find end of string");
			}
		}

		public static string Parse(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			if (cur != '"' && cur != '\\')
			{
				reader.InitBuffer((char)cur);
				reader.FillUntil(',', ')');
				reader.Read();
				return reader.BufferToString();
			}
			return ParseEscapedString(reader, context, ref cur, ')');
		}

		private static string ParseEscapedString(BufferedTextReader reader, int context, ref int cur, char matchEnd)
		{
			cur = reader.Read(context);
			reader.InitBuffer();
			do
			{
				if (cur == '\\' || cur == '"')
				{
					cur = reader.Read(context);
					if (cur == ',' || cur == matchEnd)
						return reader.BufferToString();
					for (int i = 0; i < context - 1; i++)
						cur = reader.Read();
				}
				reader.AddToBuffer((char)cur);
				reader.FillUntil('\\', '"');
				cur = reader.Read();
			} while (cur != -1);
			throw new FrameworkException("Unable to find end of string");
		}

		public static Stream ParseStream(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var cms = ChunkedMemoryStream.Create();
			var sw = cms.GetWriter();
			if (cur != '"' && cur != '\\')
			{
				sw.Write((char)cur);
				reader.FillUntil(sw, ',', ')');
			}
			else
			{
				cur = reader.Read(context);
				while (cur != -1)
				{
					if (cur == '\\' || cur == '"')
					{
						cur = reader.Read(context);
						if (cur == ',' || cur == ')')
						{
							sw.Flush();
							cms.Position = 0;
							return cms;
						}
						for (int i = 0; i < context - 1; i++)
							cur = reader.Read();
					}
					sw.Write((char)cur);
					cur = reader.Read();
				}
				reader.Read(context);
			}
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		public static List<string> ParseCollection(BufferedTextReader reader, int context, bool allowNull)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (espaced)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<string>(0);
			}
			var innerContext = context << 1;
			var list = new List<string>();
			var emptyCol = allowNull ? null : string.Empty;
			do
			{
				cur = reader.Read();
				if (cur == '"' || cur == '\\')
					list.Add(ParseEscapedString(reader, innerContext, ref cur, '}'));
				else
				{
					reader.InitBuffer((char)cur);
					reader.FillUntil(',', '}');
					cur = reader.Read();
					if (reader.BufferMatches("NULL"))
						list.Add(emptyCol);
					else
						list.Add(reader.BufferToString());
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		private static readonly byte[] NULL = new byte[] { (byte)'N', (byte)'U', (byte)'L', (byte)'L' };

		public static List<Stream> ParseStreamCollection(BufferedTextReader reader, int context, bool allowNull)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				cur = reader.Read(context);
			var innerContext = context << 1;
			var list = new List<Stream>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				var cms = ChunkedMemoryStream.Create();
				var sw = cms.GetWriter();
				if (cur == '"' || cur == '\\')
				{
					cur = reader.Read(innerContext);
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							cur = reader.Read(innerContext);
							if (cur == ',' || cur == '}')
								break;
							for (int i = 0; i < innerContext - 1; i++)
								cur = reader.Read();
						}
						sw.Write((char)cur);
						cur = reader.Read();
					}
					sw.Flush();
					cms.Position = 0;
					list.Add(cms);
				}
				else
				{
					do
					{
						sw.Write((char)cur);
						cur = reader.Read();
					} while (cur != -1 && cur != ',' && cur != '}');
					sw.Flush();
					cms.Position = 0;
					if (cms.Matches(NULL))
					{
						list.Add(null);
						cms.Dispose();
					}
					else
						list.Add(cms);
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static int SerializeCompositeURI(string value, char[] buf, int pos)
		{
			foreach (var c in value)
			{
				if (c == '\\' || c == '/')
					buf[pos++] = '\\';
				buf[pos++] = c;
			}
			return pos;
		}
	}
}
