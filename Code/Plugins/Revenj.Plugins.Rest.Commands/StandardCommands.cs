using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.ServiceModel;
using System.Text;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;

namespace Revenj.Plugins.Rest.Commands
{
	[ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
	public class StandardCommands : IStandardCommands
	{
		private readonly IServiceLocator Locator;
		private readonly ICommandConverter Converter;
		private readonly IDomainModel DomainModel;
		private readonly IWireSerialization Serialization;

		public StandardCommands(
			IServiceLocator locator,
			ICommandConverter converter,
			IDomainModel domainModel,
			IWireSerialization serialization)
		{
			this.Locator = locator;
			this.Converter = converter;
			this.DomainModel = domainModel;
			this.Serialization = serialization;
		}

		enum MethodEnum { Insert, Update, Delete }

		private Stream Persist(MethodEnum method, Either<Type> maybeRoot, Stream data)
		{
			if (maybeRoot.IsFailure) return maybeRoot.Error;
			var rootType = maybeRoot.Result;
			var array = Utility.ParseObject(Serialization, Either<Type>.Succes(rootType.MakeArrayType()), data, false, Locator);
			if (array.IsFailure) return array.Error;
			var arg = (object[])array.Result;
			return
				Converter.PassThrough<PersistAggregateRoot, PersistAggregateRoot.Argument<object>>(
					new PersistAggregateRoot.Argument<object>
					{
						RootName = rootType.FullName,
						ToInsert = method == MethodEnum.Insert ? arg : null,
						ToUpdate = method == MethodEnum.Update ? CreateKvMethod.MakeGenericMethod(rootType).Invoke(this, new[] { arg }) : null,
						ToDelete = method == MethodEnum.Delete ? arg : null
					});
		}

		private static MethodInfo CreateKvMethod = ((Func<object[], object>)CreateKeyValueArray<object>).Method.GetGenericMethodDefinition();

		private static KeyValuePair<T, T>[] CreateKeyValueArray<T>(object[] array)
		{
			return array != null ? array.Select(it => new KeyValuePair<T, T>(default(T), (T)it)).ToArray() : null;
		}

		private Stream Persist(MethodEnum method, string root, Stream body)
		{
			var type = Utility.CheckAggregateRoot(DomainModel, root);
			return Persist(method, type, body);
		}

		public Stream Insert(string root, Stream body)
		{
			return Persist(MethodEnum.Insert, root, body);
		}

		public Stream Update(string root, Stream body)
		{
			return Persist(MethodEnum.Update, root, body);
		}

		private Stream OlapCube(
			Either<Type> cubeType,
			Either<Type> specType,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream data)
		{
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Either<object>.BadRequest("At least one dimension or fact must be specified").Error;
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);

			var spec = Utility.ParseObject(Serialization, specType, data, true, Locator);
			if (cubeType.IsFailure || specType.IsFailure || spec.IsFailure) return cubeType.Error ?? specType.Error ?? spec.Error;

			return Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				new AnalyzeOlapCube.Argument<object>
				{
					CubeName = cubeType.Result.FullName,
					SpecificationName = specType.Result != null ? specType.Result.FullName : null,
					Specification = spec.Result,
					Dimensions = dimArr,
					Facts = factArr,
					Order = ordDict,
					Limit = intLimit,
					Offset = intOffset
				});
		}

		public Stream OlapCubeWithSpecification(
			string cube,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			return OlapCubeWithSpecificationQuery(cube, specification, dimensions, facts, order, limit, offset, body);
		}

		public Stream OlapCubeWithSpecificationQuery(
			string cube,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			if (string.IsNullOrEmpty(specification))
				return Either<object>.BadRequest("Specification must be specified").Error;
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification);
			return OlapCube(cubeType, specType, dimensions, facts, order, limit, offset, body);
		}

		private Stream OlapCube(
			Either<Type> cubeType,
			string dimensions,
			string facts,
			string order,
			Either<object> spec,
			string limit,
			string offset)
		{
			if (cubeType.IsFailure || spec.IsFailure) return cubeType.Error ?? spec.Error;
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				return Either<object>.BadRequest("At least one dimension or fact must be specified").Error;
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);
			return
				Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
					new AnalyzeOlapCube.Argument<object>
					{
						CubeName = cubeType.Result.FullName,
						SpecificationName = null,
						Specification = spec.Result,
						Dimensions = dimArr,
						Facts = factArr,
						Order = ordDict,
						Limit = intLimit,
						Offset = intOffset
					});
		}

		public Stream OlapCubeQuery(
			string cube,
			string specification,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset)
		{
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType = Utility.CheckDomainObject(DomainModel, cubeType, specification);
			var spec = Utility.SpecificationFromQuery(specType);
			return OlapCube(cubeType, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithGenericSpecification(
			string cube,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			if (ci.IsFailure) return ci.Error;
			var spec = Serialization.ParseGenericSpecification(ci.Result.Value, body);
			return OlapCube(ci.Result.Key, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithGenericSpecificationQuery(
			string cube,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			if (ci.IsFailure) return ci.Error;
			var spec = Utility.GenericSpecificationFromQuery(ci.Result.Value);
			return OlapCube(ci.Result.Key, dimensions, facts, order, spec, limit, offset);
		}

		public Stream OlapCubeWithExpression(
			string cube,
			string dimensions,
			string facts,
			string order,
			string limit,
			string offset,
			Stream body)
		{
			var ci = Utility.CheckCube(DomainModel, cube);
			if (ci.IsFailure) return ci.Error;
			var spec = Utility.ParseExpressionSpecification(Serialization, ci.Result.Value, body);
			return OlapCube(ci.Result.Key, dimensions, facts, order, spec, limit, offset);
		}

		private Stream Execute<TFormat>(string service, TFormat data)
		{
			return Converter.ConvertStream<ExecuteService, ExecuteService.Argument<TFormat>>(
				new ExecuteService.Argument<TFormat>
				{
					Name = service,
					Data = data
				});
		}

		//TODO use wire serialization
		public Stream Execute(string service, Stream body)
		{
			switch (Utility.GetIncomingFormat())
			{
				//TODO: maybe it's ok to use stream reader now!?
				case MessageFormat.Json:
					return Execute(service, new StreamReader(body, Encoding.UTF8).ReadToEnd());
				case MessageFormat.ProtoBuf:
					return Execute<Stream>(service, body);
				default:
					var xml = Utility.ParseXml(body);
					if (xml.IsFailure) return xml.Error;
					return Execute(service, xml.Result);
			}
		}
	}
}
