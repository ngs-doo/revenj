using System;
using System.Collections.Generic;
using System.Reflection;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IPostgresConverterRepository
	{
		void RegisterConverter(Type type, IPostgresTypeConverter converter);
		void CustomizeName(Type type, string property, string name);
	}

	public interface IPostgresConverterFactory
	{
		Func<object, BufferedTextReader, IServiceProvider, object> GetInstanceFactory(Type type);
		Func<object, string> GetSerializationFactory(Type type);
		string GetName(MemberInfo property);
	}

	internal class PostgresObjectFactory : IPostgresConverterRepository, IPostgresConverterFactory
	{
		private Dictionary<Type, IPostgresTypeConverter> TypeConverters = new Dictionary<Type, IPostgresTypeConverter>();
		private Dictionary<MemberInfo, string> CustomNames = new Dictionary<MemberInfo, string>();

		public void RegisterConverter(Type type, IPostgresTypeConverter converter)
		{
			TypeConverters[type] = converter;
		}

		public void CustomizeName(Type type, string property, string name)
		{
			var pi = type.GetProperty(property);
			if (pi == null) throw new ArgumentException("Unable to find property {0} in type {1}.".With(property, type));
			CustomNames[pi] = name;
			foreach (var iface in type.GetInterfaces())
			{
				var ii = iface.GetProperty(property);
				if (ii == null) continue;
				//TODO: two interfaces can implement same name. can't really know which one it is
				CustomNames[ii] = name;
			}
		}

		public Func<object, BufferedTextReader, IServiceProvider, object> GetInstanceFactory(Type type)
		{
			IPostgresTypeConverter converter;
			if (TypeConverters.TryGetValue(type, out converter))
				return converter.CreateInstance;
			return null;
		}

		public Func<object, string> GetSerializationFactory(Type type)
		{
			IPostgresTypeConverter converter;
			if (TypeConverters.TryGetValue(type, out converter))
				return converter.CreateRecord;
			return null;
		}

		public string GetName(MemberInfo property)
		{
			string name;
			if (CustomNames.TryGetValue(property, out name))
				return name;
			return property.Name;
		}
	}
}
