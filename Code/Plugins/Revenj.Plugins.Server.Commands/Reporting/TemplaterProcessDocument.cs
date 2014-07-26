using System;
using System.ComponentModel.Composition;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
using System.Runtime.Serialization;
using NGS.Templater;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(TemplaterProcessDocument))]
	public class TemplaterProcessDocument : IReadOnlyServerCommand
	{
		private static readonly string DocumentFolder;
		private static readonly IDocumentFactory TemplaterFactory = NGS.Templater.Configuration.Factory;

		static TemplaterProcessDocument()
		{
			var path = ConfigurationManager.AppSettings["DocumentsPath"];
			DocumentFolder = path == null ? AppDomain.CurrentDomain.BaseDirectory : Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path);
		}

		private readonly SearchDomainObject SearchDomain;
		private readonly GetDomainObject GetDomain;

		public TemplaterProcessDocument(
			SearchDomainObject searchDomain,
			GetDomainObject getDomain)
		{
			Contract.Requires(searchDomain != null);
			Contract.Requires(getDomain != null);

			this.SearchDomain = searchDomain;
			this.GetDomain = getDomain;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat>
		{
			[DataMember]
			public string File;
			[DataMember]
			public SearchDomainObject.Argument<TFormat>[] SearchSources;
			[DataMember]
			public GetDomainObject.Argument[] GetSources;
			[DataMember]
			public bool ToPdf;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return
				serializer.Serialize(
					new Argument<TFormat>
					{
						File = "Document.docx",
						SearchSources = new[] { new SearchDomainObject.Argument<TFormat> { Name = "Module.DataSource" } },
						GetSources = new[] { new GetDomainObject.Argument { Name = "Module.DataSource", Uri = new[] { "1234" } } }
					});
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;

			try
			{
				var result = Execute<TInput>(input, either.Argument);
				return CommandResult<TOutput>.Success(Serialize(output, result), "Document created");
			}
			catch (FileNotFoundException fnf)
			{
				return CommandResult<TOutput>.Fail(fnf.Message, null);
			}
			catch (ArgumentException ex)
			{
				return CommandResult<TOutput>.Fail(
					ex.Message,
					ex.GetDetailedExplanation() + @"
Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
		}

		private static TOutput Serialize<TOutput>(ISerialization<TOutput> serializer, Stream stream)
		{
			if (typeof(TOutput) == typeof(object))
				return serializer.Serialize(stream);

			using (var ms = new MemoryStream())
			{
				stream.CopyTo(ms);
				stream.Dispose();
				return serializer.Serialize(ms.ToArray());
			}
		}

		public Stream Execute<TInput>(ISerialization<TInput> input, Argument<TInput> argument)
		{
			var file = Path.Combine(DocumentFolder, argument.File);
			if (!File.Exists(file))
				throw new FileNotFoundException("Can't locate file: {0}. Check if correct file is specified.".With(argument.File));

			var ext = Path.GetExtension(argument.File);
			var cms = ChunkedMemoryStream.Create();
			using (var fs = new FileStream(file, FileMode.Open, FileAccess.Read))
			using (var document = TemplaterFactory.Open(fs, cms, ext))
			{
				if (argument.GetSources != null)
					foreach (var source in argument.GetSources)
					{
						var found = GetDomain.GetData(source);
						document.Process(found);
					}
				if (argument.SearchSources != null)
					foreach (var source in argument.SearchSources)
					{
						var found = SearchDomain.FindData<TInput>(input, source);
						document.Process(found);
					}
				var specification =
					(from a in (argument.SearchSources ?? new SearchDomainObject.Argument<TInput>[0])
					 where a.Specification != null
					 select a.Specification)
					.FirstOrDefault();
				if (specification != null)
				{
					dynamic filter = input.Deserialize<TInput, dynamic>(specification);
					document.Process(filter);
				}
			}
			cms.Position = 0;
			return argument.ToPdf ? PdfConverter.Convert(cms, ext, true) : cms;
		}
	}
}
