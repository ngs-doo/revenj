using System;
using System.Linq;
using System.Xml.Linq;
using Revenj.Processing;

namespace Revenj.Wcf
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

		public XmlCommandDescription(string[] args, Type commandType)
		{
			this.CommandType = commandType;
			if (args != null && args.Length > 0)
			{
				var xml =
					new XElement(XName.Get(commandType.Name),
						new XAttribute(XNamespace.Xmlns.GetName("i"), "http://www.w3.org/2001/XMLSchema-instance"));
				var properties =
					from a in args
					let splt = a.Split('=')
					where splt.Length == 2
					let isArr = splt[1].Contains(',')
					let vals = splt[1].Split(',').Where(it => it.Length > 0).ToArray()
					orderby splt[0]
					select new { Property = splt[0], IsArray = isArr, ArrayValues = vals, Value = splt[1] };
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