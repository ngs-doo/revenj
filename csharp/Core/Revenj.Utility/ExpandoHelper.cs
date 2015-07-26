using System;
using System.Collections.Generic;
using System.Dynamic;

namespace Revenj.Utility
{
	/// <summary>
	/// Utilities for expando object.
	/// </summary>
	public static class ExpandoHelper
	{
		/// <summary>
		/// Use dynamic syntax with ExpandoObject.
		/// </summary>
		/// <param name="eo">expando object</param>
		/// <param name="initialize">apply actions</param>
		/// <returns>self</returns>
		public static ExpandoObject With(this ExpandoObject eo, Action<dynamic> initialize)
		{
			initialize(eo);
			return eo;
		}
		/// <summary>
		/// Convert object to expando object.
		/// Copy properties from object using reflection.
		/// </summary>
		/// <param name="item">source instance</param>
		/// <returns>expando clone</returns>
		public static ExpandoObject AsExpando(this object item)
		{
			var dictionary = new ExpandoObject() as IDictionary<string, object>;
			foreach (var propertyInfo in item.GetType().GetProperties())
				dictionary.Add(propertyInfo.Name, propertyInfo.GetValue(item, null));
			return (ExpandoObject)dictionary;
		}
	}
}
