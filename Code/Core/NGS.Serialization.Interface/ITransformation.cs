namespace NGS.Serialization
{
	/// <summary>
	/// Generic interface for serialization transformation. 
	/// It can be also used for in-serialization transformation, such as XSLT
	/// </summary>
	/// <typeparam name="TInput">input format type</typeparam>
	/// <typeparam name="TOutput">output format type</typeparam>
	public interface ITransformation<TInput, TOutput>
	{
		/// <summary>
		/// Transform provided input value to output result.
		/// </summary>
		/// <param name="input">input serialization</param>
		/// <param name="output">output serialization</param>
		/// <param name="value">input value</param>
		/// <returns>transformed output</returns>
		TOutput Transform(ISerialization<TInput> input, ISerialization<TOutput> output, TInput value);
	}
}
