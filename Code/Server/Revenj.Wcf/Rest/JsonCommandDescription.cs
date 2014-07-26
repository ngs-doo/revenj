using System;
using System.Collections.Specialized;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class JsonCommandDescription : IServerCommandDescription<StreamReader>
	{
		public string RequestID { get; private set; }
		public StreamReader Data { get; set; }
		public Type CommandType { get; private set; }

		public JsonCommandDescription(NameValueCollection args, Stream message, Type commandType)
		{
			this.CommandType = commandType;
			if (message != null)
			{
				Data = new StreamReader(message, Encoding.UTF8);
			}
			else if (args != null && args.Count > 0)
			{
				const string start = @"{
	";
				const string end = @"
}";
				var properties =
					from key in args.AllKeys
					let val = args[key]
					let isArr = val.Contains(',')
					let arrVal = isArr ? string.Join(",", val.Split(',').Where(it => it.Length > 0).Select(it => "\"{0}\"".With(it))) : null
					select "\"{0}\": ".With(key) + (isArr ? "[" + arrVal + "]" : "\"{0}\"".With(val));
				var ms = new MemoryStream(Encoding.UTF8.GetBytes(start + string.Join(@",
	", properties) + end));
				Data = new StreamReader(ms);
			}
		}
	}
}