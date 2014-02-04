using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.ServiceModel;
using System.Text;
using System.Xml.Linq;
using NGS.DomainPatterns;
using NGS.Serialization;
using Revenj.Api;
using Revenj.Plugins.Server.Commands;

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

		private Stream Persist(MethodEnum method, Type root, Stream data)
		{
			var array = (object[])Utility.ParseObject(Serialization, root.MakeArrayType(), data, false, Locator);
			return
				Converter.PassThrough<PersistAggregateRoot, PersistAggregateRoot.Argument<object>>(
					new PersistAggregateRoot.Argument<object>
					{
						RootName = root.FullName,
						ToInsert = method == MethodEnum.Insert ? array : null,
						ToUpdate = method == MethodEnum.Update ? CreateKvMethod.MakeGenericMethod(root).Invoke(this, new[] { array }) : null,
						ToDelete = method == MethodEnum.Delete ? array : null
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
			Type cubeType,
			Type specificationType,
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
				Utility.ThrowError("At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);

			var specification = specificationType != null
				? Utility.ParseObject(Serialization, specificationType, data, true, Locator)
				: null;

			return Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
				new AnalyzeOlapCube.Argument<object>
				{
					CubeName = cubeType.FullName,
					SpecificationName = specificationType != null ? specificationType.FullName : null,
					Specification = specification,
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
				Utility.ThrowError("Specification must be specified", HttpStatusCode.BadRequest);
			var cubeType = Utility.CheckDomainObject(DomainModel, cube);
			var specType =
				Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : cubeType.FullName + "+") + specification);
			return OlapCube(cubeType, specType, dimensions, facts, order, limit, offset, body);
		}

		private Stream OlapCube(
			Type cubeType,
			string dimensions,
			string facts,
			string order,
			object specification,
			string limit,
			string offset)
		{
			var dimArr = string.IsNullOrEmpty(dimensions) ? new string[0] : dimensions.Split(',');
			var factArr = string.IsNullOrEmpty(facts) ? new string[0] : facts.Split(',');
			if (dimArr.Length == 0 && factArr.Length == 0)
				Utility.ThrowError("At least one dimension or fact must be specified", HttpStatusCode.BadRequest);
			var ordDict = Utility.ParseOrder(order);
			int? intLimit, intOffset;
			Utility.ParseLimitOffset(limit, offset, out intLimit, out intOffset);

			return
				Converter.PassThrough<AnalyzeOlapCube, AnalyzeOlapCube.Argument<object>>(
					new AnalyzeOlapCube.Argument<object>
					{
						CubeName = cubeType.FullName,
						SpecificationName = null,
						Specification = specification,
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
			var specType = string.IsNullOrEmpty(specification)
				? null
				: Utility.CheckDomainObject(
					DomainModel,
					(specification.Contains("+") ? string.Empty : cubeType.FullName + "+") + specification);

			var spec = specType != null ? Utility.SpecificationFromQuery(specType) : null;
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
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);
			object spec;
			switch (Utility.GetIncomingFormat())
			{
				case MessageFormat.Json:
					spec = Utility.ParseGenericSpecification<string>(Serialization, ci.Key, body);
					break;
				case MessageFormat.ProtoBuf:
					spec = Utility.ParseGenericSpecification<MemoryStream>(Serialization, ci.Key, body);
					break;
				default:
					spec = Utility.ParseGenericSpecification<XElement>(Serialization, ci.Key, body);
					break;
			}
			return OlapCube(ci.Key, dimensions, facts, order, spec, limit, offset);
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
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);

			var spec = Utility.GenericSpecificationFromQuery(ci.Value);
			return OlapCube(ci.Key, dimensions, facts, order, spec, limit, offset);
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
			if (ci.Value == null)
				Utility.ThrowError("Cube data source not found. Static DataSource property not found.", HttpStatusCode.NotImplemented);

			var spec = Utility.ParseExpressionSpecification(Serialization, ci.Key, body);
			return OlapCube(ci.Key, dimensions, facts, order, spec, limit, offset);
		}

		private Stream Execute<TFormat>(string service, TFormat data)
		{
			if (typeof(TFormat) == typeof(StreamReader))
				return Execute(service, (data as StreamReader).ReadToEnd());
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
				case MessageFormat.Json:
					return Execute(service, new StreamReader(body, Encoding.UTF8));
				case MessageFormat.ProtoBuf:
					return Execute(service, body);
				default:
					return Execute(service, Utility.ParseXml(body));
			}
		}
	}
}
