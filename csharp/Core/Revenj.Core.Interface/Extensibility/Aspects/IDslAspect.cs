using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// DSL aspects will be resolved during system startup.
	/// Aspects will be registered for interception.
	/// In production mode this aspects will not be loaded.
	/// </summary>
	[ContractClass(typeof(DomainAspectContract))]
	public interface IDslAspect
	{
		/// <summary>
		/// Register aspect into the system during system startup.
		/// </summary>
		/// <param name="aspects">aspect management service</param>
		void Register(IAspectRegistrator aspects);
	}

	[ContractClassFor(typeof(IDslAspect))]
	internal sealed class DomainAspectContract : IDslAspect
	{
		public void Register(IAspectRegistrator aspectRegistrator)
		{
			Contract.Requires(aspectRegistrator != null);
		}
	}
}
