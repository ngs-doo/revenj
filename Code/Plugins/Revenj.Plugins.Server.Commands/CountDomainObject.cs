using System;
using System.ComponentModel.Composition;
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
	[ExportMetadata(Metadata.ClassType, typeof(CountDomainObject))]
	public class CountDomainObject : IReadOnlyServerCommand
	{
		private readonly IServiceLocator Locator;
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public CountDomainObject(
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

		public ICommandResult<TOutput> Execute<TInput, TOutput>(ISerialization<TInput> input, ISerialization<TOutput> output, TInput data)
		{
			var either = CommandResult<TOutput>.Check<Argument<TInput>, TInput>(input, output, data, CreateExampleArgument);
			if (either.Error != null)
				return either.Error;

			try
			{
				var command = PrepareCommand(either.Argument.Name, either.Argument.SpecificationName);
				var count = command.FindCount(input, Locator, either.Argument.Specification);
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

		public long Execute(string name)
		{
			var command = PrepareCommand(name, null);
			return command.FindCount<object>(Locator, null);
		}

		public long Execute<TSpecification>(string name, TSpecification specification)
		{
			var command = PrepareCommand(name, typeof(TSpecification).FullName);
			return command.FindCount(Locator, specification);
		}

		private IFindDomainObjectCountCommand PrepareCommand(string domainName, string specificationName)
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
				return Activator.CreateInstance(commandType) as IFindDomainObjectCountCommand;
			}
			else
			{
				var specificationType =
					DomainModel.FindNested(domainName, specificationName)
					?? DomainModel.Find(specificationName);
				if (specificationType == null)
					throw new ArgumentException("Couldn't find specification: {0}".With(specificationName));
				var commandType = typeof(SearchDomainObjectWithSpecificationCommand<,>).MakeGenericType(domainObjectType, specificationType);
				return Activator.CreateInstance(commandType) as IFindDomainObjectCountCommand;
			}
		}

		private interface IFindDomainObjectCountCommand
		{
			long FindCount<TInput>(ISerialization<TInput> input, IServiceLocator locator, TInput data);
			long FindCount<TSpecification>(IServiceLocator locator, TSpecification specification);
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

		private class SearchDomainObjectCommand<TDomainObject> : IFindDomainObjectCountCommand
			where TDomainObject : class, IDataSource
		{
			public virtual long FindCount<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceLocator locator,
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

			public long FindCount<TSpecification>(IServiceLocator locator, TSpecification specification)
			{
				var repository = GetRepository<TDomainObject>(locator);
				IQueryable<TDomainObject> result;
				if (specification == null)
					result = repository.FindAll();
				else
					result = repository.Query((dynamic)specification);
				return result.LongCount();
			}
		}

		private class SearchDomainObjectWithSpecificationCommand<TDomainObject, TSpecification> : SearchDomainObjectCommand<TDomainObject>
			where TDomainObject : class, IDataSource
		{
			public override long FindCount<TFormat>(
				ISerialization<TFormat> serializer,
				IServiceLocator locator,
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
				IQueryable<TDomainObject> result;
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
				return result.LongCount();
			}
		}
	}
}
