using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.Threading.Tasks;
using Revenj.DomainPatterns;
using NGS.Serialization;

namespace Revenj
{
	internal class StandardProxy : IStandardProxy
	{
		private const string URL = "Commands.svc/";
		private readonly HttpClient Http;
		private readonly ProtobufSerialization Protobuf;
		private readonly IApplicationProxy Application;

		public StandardProxy(
			HttpClient http,
			ProtobufSerialization protobuf,
			IApplicationProxy application)
		{
			this.Http = http;
			this.Protobuf = protobuf;
			this.Application = application;
		}

		[DataContract(Namespace = "")]
		class PersistArg
		{
			[DataMember]
			public string RootName;
			[DataMember]
			public MemoryStream ToInsert;
			[DataMember]
			public MemoryStream ToUpdate;
			[DataMember]
			public MemoryStream ToDelete;
		}

		public Task<string[]> Persist<T>(
			IEnumerable<T> insert,
			IEnumerable<KeyValuePair<T, T>> update,
			IEnumerable<T> delete)
			where T : IAggregateRoot
		{
			var insertList = insert != null ? insert.ToList() : new List<T>();
			var updateList = update != null ? update.ToList() : new List<KeyValuePair<T, T>>();
			var deleteList = delete != null ? delete.ToList() : new List<T>();
			if (insertList.Count == 0 && updateList.Count == 0 && deleteList.Count == 0)
				return Task.Factory.StartNew(() => new string[0]);
			foreach (var it in insertList)
			{
				if (it != null)
					it.Validate();
				else throw new ArgumentNullException("Provided null value in insert collection");
			}
			foreach (var it in updateList)
			{
				if (it.Value != null)
					it.Value.Validate();
				else throw new ArgumentNullException("Provided null value in update collection");
			}
			foreach (var it in deleteList)
			{
				if (it != null)
					it.Validate();
				else throw new ArgumentNullException("Provided null value in delete collection");
			}
			var arg = new PersistArg
			{
				RootName = typeof(T).FullName,
				ToInsert = insertList.Count > 0 ? Protobuf.Serialize(insertList.ToArray()) : null,
				ToUpdate = updateList.Count > 0 ? Protobuf.Serialize(updateList.ToArray()) : null,
				ToDelete = deleteList.Count > 0 ? Protobuf.Serialize(deleteList.ToArray()) : null
			};
			return Application.Post<PersistArg, string[]>("PersistAggregateRoot", arg);
		}

		private static string BuildOlapArguments(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			var query = string.Empty;
			if (dimensions != null && dimensions.Any())
				query = "dimensions=" + string.Join(",", dimensions.ToArray());
			if (facts != null && facts.Any())
				query += (query.Length > 0 ? "&" : string.Empty) + "facts=" + string.Join(",", facts.ToArray());
			if (query.Length == 0)
				throw new ArgumentException("At least one dimension or fact is required");
			if (order != null && order.Any())
				query += "&order=" + string.Join(",", order.Select(it => (!it.Value ? "-" : string.Empty) + it.Key).ToArray());
			return query;
		}

		public Task<TResult[]> OlapCube<TCube, TSpecification, TResult>(
			TSpecification specification,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			var specName = typeof(TSpecification).FullName.StartsWith(typeof(TCube).FullName)
				? typeof(TSpecification).Name
				: typeof(TSpecification).FullName;
			return
				Http.Call<TSpecification>(
					URL + "olap/" + typeof(TCube).FullName + "?specification=" + specName + "&" + BuildOlapArguments(dimensions, facts, order),
					"PUT",
					specification,
					new[] { HttpStatusCode.Created },
					"application/json")
				.ContinueWith<TResult[]>(t =>
				{
					var dcs = new DataContractJsonSerializer(typeof(TResult[]));
					return (TResult[])dcs.ReadObject(t.Result);
				});
		}

		public Task<TResult[]> OlapCube<TCube, TResult>(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			return
				Http.Get(
					URL + "olap/" + typeof(TCube).FullName + "?" + BuildOlapArguments(dimensions, facts, order),
					new[] { HttpStatusCode.Created },
					"application/json")
				.ContinueWith<TResult[]>(t =>
				{
					var dcs = new DataContractJsonSerializer(typeof(TResult[]));
					return (TResult[])dcs.ReadObject(t.Result);
				});
		}

		public Task<TResult> Execute<TArgument, TResult>(string command, TArgument argument)
		{
			return
				Http.Call<TArgument, TResult>(
					URL + "execute/" + command,
					"POST",
					argument,
					new[] { HttpStatusCode.OK, HttpStatusCode.Created });
		}
	}
}
