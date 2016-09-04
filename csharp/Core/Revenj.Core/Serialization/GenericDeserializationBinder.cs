using System;
using System.Diagnostics.Contracts;
using System.Runtime.Serialization;
using Revenj.Extensibility;

namespace Revenj.Serialization
{
	internal class GenericDeserializationBinder : SerializationBinder
	{
		private readonly Lazy<ITypeResolver> TypeResolver;

		public GenericDeserializationBinder(Lazy<ITypeResolver> typeResolver)
		{
			Contract.Requires(typeResolver != null);

			this.TypeResolver = typeResolver;
		}

		public override Type BindToType(string assemblyName, string typeName)
		{
			return TypeResolver.Value.Resolve(typeName);
		}

		public override void BindToName(Type serializedType, out string assemblyName, out string typeName)
		{
			assemblyName = null;
			typeName = serializedType.FullName;
		}
	}
}
