using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Runtime.Serialization;
using System.Security;
using NGS;
using NGS.Common;
using NGS.DomainPatterns;
using NGS.Extensibility;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;
using Revenj.Processing;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(SearchDomainObject))]
	public class SearchDomainObject : IReadOnlyServerCommand
	{
		private static readonly int? MaxSearchLimit;

		static SearchDomainObject()
		{
			int msl;
			if (int.TryParse(ConfigurationManager.AppSettings["MaxSearchLimit"], out msl))
				MaxSearchLimit = msl;
		}

		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public SearchDomainObject(
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
			public string Name;
			[DataMember]
			public string SpecificationName;
			[DataMember]
			public TFormat Specification;
			[DataMember]
			public int? Offset;
			[DataMember]
			public int? Limit;
			[DataMember]
			public Dictionary<string, bool> Order;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return
				serializer.Serialize(
					new Argument<TFormat>
					{
						Name = "Module.Entity",
						Limit = 10,
						SpecificationName = "Module.Entity+FindImportant"
					});
		}

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;

			try
			{
				var result = FindAndReturn(input, output, either.Argument);
				return CommandResult<TOutput>.Success(result.Result, "Found {0} item(s)", result.Count);
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

		private ISearchDomainObjectCommand<object> PrepareCommand(string domainName, string specificationName)
		{
			if (domainName == null)
				throw new ArgumentException("Domain object name not provided.");
			var domainObjectType = DomainModel.Find(domainName);
			if (domainObjectType == null)
				throw new ArgumentException("Couldn't find domain object: {0}".With(domainName));
			if (!typeof(IDataSource).IsAssignableFrom(domainObjectType))
				throw new ArgumentException(@"Specified type ({0}) is not a data source. 
Please check your arguments.".With(domainName));
			if (!Permissions.CanAccess(domainObjectType))
				throw new SecurityException("You don't have permission to access: {0}.".With(domainName));
			if (string.IsNullOrWhiteSpace(specificationName))
			{
				var commandType = typeof(SearchDomainObjectCommand<>).MakeGenericType(domainObjectType);
				return Activator.CreateInstance(commandType) as ISearchDomainObjectCommand<object>;
			}
			else
			{
				var specificationType =
					DomainModel.FindNested(domainName, specificationName)
					?? DomainModel.Find(specificationName);
				if (specificationType == null)
					throw new ArgumentException("Couldn't find specification: {0}".With(specificationName));
				var commandType = typeof(SearchDomainObjectWithSpecificationCommand<,>).MakeGenericType(domainObjectType, specificationType);
				return Activator.CreateInstance(commandType) as ISearchDomainObjectCommand<object>;
			}
		}

		public object[] FindData<TFormat>(ISerialization<TFormat> serializer, Argument<TFormat> argument)
		{
			var command = PrepareCommand(argument.Name, argument.SpecificationName);
			var limit = argument.Limit ?? MaxSearchLimit;
			return
				command.FindBy(
					serializer,
					DomainModel,
					Locator,
					argument.Specification,
					argument.Offset,
					limit,
					argument.Order);
		}

		private class SearchResult<TFormat>
		{
			public TFormat Result;
			public int Count;
		}

		private SearchResult<TOutput> FindAndReturn<TInput, TOutput>(
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			Argument<TInput> argument)
		{
			var command = PrepareCommand(argument.Name, argument.SpecificationName);
			var limit = argument.Limit ?? MaxSearchLimit;
			return
				command.FindAndSerialize(
					input,
					output,
					DomainModel,
					Locator,
					Permissions,
					argument.Specification,
					argument.Offset,
					limit,
					argument.Order);
		}

		private interface ISearchDomainObjectCommand<out TDomainObject>
		{
			TDomainObject[] FindBy<TFormat>(
				ISerialization<TFormat> serializer,
				IDomainModel domainModel,
				IServiceLocator locator,
				TFormat data,
				int? offset,
				int? limit,
				IDictionary<string, bool> order);
			SearchResult<TOutput> FindAndSerialize<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IDomainModel domainModel,
				IServiceLocator locator,
				IPermissionManager permissions,
				TInput data,
				int? offset,
				int? limit,
				IDictionary<string, bool> order);
		}

		private static IQueryableRepository<T> GetRepository<T>(IServiceLocator locator)
			where T : IDataSource
		{
			try
			{
				return locator.Resolve<IQueryableRepository<T>>();
			}
			catch (Exception ex)
			{
				throw new ArgumentException("Can't query {0}. Check if {0} supports querying".With(typeof(T).FullName), ex);
			}
		}

		private static T[] QueryWithConditions<T>(int? offset, int? limit, IDictionary<string, bool> order, IQueryable<T> result)
		{
			if (order != null)
				result = result.OrderBy(order);
			if (offset != null)
				result = result.Skip(offset.Value);
			if (limit != null && limit.Value != int.MaxValue)
				result = result.Take(limit.Value);
			return result.ToArray();
		}

		private class SearchDomainObjectCommand<TDomainObject> : ISearchDomainObjectCommand<TDomainObject>
			where TDomainObject : class, IDataSource
		{
			public virtual TDomainObject[] FindBy<TFormat>(
				ISerialization<TFormat> serializer,
				IDomainModel domainModel,
				IServiceLocator locator,
				TFormat data,
				int? offset,
				int? limit,
				IDictionary<string, bool> order)
			{
				var repository = GetRepository<TDomainObject>(locator);
				IQueryable<TDomainObject> result;
				if (data == null)
					result = repository.Query<TDomainObject>(null);
				else
				{
					dynamic specification;
					try
					{
						specification = serializer.Deserialize<TFormat, dynamic>(data, locator);
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
					try
					{
						result = repository.Query(specification);
					}
					catch (Exception ex)
					{
						throw new ArgumentException(
							"Error while executing query: {0}. ".With(ex.Message),
							new FrameworkException(@"Specification deserialized as: {0}".With((TFormat)serializer.Serialize(specification)), ex));
					}
				}
				return QueryWithConditions<TDomainObject>(offset, limit, order, result);
			}

			public SearchResult<TOutput> FindAndSerialize<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IDomainModel domainModel,
				IServiceLocator locator,
				IPermissionManager permissions,
				TInput data,
				int? offset,
				int? limit,
				IDictionary<string, bool> order)
			{
				var result = FindBy(input, domainModel, locator, data, offset, limit, order);
				var filtered = permissions.ApplyFilters(result);
				return new SearchResult<TOutput> { Result = output.Serialize(filtered), Count = filtered.Length };
			}
		}

		private class SearchDomainObjectWithSpecificationCommand<TDomainObject, TSpecification> : SearchDomainObjectCommand<TDomainObject>
			where TDomainObject : class, IDataSource
		{
			public override TDomainObject[] FindBy<TFormat>(
				ISerialization<TFormat> serializer,
				IDomainModel domainModel,
				IServiceLocator locator,
				TFormat data,
				int? offset,
				int? limit,
				IDictionary<string, bool> order)
			{
				var repository = GetRepository<TDomainObject>(locator);
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
					throw new ArgumentException("Specification could not be deserialized.");
				IQueryable<TDomainObject> result;
				try
				{
					result = repository.Query(specification);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Error while executing query: {0}.".With(ex.Message),
						new FrameworkException(@"Specification deserialized as: {0}".With((TFormat)serializer.Serialize(specification)), ex));
				}
				return QueryWithConditions(offset, limit, order, result);
			}
		}
	}
}
