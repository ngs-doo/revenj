using System;
using System.IO;
using System.Linq;
using System.Text;
using NGS;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class JsonCommandDescription : IServerCommandDescription<StreamReader>
	{
		public string RequestID { get; private set; }
		public StreamReader Data { get; set; }
		public Type CommandType { get; private set; }

		public JsonCommandDescription(string[] args, Stream message, Type commandType)
		{
			this.CommandType = commandType;
			if (message != null)
			{
				Data = new StreamReader(message, Encoding.UTF8);
			}
			else if (args != null && args.Length > 0)
			{
				const string start = @"{
	";
				const string end = @"
}";
				var properties =
					from a in args
					let splt = a.Split('=')
					where splt.Length == 2
					let isArr = splt[1].Contains(',')
					let vals = splt[1].Split(',').Where(it => it.Length > 0).Select(it => "\"{0}\"".With(it))
					select "\"{0}\": ".With(splt[0]) + (isArr ? "[" + string.Join(",", vals) + "]" : "\"{0}\"".With(splt[1]));
				var ms = new MemoryStream(Encoding.UTF8.GetBytes(start + string.Join(@",
	", properties) + end));
				Data = new StreamReader(ms);
			}
		}
	}
}