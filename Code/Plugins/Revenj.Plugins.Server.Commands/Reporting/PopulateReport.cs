using System;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Security.Principal;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(PopulateReport))]
	public class PopulateReport : IReadOnlyServerCommand
	{
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public PopulateReport(
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
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { ReportName = "Module.Report" });
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(
			IServiceProvider locator,
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			IPrincipal principal,
			TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;
			var argument = either.Argument;

			var reportType = DomainModel.Find(argument.ReportName);
			if (reportType == null)
			{
				return CommandResult<TOutput>.Fail(
					"Couldn't find report type {0}.".With(argument.ReportName),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));
			}
			var ri = reportType.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IReport<>));
			if (ri == null)
			{
				return CommandResult<TOutput>.Fail(@"Specified type ({0}) is not an report. 
Please check your arguments.".With(argument.ReportName), null);
			}
			if (!Permissions.CanAccess(reportType.FullName, principal))
			{
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.ReportName);
			}
			try
			{
				var commandType = typeof(PopulateReportCommand<,>).MakeGenericType(reportType, ri.GetGenericArguments()[0]);
				var command = Activator.CreateInstance(commandType) as IPopulateReport;
				var result = command.Populate(input, output, locator, argument.Data);

				return CommandResult<TOutput>.Success(result, "Report populated");
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

		private interface IPopulateReport
		{
			TOutput Populate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data);
		}

		private class PopulateReportCommand<TReport, TResult> : IPopulateReport
			where TReport : IReport<TResult>
		{
			public TOutput Populate<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IServiceProvider locator,
				TInput data)
			{
				TReport report;
				try
				{
					report = data != null ? input.Deserialize<TInput, TReport>(data, locator) : Activator.CreateInstance<TReport>();
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Error deserializing report.", ex);
				}
				var result = report.Populate(locator);
				return output.Serialize(result);
			}
		}
	}
}
