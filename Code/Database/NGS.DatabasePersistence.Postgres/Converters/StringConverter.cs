using System.Collections.Generic;
using System.IO;
using System.Text;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class StringConverter
	{
		public static void Skip(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return;
			if (cur != '"' && cur != '\\')
			{
				while (cur != -1 && cur != ',' && cur != ')')
					cur = reader.Read();
			}
			else
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
				while (cur != -1)
				{
					if (cur == '\\' || cur == '"')
					{
						for (int i = 0; i < context; i++)
							cur = reader.Read();
						if (cur == ',' || cur == ')')
							return;
						for (int i = 0; i < context - 1; i++)
							cur = reader.Read();
					}
					cur = reader.Read();
				}
				for (int i = 0; i < context; i++)
					reader.Read();
			}
		}

		public static string Parse(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var sb = new StringBuilder();
			if (cur != '"' && cur != '\\')
			{
				while (cur != -1 && cur != ',' && cur != ')')
				{
					sb.Append((char)cur);
					cur = reader.Read();
				}
			}
			else
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
				while (cur != -1)
				{
					if (cur == '\\' || cur == '"')
					{
						for (int i = 0; i < context; i++)
							cur = reader.Read();
						if (cur == ',' || cur == ')')
							return sb.ToString();
						for (int i = 0; i < context - 1; i++)
							cur = reader.Read();
					}
					sb.Append((char)cur);
					cur = reader.Read();
				}
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			return sb.ToString();
		}

		public static Stream ParseStream(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var cms = ChunkedMemoryStream.Create();
			var sw = new StreamWriter(cms);
			if (cur != '"' && cur != '\\')
			{
				while (cur != -1 && cur != ',' && cur != ')')
				{
					sw.Write((char)cur);
					cur = reader.Read();
				}
			}
			else
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
				while (cur != -1)
				{
					if (cur == '\\' || cur == '"')
					{
						for (int i = 0; i < context; i++)
							cur = reader.Read();
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
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		public static List<string> ParseCollection(TextReader reader, int context, bool allowNull)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<string>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				var sb = new StringBuilder();
				if (cur == '"' || cur == '\\')
				{
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							for (int i = 0; i < innerContext; i++)
								cur = reader.Read();
							if (cur == ',' || cur == '}')
								break;
							for (int i = 0; i < innerContext - 1; i++)
								cur = reader.Read();
						}
						sb.Append((char)cur);
						cur = reader.Read();
					}
					list.Add(sb.ToString());
				}
				else
				{
					do
					{
						sb.Append((char)cur);
						cur = reader.Read();
					} while (cur != -1 && cur != ',' && cur != '}');
					var val = sb.ToString();
					if (val == "NULL")
						list.Add(allowNull ? null : string.Empty);
					else
						list.Add(val);
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static List<Stream> ParseStreamCollection(TextReader reader, int context, bool allowNull)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<Stream>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				var cms = ChunkedMemoryStream.Create();
				var sw = new StreamWriter(cms);
				if (cur == '"' || cur == '\\')
				{
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							for (int i = 0; i < innerContext; i++)
								cur = reader.Read();
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
					if (cms.IsNull())
						list.Add(null);
					else
						list.Add(cms);
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}
	}
}
