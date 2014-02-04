using NGS.Serialization;

namespace Revenj.Plugins.Server.Commands.Transformations
{
	public class GetFromUri<TInput, TOutput> : ITransformation<TInput, TOutput>
	{
		public string Name { get; set; }

		public TOutput Transform(ISerialization<TInput> input, ISerialization<TOutput> output, TInput value)
		{
			var arg = input.Deserialize<TInput, string[]>(value);
			return output.Serialize(new GetDomainObject.Argument
			{
				Name = Name,
				Uri = arg
			});
		}

		public static ITransformation<TInput, TOutput> Create(string name)
		{
			return new GetFromUri<TInput, TOutput> { Name = name };
		}
	}
}
