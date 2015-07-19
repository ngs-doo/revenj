using System;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
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
	[ExportMetadata(Metadata.ClassType, typeof(CreateReport))]
	public class CreateReport : IReadOnlyServerCommand
	{
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public CreateReport(
			IDomainModel domainModel,
			IPermissionManager permissions)
		{
			Contract.Requires(domainModel != null);
			Contract.Requires(permissions != null);

			this.DomainModel = domainModel;
			this.Permissions = permissions;
		}

		[DataContract(Namespace = "")]
		public class Argument<TFormat>
		{
			[DataMember]
			public TFormat Data;
			[DataMember]
			public string ReportName;
			[DataMember]
			public string TemplaterName;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { ReportName = "Module.Report", TemplaterName = "CreatePdf" });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(
			IServiceProvider locator,
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var reportType = DomainModel.Find(argument.ReportName);
			if (reportType == null)
				return CommandResult<TOutput>.Fail(
					"Couldn't find report type {0}.".With(argument.ReportName),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			var ri = reportType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IReport<>));
			if (ri == null)
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an report. 
Please check your arguments.".With(argument.ReportName), null);

			if (!Permissions.CanAccess(reportType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.ReportName);

			var documentType = DomainModel.FindNested(argument.ReportName, argument.TemplaterName);
			if (documentType == null)
				return
					CommandResult<TOutput>.Fail(
						"Couldn't find Templater type {0} for {1}.".With(argument.TemplaterName, argument.ReportName),
						@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!Permissions.CanAccess(documentType))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0} in {1}.",
						argument.TemplaterName,
						argument.ReportName);

			if (!typeof(IDocumentReport<>).MakeGenericType(reportType).IsAssignableFrom(documentType))
				return
					CommandResult<TOutput>.Fail(
						"Templater type {0} for {1} is not IDocumentReport<{1}>. Check {0}.".With(
							documentType.FullName,
							reportType.FullName),
						null);

			try
			{
				var commandType = typeof(GenerateReportCommand<,>).MakeGenericType(reportType, ri.GetGenericArguments()[0]);
				var command = Activator.CreateInstance(commandType) as IGenerateReport;
				var result = command.Convert(input, locator, documentType, argument.Data);

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

		private interface IGenerateReport
		{
			Stream Convert<TFormat>(ISerialization<TFormat> serializer, IServiceProvider locator, Type documentType, TFormat data);
		}

		private class GenerateReportCommand<TReport, TResult> : IGenerateReport
			where TReport : IReport<TResult>
		{
			public Stream Convert<TFormat>(ISerialization<TFormat> serializer, IServiceProvider locator, Type documentType, TFormat data)
			{
				TReport report;
				try
				{
					report = data != null ? serializer.Deserialize<TFormat, TReport>(data, locator) : Activator.CreateInstance<TReport>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Error deserializing report", ex);
				}
				IDocumentReport<TReport> reportGenerator;
				try
				{
					reportGenerator = locator.Resolve<IDocumentReport<TReport>>(documentType);
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Can't create report. Is report {0} registered in system?".With(documentType.FullName), ex);
				}
				return reportGenerator.Create(report);
			}
		}
	}
}
