using System;
using System.Collections.Specialized;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.Processing;
using Revenj.Utility;

namespace Revenj.Wcf
{
	public class JsonCommandDescription : IServerCommandDescription<TextReader>
	{
		public string RequestID { get; private set; }
		public TextReader Data { get; set; }
		public Type CommandType { get; private set; }

		public JsonCommandDescription(NameValueCollection args, Stream message, Type commandType)
		{
			this.CommandType = commandType;
			if (message != null)
			{
				var cms = message as ChunkedMemoryStream;
				if (cms != null)
					Data = cms.GetReader();
				else
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
				Data = new StringReader(start + string.Join(@",
	", properties) + end);
			}
		}
	}
}