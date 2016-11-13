using System.IO;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	public interface ITemplaterService
	{
		Task<Stream> Populate<T>(string file, T aggregate)
			where T : class, IIdentifiable;
		Task<Stream> Populate<T>(string file, ISpecification<T> specification)
			where T : class, ISearchable;
	}
}
