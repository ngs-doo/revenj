using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Data;
using System.Diagnostics.Contracts;
using System.IO;
using System.Net;
using System.Runtime.Serialization;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(OlapCubeReport))]
	public class OlapCubeReport : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public OlapCubeReport(
			IServiceLocator locator,
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(locator != null);
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

			this.Locator = locator;
			this.DomainModel = domainModel;
			this.Permissions = permissions;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat> : AnalyzeOlapCube.Argument<TFormat>
		{
			[DataMember]
			public string TemplaterName;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			var dictOrder = new Dictionary<string, bool>();
			dictOrder["City"] = true;
			dictOrder["ShopName"] = false;
			return
				serializer.Serialize(
					new Argument<TFormat>
					{
						CubeName = "Module.Cube",
						TemplaterName = "YearlyStatistics",
						SpecificationName = "ByRegion",
						Dimensions = new[] { "ShopName", "City" },
						Facts = new[] { "TotalSum", "DailyAverage" },
						Order = dictOrder
					});
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var documentType = DomainModel.FindNested(argument.CubeName, argument.TemplaterName);
			if (documentType == null)
				return CommandResult<TOutput>.Fail(
					"Couldn't find Templater type {0} for {1}.".With(argument.TemplaterName, argument.CubeName),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!Permissions.CanAccess(documentType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0} in {1}.",
						argument.TemplaterName,
						argument.CubeName);

			if (!typeof(IDocumentReport<DataTable>).IsAssignableFrom(documentType))
				return
					CommandResult<TOutput>.Fail(
						"Templater type {0} for {1} is not IDocumentReport<DataTable>. Check {0}.".With(
							documentType.FullName,
							argument.CubeName),
						null);

			IDocumentReport<DataTable> report;
			try
			{
				report = Locator.Resolve<IDocumentReport<DataTable>>(documentType);
			}
			catch (Exception ex)
			{
				return CommandResult<TOutput>.Fail(
					@"Can't create document report. Is report {0} registered in system?".With(documentType.FullName),
					ex.GetDetailedExplanation());
			}
			try
			{
				var table = AnalyzeOlapCube.PopulateTable(input, output, Locator, DomainModel, argument, Permissions);
				var result = report.Create(table);
				return CommandResult<TOutput>.Return(HttpStatusCode.Created, Serialize(output, result), "Report created");
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
	}
}
