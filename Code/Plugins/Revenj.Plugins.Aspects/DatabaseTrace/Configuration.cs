using System;
using System.ComponentModel.Composition;
using System.Configuration;
using System.Data;
using NGS.DatabasePersistence;
using NGS.Extensibility;

namespace Revenj.Plugins.Aspects.DatabaseTrace
{
	[Export(typeof(ISystemAspect))]
	public class Configuration : ISystemAspect
	{
		public static bool UseTrace { get; set; }

		static Configuration()
		{
			UseTrace = ConfigurationManager.AppSettings["Performance.TraceDatabase"] == "true";
		}

		public void Initialize(IObjectFactory factory)
		{
			if (!UseTrace)
				return;
			factory.RegisterTypes(new[] { typeof(QueryInterceptor) });
			var interceptor = factory.Resolve<QueryInterceptor>();
			var aspectRegistrator = factory.Resolve<IAspectRegistrator>();

			aspectRegistrator.Around<IDatabaseQuery, IDbCommand, int>(
				q => q.Execute((IDbCommand)null),
				interceptor.LogExecuteNonQuery);
			aspectRegistrator.Around<IDatabaseQuery, IDbCommand, Action<IDataReader>>(
				q => q.Execute(null, null),
				interceptor.LogExecuteDataReader);
			aspectRegistrator.Around<IDatabaseQuery, IDbCommand, DataTable, int>(
				q => q.Fill(null, null),
				interceptor.LogFillTable);
		}
	}
}
