using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using Revenj.Extensibility;

namespace Revenj.DomainPatterns
{
	internal class DomainTypeResolver : ITypeResolver
	{
		private readonly ISystemState State;
		private readonly Lazy<IDomainModel> LazyDom;
		private readonly ConcurrentDictionary<string, Type> Cache = new ConcurrentDictionary<string, Type>(1, 127);

		public DomainTypeResolver(
			Lazy<IDomainModel> lazyDom,
			ISystemState state)
		{
			Contract.Requires(lazyDom != null);
			Contract.Requires(state != null);

			this.LazyDom = lazyDom;
			this.State = state;
		}

		public Type Resolve(string name)
		{
			Type found;
			if (!Cache.TryGetValue(name, out found))
			{
				found = ResolveType(name, false);
				if (found != null)
					Cache.TryAdd(name, found);
			}
			return found;
		}

		private Type ResolveType(string name, bool useShortName)
		{
			var type =
				(State.IsBooting ? null : LazyDom.Value.Find(name))
				?? Type.GetType(name)
				?? (from asm in Utility.AssemblyScanner.GetAssemblies()
					let asmType = asm.GetType(name)
					where asmType != null
					&& (asmType.IsPublic || asmType.IsNestedPublic)
					select asmType).FirstOrDefault()
				?? TryToResolveTypeWithoutStrongName(name);
			if (type == null && useShortName && name.Contains(","))
				return ResolveType(name.Substring(0, name.IndexOf(",")), false);
			return type;
		}

		private Type TryToResolveTypeWithoutStrongName(string name)
		{
			var first = name.IndexOf("[[");
			if (first < 1)
				return null;
			var last = name.LastIndexOf("]]");
			var mainTypeName = name.Substring(0, first) + name.Substring(last + 2);
			var mainType = Resolve(mainTypeName);
			if (mainType == null)
				return null;
			var subTypeNames = name.Substring(first + 2, last - first - 2).Split(new[] { "],[" }, StringSplitOptions.None);
			var list = new List<Type>();
			var dict = new Dictionary<string, Type>();
			foreach (var subName in subTypeNames)
			{
				if (dict.ContainsKey(subName))
					list.Add(dict[subName]);
				else
				{
					var genericType = ResolveType(subName, true);
					if (genericType == null)
						return null;
					list.Add(genericType);
					dict[subName] = genericType;
				}
			}
			if (mainType.IsGenericType)
				return mainType.MakeGenericType(list.ToArray());
			if (mainType.IsArray)
			{
				var elementType = mainType.GetElementType();
				if (elementType.IsGenericType)
					return elementType.MakeGenericType(list.ToArray()).MakeArrayType();
			}
			return mainType;
		}
	}
}
