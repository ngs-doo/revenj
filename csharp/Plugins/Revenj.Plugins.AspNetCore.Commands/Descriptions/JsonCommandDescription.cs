using System;
using System.IO;
using System.Linq;
using System.Text;
using Microsoft.AspNetCore.Http;
using Revenj.Processing;
using Revenj.Utility;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class JsonCommandDescription : IServerCommandDescription<TextReader>
	{
		public string RequestID { get; private set; }
		public TextReader Data { get; set; }
		public Type CommandType { get; private set; }

		public JsonCommandDescription(IQueryCollection args, Stream message, Type commandType)
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
					from kv in args
					let key = kv.Key
					let val = kv.Value
					let arrVal = val.Count > 1 ? string.Join(",", val.Where(it => it.Length > 0).Select(it => "\"{0}\"".With(it))) : null
					select "\"{0}\": ".With(key) + (val.Count > 1 ? "[" + arrVal + "]" : "\"{0}\"".With(val));
				Data = new StringReader(start + string.Join(@",
	", properties) + end);
			}
		}
	}
}