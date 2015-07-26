using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using Revenj.Common;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.Processing;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;
using System.Security.Principal;

namespace Revenj.Plugins.Server.Commands
{
	[Export(typeof(IServerCommand))]
	[ExportMetadata(Metadata.ClassType, typeof(FindInvalidData))]
	public class FindInvalidData : IReadOnlyServerCommand
	{
		private readonly IDomainModel DomainModel;
		private readonly IPermissionManager Permissions;

		public FindInvalidData(
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
			public string DomainObjectName;
			[DataMember]
			public string ValidationName;
			[DataMember]
			public string BindingObjectName;
			[DataMember]
			public TFormat Specification;
		}

		private static TFormat CreateExampleArgument<TFormat>(ISerialization<TFormat> serializer)
		{
			return serializer.Serialize(new Argument<TFormat> { DomainObjectName = "Module.Entity", ValidationName = "Validation" });
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

			Type domainType = DomainModel.Find(argument.DomainObjectName);
			if (domainType == null)
				return CommandResult<TOutput>.Fail(
					"Couldn't find domain object type {0}.".With(argument.DomainObjectName),
					@"Example argument: 
" + CommandResult<TOutput>.ConvertToString(CreateExampleArgument(output)));

			if (!Permissions.CanAccess(domainType.FullName, principal))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.DomainObjectName);

			Type validationType = DomainModel.FindNested(domainType.FullName, argument.ValidationName);
			if (validationType == null)
				return
					CommandResult<TOutput>.Fail(
						"Couldn't find validation {0} for {1}".With(
							argument.ValidationName,
							argument.DomainObjectName),
						null);

			if (!Permissions.CanAccess(validationType.FullName, principal))
				return
					CommandResult<TOutput>.Return(
						HttpStatusCode.Forbidden,
						default(TOutput),
						"You don't have permission to access: {0}.",
						argument.ValidationName);

			Type bindingType = string.IsNullOrWhiteSpace(argument.BindingObjectName) ? null : DomainModel.Find(argument.BindingObjectName);

			try
			{
				var commandType = typeof(FindInvalidDataCommand<>).MakeGenericType(domainType);
				var command = Activator.CreateInstance(commandType) as IFindInvalidDataCommand;
				var result = command.FindInvalid(input, output, DomainModel, locator, validationType, bindingType, argument.Specification);
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

		private class FindResult<TFormat>
		{
			public TFormat Result;
			public int Count;
		}

		private interface IFindInvalidDataCommand
		{
			FindResult<TOutput> FindInvalid<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IDomainModel dom,
				IServiceProvider serviceLocator,
				Type validationType,
				Type bindType,
				TInput data);
		}

		private class FindInvalidDataCommand<TEntity> : IFindInvalidDataCommand
			where TEntity : IIdentifiable
		{
			public FindResult<TOutput> FindInvalid<TInput, TOutput>(
				ISerialization<TInput> input,
				ISerialization<TOutput> output,
				IDomainModel dom,
				IServiceProvider locator,
				Type validationType,
				Type bindType,
				TInput data)
			{
				var repository = locator.Resolve<IQueryableRepository<TEntity>>();
				var validation = locator.Resolve<IValidation<TEntity>>(validationType);
				ISpecification<TEntity> specification = null;
				try
				{
					if (data != null)
						specification = input.Deserialize<TInput, ISpecification<TEntity>>(data, locator);
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Error deserializing specification", ex);
				}

				List<TEntity> invalidItems;
				try
				{
					invalidItems = validation.FindInvalidItems(repository.Query(specification)).ToList();
				}
				catch (Exception ex)
				{
					string additionalInfo;
					try
					{
						additionalInfo = specification == null ? "Specification not provided" : @"Specification deserialized as: 
" + input.Serialize(specification);
					}
					catch (Exception sex)
					{
						additionalInfo = "Error serializing specification: " + sex.Message;
					}
					throw new ArgumentException(
						"Error while executing query: {0}".With(ex.Message),
						new FrameworkException(additionalInfo, ex));
				}

				if (bindType != null)
				{
					var commandType = typeof(BindToDomainObject<,>).MakeGenericType(bindType, typeof(TEntity));
					var command = Activator.CreateInstance(commandType) as IBindToDomainObject<TEntity>;
					return command.BindInvalid(output, invalidItems, validation, locator);
				}

				var items =
					(from ii in invalidItems
					 select new InvalidItem { ErrorDescription = validation.GetErrorDescription(ii), URI = ii.URI })
					.ToArray();
				return new FindResult<TOutput> { Result = output.Serialize(items), Count = items.Length };
			}
		}

		private interface IBindToDomainObject<TEntity>
			where TEntity : IIdentifiable
		{
			FindResult<TOutput> BindInvalid<TOutput>(
				ISerialization<TOutput> output,
				List<TEntity> invalidItems,
				IValidation<TEntity> validation,
				IServiceProvider locator);
		}

		private class BindToDomainObject<TValue, TEntity> : IBindToDomainObject<TEntity>
			where TValue : IIdentifiable
			where TEntity : IIdentifiable
		{
			public FindResult<TOutput> BindInvalid<TOutput>(
				ISerialization<TOutput> output,
				List<TEntity> invalidItems,
				IValidation<TEntity> validation,
				IServiceProvider locator)
			{
				var repository = locator.Resolve<IRepository<TValue>>();
				var i = repository.Find(invalidItems.Select(it => it.URI));
				var items =
					(from ii in invalidItems
					 let item = i.FirstOrDefault(it => it.URI == ii.URI)
					 select new InvalidItem<TValue> { ErrorDescription = validation.GetErrorDescription(ii), URI = ii.URI, Value = item })
					.ToArray();
				return new FindResult<TOutput> { Result = output.Serialize(items), Count = items.Length };
			}
		}
	}
}
