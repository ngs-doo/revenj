using System;

namespace Revenj.DomainPatterns
{
	public interface IReport<TData>
	{
		TData Populate(IServiceProvider locator);
	}
}
