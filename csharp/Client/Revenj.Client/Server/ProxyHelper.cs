using Revenj.DomainPatterns;

namespace Revenj
{
	class ProxyHelper
	{
		public static string GetSpecificationDomainName<T>(ISpecification<T> specification)
			where T : class, ISearchable
		{
			var domainName = typeof(T).FullName;
			var specType = specification.GetType();

			return specType.FullName.StartsWith(domainName)
				? specType.Name
				: specType.FullName;
		}
	}
}
