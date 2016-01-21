using System;
using System.Collections.Concurrent;
using System.Diagnostics.Contracts;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Postgres
{
	public class SqlSourceAttribute : Attribute
	{
		public string SqlSource { get; private set; }

		public SqlSourceAttribute(string source)
		{
			Contract.Requires(source != null);

			this.SqlSource = source;
		}

		private static ConcurrentDictionary<Type, string> SourceTypes = new ConcurrentDictionary<Type, string>(1, 127);
		//TODO move to another place... add repository check
		public static string FindSource(Type type)
		{
			string source;
			if (!SourceTypes.TryGetValue(type, out source))
			{
				var attr = type.GetCustomAttributes(typeof(SqlSourceAttribute), false) as SqlSourceAttribute[];
				if (attr != null && attr.Length == 1)
					source = SourceTypes[type] = attr[0].SqlSource;
				else if (typeof(IAggregateRoot).IsAssignableFrom(type))
					source = SourceTypes[type] = "\"{0}\".\"{1}_entity\"".With(type.Namespace, type.Name);
				else if (typeof(IDomainEvent).IsAssignableFrom(type))
					source = SourceTypes[type] = "\"{0}\".\"{1}_event\"".With(type.Namespace, type.Name);
				else if (typeof(IIdentifiable).IsAssignableFrom(type))
					source = SourceTypes[type] = "\"{0}\".\"{1}\"".With(type.Namespace, type.Name);
				//TODO cleanup
				else if (typeof(IEntity).IsAssignableFrom(type))
					source = SourceTypes[type] = "\"{0}\".\"{1}_entity\"".With(type.Namespace, type.Name);
				//PERF: lets cache everything
				else
					source = SourceTypes[type] = null;
			}
			return source;
		}
	}
}
