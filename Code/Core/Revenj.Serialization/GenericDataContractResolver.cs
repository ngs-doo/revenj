using System;
using System.Diagnostics.Contracts;
using System.Runtime.Serialization;
using System.Xml;
using Revenj.Extensibility;

namespace Revenj.Serialization
{
	internal class GenericDataContractResolver : DataContractResolver
	{
		private readonly ITypeResolver TypeResolver;

		public GenericDataContractResolver(ITypeResolver typeResolver)
		{
			Contract.Requires(typeResolver != null);

			this.TypeResolver = typeResolver;
		}

		public override Type ResolveName(
			string typeName,
			string typeNamespace,
			Type declaredType,
			DataContractResolver knownTypeResolver)
		{
			string actualTypeName = Uri.UnescapeDataString(typeName.Replace("..", "%"));

			var type = Type.GetType(actualTypeName + ", " + typeNamespace);
			if (type == null)
				type = TypeResolver.Resolve(actualTypeName);
			if (type == null)
				type = TypeResolver.Resolve(actualTypeName + ", " + typeNamespace);
			return type;
		}

		public override bool TryResolveType(
			Type type,
			Type declaredType,
			DataContractResolver knownTypeResolver,
			out XmlDictionaryString typeName,
			out XmlDictionaryString typeNamespace)
		{
			var dict = new XmlDictionary();
			typeName = dict.Add(Uri.EscapeDataString(type.FullName).Replace("%", ".."));
			typeNamespace = dict.Add(type.Namespace);
			return true;
		}
	}
}
