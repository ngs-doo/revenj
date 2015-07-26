namespace Revenj.Plugins.Rest.Commands
{
	/// <summary>
	/// Supported message formats
	/// </summary>
	public enum MessageFormat
	{
		/// <summary>
		/// Extensible Markup Language
		/// </summary>
		Xml = 0,
		/// <summary>
		/// Javascript object notation
		/// </summary>
		Json = 1,
		/// <summary>
		/// Protocol buffer
		/// </summary>
		ProtoBuf = 2
	}
}
