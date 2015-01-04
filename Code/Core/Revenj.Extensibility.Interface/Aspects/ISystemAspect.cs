using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// System aspects will be resolved during system startup.
	/// Services can configure system behavior.
	/// </summary>
	[ContractClass(typeof(SystemAspectContract))]
	public interface ISystemAspect
	{
		/// <summary>
		/// Initialize aspect and provide system scope
		/// </summary>
		/// <param name="factory">system scope</param>
		void Initialize(IObjectFactory factory);
	}

	[ContractClassFor(typeof(ISystemAspect))]
	internal sealed class SystemAspectContract : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			Contract.Requires(factory != null);
		}
	}
}
