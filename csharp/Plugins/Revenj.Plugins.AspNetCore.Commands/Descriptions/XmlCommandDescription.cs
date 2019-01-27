using System;
using System.Linq;
using System.Xml.Linq;
using Microsoft.AspNetCore.Http;
using Revenj.Processing;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class XmlCommandDescription : IServerCommandDescription<XElement>
	{
		public string RequestID { get; private set; }
		public XElement Data { get; set; }
		public Type CommandType { get; private set; }

		public XmlCommandDescription(XElement data, Type commandType)
		{
			this.CommandType = commandType;
			this.Data = data;
		}

		public XmlCommandDescription(IQueryCollection args, Type commandType)
		{
			this.CommandType = commandType;
			if (args != null && args.Count > 0)
			{
				var xml =
					new XElement(XName.Get(commandType.Name),
						new XAttribute(XNamespace.Xmlns.GetName("i"), "http://www.w3.org/2001/XMLSchema-instance"));
				var properties =
					from kv in args
					let key = kv.Key
					let val = kv.Value
					let arrVal = val.Count > 1 ? val.Where(it => it.Length > 0).ToArray() : null
					orderby key
					select new { Property = key, IsArray = val.Count > 1, ArrayValues = arrVal, Value = val[0] };
				foreach (var p in properties)
				{
					if (!p.IsArray)
						xml.Add(new XElement(XName.Get(p.Property), p.Value));
					else
					{
						XNamespace d2p1 = "http://schemas.microsoft.com/2003/10/Serialization/Arrays";
						var array =
							new XElement(XName.Get(p.Property),
								new XAttribute(XNamespace.Xmlns.GetName("d2p1"), "http://schemas.microsoft.com/2003/10/Serialization/Arrays"));
						xml.Add(array);
						foreach (var it in p.ArrayValues)
							array.Add(new XElement(d2p1 + "string", it));
					}
				}
				this.Data = xml;
			}
		}
	}
}