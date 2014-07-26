using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Data;
using System.Diagnostics.Contracts;
using System.Net;
using System.Runtime.Serialization;
using System.Security;
using Revenj.Common;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(AnalyzeOlapCube))]
	public class AnalyzeOlapCube : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public AnalyzeOlapCube(
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
		public class Argument<TFormat>
		{
			[DataMember]
			public string CubeName;
			[DataMember]
			public string SpecificationName;
			[DataMember]
			public TFormat Specification;
			[DataMember]
			public string[] Dimensions;
			[DataMember]
			public string[] Facts;
			[DataMember]
			public Dictionary<string, bool> Order;
			[DataMember]
			public bool UseDataTable;
			[DataMember]
			public int? Limit;
			[DataMember]
			public int? Offset;
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

			try
			{
				var table = PopulateTable(input, output, Locator, DomainModel, either.Argument, Permissions);
				if (either.Argument.UseDataTable)
					return CommandResult<TOutput>.Return(HttpStatusCode.Created, output.Serialize(table), "Data analyzed");
				var result = ConvertTable.Convert(output, table);
				return CommandResult<TOutput>.Return(HttpStatusCode.Created, result, "Data analyzed");
			}
			catch (SecurityException ex)
			{
				return CommandResult<TOutput>.Return(HttpStatusCode.Forbidden, default(TOutput), ex.Message);
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

		public static DataTable PopulateTable<TInput, TOutput>(
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			IServiceLocator Locator,
			IDomainModel DomainModel,
			Argument<TInput> argument,
			IPermissionManager Permissions)
		{
			var cubeType = DomainModel.Find(argument.CubeName);
			if (cubeType == null)
				throw new ArgumentException(
					"Couldn't find cube type {0}.".With(argument.CubeName),
					new FrameworkException(@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output))));

			if (!Permissions.CanAccess(cubeType))
				throw new SecurityException("You don't have permission to access: {0}.".With(argument.CubeName));

			if (!typeof(IOlapCubeQuery).IsAssignableFrom(cubeType))
				throw new ArgumentException("Cube type {0} is not IOlapCubeQuery.".With(cubeType.FullName));

			IOlapCubeQuery query;
			try
			{
				query = Locator.Resolve<IOlapCubeQuery>(cubeType);
			}
			catch (Exception ex)
			{
				throw new ArgumentException(
					"Can't create cube query. Is query {0} registered in system?".With(cubeType.FullName),
					ex);
			}

			if (string.IsNullOrEmpty(argument.SpecificationName) && argument.Specification == null)
				return query.Analyze(argument.Dimensions, argument.Facts, argument.Order, argument.Limit, argument.Offset);
			else if (string.IsNullOrEmpty(argument.SpecificationName))
			{
				dynamic specification;
				try
				{
					specification = input.Deserialize<TInput, dynamic>(argument.Specification, Locator);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Specification could not be deserialized.",
						new FrameworkException(@"Please provide specification name. Error: {0}.".With(ex.Message), ex));
				}
				if (specification == null)
					throw new ArgumentException(
						"Specification could not be deserialized.",
						new FrameworkException("Please provide specification name."));
				return query.Analyze(argument.Dimensions, argument.Facts, argument.Order, specification, argument.Limit, argument.Offset);
			}
			else
			{
				var specificationType =
					DomainModel.FindNested(argument.CubeName, argument.SpecificationName)
					?? DomainModel.Find(argument.SpecificationName);
				if (specificationType == null)
					throw new ArgumentException("Couldn't find specification: {0}".With(argument.SpecificationName));
				var commandType = typeof(AnalyzeWithSpecification<>).MakeGenericType(specificationType);
				var command = (IAnalyzeData)Activator.CreateInstance(commandType);
				return
					command.Analyze(
						input,
						Locator,
						query,
						argument.Dimensions,
						argument.Facts,
						argument.Order,
						argument.Limit,
						argument.Offset,
						argument.Specification);
			}
		}

		private static class ConvertTable
		{
			public static TFormat Convert<TFormat>(ISerialization<TFormat> serializer, DataTable table)
			{
				var result = new Dictionary<string, object>[table.Rows.Count];
				for (int i = 0; i < table.Rows.Count; i++)
				{
					var row = table.Rows[i];
					var item = new Dictionary<string, object>();
					//TODO DateTime format serizalization problem - use dynamic instead of object!?
					foreach (DataColumn c in table.Columns)
						item[c.ColumnName] = row[c.ColumnName];
					result[i] = item;
				}
				return serializer.Serialize(result);
			}
		}

		private interface IAnalyzeData
		{
			DataTable Analyze<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceLocator locator,
				IOlapCubeQuery query,
				IEnumerable<string> dimensions,
				IEnumerable<string> facts,
				IDictionary<string, bool> order,
				int? limit,
				int? offset,
				TFormat data);
		}

		private class AnalyzeWithSpecification<TSpecification> : IAnalyzeData
		{
			public DataTable Analyze<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceLocator locator,
				IOlapCubeQuery query,
				IEnumerable<string> dimensions,
				IEnumerable<string> facts,
				IDictionary<string, bool> order,
				int? limit,
				int? offset,
				TFormat data)
			{
				dynamic specification;
				if (data == null)
				{
					try
					{
						specification = Activator.CreateInstance(typeof(TSpecification));
					}
					catch (Exception ex)
					{
						throw new ArgumentException("Specification can't be created. It must be sent as argument.", ex);
					}
				}
				else
				{
					try
					{
						specification = serializer.Deserialize<TFormat, TSpecification>(data, locator);
					}
					catch (Exception ex)
					{
						throw new ArgumentException("Specification could not be deserialized.", ex);
					}
				}
				if (specification == null)
					throw new FrameworkException("Specification could not be deserialized.");
				return query.Analyze(dimensions, facts, order, specification, limit, offset);
			}
		}
	}
}
