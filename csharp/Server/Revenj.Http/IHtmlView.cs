using System.IO;

namespace Revenj.Http
{
	public interface IHtmlView
	{
		void Render(TextWriter writer);
	}
}
