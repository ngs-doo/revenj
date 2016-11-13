using System;
using System.Collections.Generic;
using System.Text;
using Revenj.DomainPatterns;
using Revenj.Storage;
using NGS.Serialization;

namespace Revenj
{
	public static class Client
	{
#if !PORTABLE
		public static IServiceProvider Start()
		{
			var dict = new Dictionary<string, string>();
			foreach (string k in System.Configuration.ConfigurationManager.AppSettings.Keys)
				dict[k] = System.Configuration.ConfigurationManager.AppSettings[k];
			return Start(dict);
		}
#endif

		public static IServiceProvider Start(string remoteUrl, string auth)
		{
			return Start(new Dictionary<string, string>() { { "RemoteUrl", remoteUrl }, { "Auth", auth } });
		}

		public static IServiceProvider Start(string remoteUrl, string username, string password)
		{
			var auth = Convert.ToBase64String(Encoding.UTF8.GetBytes(username + ":" + password));
			return Start(new Dictionary<string, string>() { { "RemoteUrl", remoteUrl }, { "BasicAuth", auth } });
		}

		public static IServiceProvider Start(IDictionary<string, string> settings)
		{
			var configuration = new Configuration(new Dictionary<string, string>(settings));
			var locator = new DictionaryServiceLocator();
			var protobuf = new ProtobufSerialization();
			var restHttp = new HttpClient(locator, protobuf, configuration);
			var app = new ApplicationProxy(restHttp);
			var domain = new DomainProxy(restHttp, app);
			var report = new ReportingProxy(restHttp, app);
			locator.Register(typeof(Configuration), configuration);
			locator.Register(typeof(ProtobufSerialization), protobuf);
			locator.Register(typeof(HttpClient), restHttp);
			locator.Register(typeof(IServiceProvider), locator);
			locator.Register(typeof(IApplicationProxy), app);
			locator.Register(typeof(ICrudProxy), new CrudProxy(restHttp));
			locator.Register(typeof(IS3Repository), new LitS3Repository(configuration));
			locator.Register(typeof(IDomainProxy), domain);
			locator.Register(typeof(IStandardProxy), new StandardProxy(restHttp, protobuf, app));
			locator.Register(typeof(IReportingProxy), report);
			locator.Register(typeof(ITemplaterService), new ClientTemplaterService(report));
			locator.Register(typeof(ISearchableRepository<>), typeof(ClientSearchableRepository<>));
			locator.Register(typeof(IRepository<>), typeof(ClientRepository<>));
			locator.Register(typeof(IPersistableRepository<>), typeof(ClientPersistableRepository<>));
			locator.Register(typeof(IDomainEventStore), new ClientDomainStore(domain));
			Static.Locator = locator;
			return locator;
		}
	}
}
