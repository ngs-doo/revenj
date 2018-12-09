namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Generic handler signature. Used for handling (processing) an object.
	/// With appropriate signature, it can be used for collection processing (array),
	/// lazy processing (lazy),
	/// future processing (task)
	/// </summary>
	/// <typeparam name="T">t</typeparam>
	public interface IHandler<T>
	{
		void Handle(T t);
	}
}
