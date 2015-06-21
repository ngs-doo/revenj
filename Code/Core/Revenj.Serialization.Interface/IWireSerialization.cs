using System;
using System.Diagnostics.Contracts;
using System.IO;
using System.Runtime.Serialization;

namespace Revenj.Serialization
{
	/// <summary>
	/// Serialization interface for on and from the wire communication. 
	/// Specify accept/contentType for appropriate serialization.
	/// During deserialization provide IServiceLocator to context information when required
	/// </summary>
	public interface IWireSerialization
	{
		/// <summary>
		/// Serialize object to appropriate format XML, JSON, Protocol buffer within the stream
		/// </summary>
		/// <param name="value">argument for serialization</param>
		/// <param name="accept">allowed formats</param>
		/// <param name="destination">serialization destination</param>
		/// <returns>serialization format</returns>
		string Serialize(object value, string accept, Stream destination);
		/// <summary>
		/// Deserialize object from provided format. 
		/// Provide context to initialize object and references after deserialization
		/// </summary>
		/// <param name="source">raw data in specified format</param>
		/// <param name="target">object type</param>
		/// <param name="contentType">specified serialization format</param>
		/// <param name="context">context info for initialization after deserialization</param>
		/// <returns>deserialized object</returns>
		object Deserialize(Stream source, Type target, string contentType, StreamingContext context);
		/// <summary>
		/// Get serializer for specified format (eg. XElement for XML, string for JSON, etc...)
		/// </summary>
		/// <typeparam name="TFormat">serializer format</typeparam>
		/// <returns>serializer instance</returns>
		ISerialization<TFormat> GetSerializer<TFormat>();
	}

	/// <summary>
	/// Helper class for deserialization
	/// </summary>
	public static class WireSerializationHelper
	{
		/// <summary>
		/// Deserialize typesafe object without providing context information.
		/// .NET objects or value objects don't require context so they can be deserialized
		/// without IServiceLocator in context.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="serialization">serialization service</param>
		/// <param name="source">serialized object in specified format</param>
		/// <param name="contentType">MIME type specifying the format</param>
		/// <returns>deserialized object</returns>
		public static T Deserialize<T>(
			this IWireSerialization serialization,
			Stream source,
			string contentType)
		{
			Contract.Requires(serialization != null);

			return (T)serialization.Deserialize(source, typeof(T), contentType, default(StreamingContext));
		}
	}
}
