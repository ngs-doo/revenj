using System;

namespace Revenj.Extensibility
{
	public interface IAspectComposer : IDisposable
	{
		object Create(Type type, object[] args, Type[] services);

		IAspectComposer CreateInnerComposer();
	}

	public static class AspectComposerHelper
	{
		public static TIf Create<TImp, TIf>(this IAspectComposer composer)
			where TImp : class, TIf
		{
			return (TIf)composer.Create(typeof(TImp), null, new[] { typeof(TImp), typeof(TIf) });
		}
	}
}
