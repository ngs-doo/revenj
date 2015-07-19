using System.Diagnostics.Contracts;
using System.Runtime.Serialization;

namespace Revenj.Serialization
{
	/// <summary>
	/// Generic serialization interface. 
	/// Use TFormat to specify serialization type (example: ISerialization&lt;string&gt; for JSON serialization).
	/// During deserialization provide IServiceProvider to context information when required
	/// </summary>
	/// <typeparam name="TFormat">define serialization type</typeparam>
	[ContractClass(typeof(SerializationContract<>))]
	public interface ISerialization<TFormat>
	{
		/// <summary>
		/// Serialize object to appropriate format (XElement, StreamReader, MemoryStream...)
		/// </summary>
		/// <typeparam name="T">argument type. It will be inferred</typeparam>
		/// <param name="value">argument for serialization</param>
		/// <returns>serialized representation of provided argument</returns>
		TFormat Serialize<T>(T value);
		/// <summary>
		/// Deserialize object from provided format. 
		/// Provide context to initialize object and references after deserialization
		/// </summary>
		/// <typeparam name="T">Specify object type</typeparam>
		/// <param name="data">serialized object in provided format</param>
		/// <param name="context">context info for initialization after deserialization</param>
		/// <returns>deserialized object</returns>
		T Deserialize<T>(TFormat data, StreamingContext context);
	}

	[ContractClassFor(typeof(ISerialization<>))]
	internal sealed class SerializationContract<TFormat> : ISerialization<TFormat>
	{
		public TFormat Serialize<T>(T value)
		{
			return default(TFormat);
		}
		public T Deserialize<T>(TFormat data, StreamingContext context)
		{
			Contract.Requires(data != null);

			return default(T);
		}
	}

	/// <summary>
	/// Helper class for deserialization
	/// </summary>
	public static class SerializationHelper
	{
		/// <summary>
		/// Deserialize object without providing context information.
		/// .NET objects or value objects don't require context so they can be deserialized
		/// without IServiceProvider in context.
		/// </summary>
		/// <typeparam name="TFormat">serialization format</typeparam>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="serialization">serialization service</param>
		/// <param name="data">serialized object in specified format</param>
		/// <returns>deserialized object</returns>
		public static T Deserialize<TFormat, T>(
			this ISerialization<TFormat> serialization,
			TFormat data)
		{
			Contract.Requires(serialization != null);

			return serialization.Deserialize<T>(data, default(StreamingContext));
		}

		/// <summary>
		/// Deserialize object using provided context information.
		/// Context should usually be IServiceLocator, but in special cases can be something else.
		/// </summary>
		/// <typeparam name="TFormat">serialization format</typeparam>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="serialization">serialization service</param>
		/// <param name="data">serialized object in specified format</param>
		/// <param name="context">context information which will be used during deserialization</param>
		/// <returns>deserialized object</returns>
		public static T Deserialize<TFormat, T>(
			this ISerialization<TFormat> serialization,
			TFormat data,
			object context)
		{
			Contract.Requires(serialization != null);

			var sc = new StreamingContext(StreamingContextStates.All, context);
			return serialization.Deserialize<T>(data, sc);
		}
	}
}
