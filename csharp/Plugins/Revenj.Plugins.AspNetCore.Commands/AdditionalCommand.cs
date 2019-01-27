using System;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class AdditionalCommand
	{
		/// <summary>
		/// Argument for specified command
		/// </summary>
		public object Argument { get; set; }
		/// <summary>
		/// Command type
		/// </summary>
		public Type CommandType { get; set; }
		/// <summary>
		/// Header where to save result
		/// </summary>
		public string ToHeader { get; set; }
	}

}
