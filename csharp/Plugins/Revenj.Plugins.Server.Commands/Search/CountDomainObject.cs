using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Runtime.Serialization;
using System.Security.Principal;
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
	[ExportMetadata(Metadata.ClassType, typeof(CountDomainObject))]
	public class CountDomainObject : IReadOnlyServerCommand
	{
		private static Dictionary<Type, ICountCommand> Cache = new Dictionary<Type, ICountCommand>();

		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public CountDomainObject(
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
			public string Name;
			[DataMember]
			public string SpecificationName;
			[DataMember]
			public TFormat Specification;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return
				serializer.Serialize(
					new Argument<TFormat>
					{
						Name = "Module.Entity",
						SpecificationName = "Module.Entity+FindImportant"
					});
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

			try
			{
				var command = PrepareCommand(principal, either.Argument.Name, either.Argument.SpecificationName);
				var count = command.FindCount(input, locator, either.Argument.Specification);
				return CommandResult<TOutput>.Success(output.Serialize(count), count.ToString());
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

		private ICountCommand PrepareCommand(IPrincipal principal, string domainName, string specificationName)
		{
			var domainObjectType = DomainModel.FindDataSourceAndCheckPermissions(Permissions, principal, domainName);
			if (string.IsNullOrWhiteSpace(specificationName))
			{
				ICountCommand command;
				if (!Cache.TryGetValue(domainObjectType, out command))
				{
					var commandType = typeof(SearchDomainObjectCommand<>).MakeGenericType(domainObjectType);
					command = Activator.CreateInstance(commandType) as ICountCommand;
					var newCache = new Dictionary<Type, ICountCommand>(Cache);
					newCache[domainObjectType] = command;
					Cache = newCache;
				}
				return command;
			}
			else
			{
				var specificationType =
					DomainModel.FindNested(domainName, specificationName)
					?? DomainModel.Find(specificationName);
				if (specificationType == null)
					throw new ArgumentException("Couldn't find specification: {0}".With(specificationName));
				//TODO: cache command
				var commandType = typeof(SearchDomainObjectWithSpecificationCommand<,>).MakeGenericType(domainObjectType, specificationType);
				return Activator.CreateInstance(commandType) as ICountCommand;
			}
		}

		private interface ICountCommand
		{
			long FindCount<TInput>(ISerialization<TInput> input, IServiceProvider locator, TInput data);
			long FindCount<TSpecification>(IServiceProvider locator, TSpecification specification);
		}

		private static IQueryableRepository<T> GetRepository<T>(IServiceProvider locator)
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

		private class SearchDomainObjectCommand<TDomainObject> : ICountCommand
			where TDomainObject : IDataSource
		{
			public virtual long FindCount<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceProvider locator,
				TFormat data)
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
						throw new ArgumentException(@"Specification could not be deserialized.
Please provide specification name. Error: {0}.".With(ex.Message), ex);
					}
					if (specification == null)
						throw new FrameworkException("Specification could not be deserialized. Please provide specification name.");
					try
					{
						result = repository.Query(specification);
					}
					catch (Exception ex)
					{
						throw new ArgumentException(
							"Error while executing query: {0}.".With(ex.Message),
							new FrameworkException("Specification deserialized as: {0}".With((TFormat)serializer.Serialize(specification)), ex));
					}
				}
				return result.LongCount();
			}

			public long FindCount<TSpecification>(IServiceProvider locator, TSpecification specification)
			{
				var repository = GetRepository<TDomainObject>(locator);
				if (specification == null)
					return repository.Count();
				return repository.Count((dynamic)specification);
			}
		}

		private class SearchDomainObjectWithSpecificationCommand<TDomainObject, TSpecification> : SearchDomainObjectCommand<TDomainObject>
			where TDomainObject : IDataSource
		{
			public override long FindCount<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceProvider locator,
				TFormat data)
			{
				var repository = GetRepository<TDomainObject>(locator);
				dynamic specification;
				if (data == null)
				{
					try
					{
						specification = Activator.CreateInstance(typeof(TSpecification));
					}
					catch
					{
						throw new ArgumentException("Specification can't be created. It must be sent as argument.");
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
						throw new ArgumentException(@"Specification could not be deserialized: {0}.".With(ex.Message), ex);
					}
				}
				if (specification == null)
					throw new FrameworkException("Specification could not be deserialized.");
				try
				{
					return repository.Count(specification);
				}
				catch (Exception ex)
				{
					throw new ArgumentException(
						"Error while executing query: {0}.".With(ex.Message),
						new FrameworkException("Specification deserialized as: {0}".With((TFormat)serializer.Serialize(specification)), ex));
				}
			}
		}
	}
}
