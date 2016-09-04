using System;
using System.Collections.Generic;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IPostgresConverterRepository
	{
		void RegisterConverter(Type type, IPostgresTypeConverter converter);
	}

	public interface IPostgresConverterFactory
	{
		Func<object, BufferedTextReader, IServiceProvider, object> GetInstanceFactory(Type type);
		Func<object, string> GetSerializationFactory(Type type);
	}

	internal class PostgresObjectFactory : IPostgresConverterRepository, IPostgresConverterFactory
	{
		private Dictionary<Type, IPostgresTypeConverter> TypeConverters = new Dictionary<Type, IPostgresTypeConverter>();

		public void RegisterConverter(Type type, IPostgresTypeConverter converter)
		{
			TypeConverters[type] = converter;
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
	}
}
