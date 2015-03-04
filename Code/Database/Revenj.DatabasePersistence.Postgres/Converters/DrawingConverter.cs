using System;
using System.Drawing;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DrawingConverter
	{
		public static IPostgresTuple ToTuple(Point value)
		{
			return new PointTuple(value);
		}

		public static IPostgresTuple ToTuple(PointF value)
		{
			return new PointFTuple(value);
		}

		public static IPostgresTuple ToTuple(RectangleF value)
		{
			return new RectangleFTuple(value);
		}

		class PointTuple : IPostgresTuple
		{
			private readonly Point Value;

			public PointTuple(Point value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write('(');
				sw.Write(Value.X);
				sw.Write(',');
				sw.Write(Value.Y);
				sw.Write(')');
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
					return "'(" + Value.X + "," + Value.Y + ")'";
				return "(" + Value.X + "," + Value.Y + ")";
			}
		}

		class PointFTuple : IPostgresTuple
		{
			private readonly PointF Value;

			public PointFTuple(PointF value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write('(');
				sw.Write(Value.X);
				sw.Write(',');
				sw.Write(Value.Y);
				sw.Write(')');
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
					return "'(" + Value.X + "," + Value.Y + ")'";
				return "(" + Value.X + "," + Value.Y + ")";
			}
		}

		class RectangleFTuple : IPostgresTuple
		{
			private readonly RectangleF Value;

			public RectangleFTuple(RectangleF value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write('(');
				sw.Write(Value.Right);
				sw.Write(',');
				sw.Write(Value.Bottom);
				sw.Write("),(");
				sw.Write(Value.X);
				sw.Write(',');
				sw.Write(Value.Y);
				sw.Write(')');
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}

			public string BuildTuple(bool quote)
			{
				if (quote)
					return "'(" + Value.X + "," + Value.Y + ")'";
				return "(" + Value.X + "," + Value.Y + ")";
			}
		}
	}
}