using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.ServiceModel;
using System.Web;
using System.Xml.Linq;
using Revenj.Api;
using Revenj.Common;
using Revenj.Extensibility;
using Revenj.Processing;
using System.Threading;

namespace Revenj.Wcf
{
	public class SoapApplication : ISoapApplication
	{
		private readonly IProcessingEngine ProcessingEngine;
		private readonly IPluginRepository<IServerCommand> ServerCommands;

		public SoapApplication(
			IProcessingEngine processingEngine,
			IPluginRepository<IServerCommand> serverCommandsRepository)
		{
			Contract.Requires(processingEngine != null);
			Contract.Requires(serverCommandsRepository != null);

			this.ProcessingEngine = processingEngine;
			this.ServerCommands = serverCommandsRepository;
		}

		public SoapResultDescription Execute(SoapCommandDescription[] soapCommands)
		{
			IServerCommandDescription<XElement>[] processingCommands;
			if (soapCommands == null || soapCommands.Length == 0)
				throw new FaultException("SOAP commands missing.");

			if (soapCommands.Where(sc => sc.CommandName == null ||
				ServerCommands.Find(sc.CommandName) == null).Any())
			{
				var unknownCommand =
					(from sc in soapCommands
					 where sc.CommandName == null ||
					 ServerCommands.Find(sc.CommandName) == null
					 select sc.CommandName)
					 .First();

				throw new FaultException("Unknown command: " + unknownCommand);
			}

			processingCommands = PrepareCommands(soapCommands).ToArray();

			var result = ProcessingEngine.Execute<XElement, XElement>(processingCommands, Thread.CurrentPrincipal);

			if (result.Status == HttpStatusCode.ServiceUnavailable)
				HttpRuntime.UnloadAppDomain();
			if (result.Status == HttpStatusCode.InternalServerError || result.ExecutedCommandResults == null)
				throw new FrameworkException(result.Message);
			if ((int)result.Status >= 300)
				throw new FaultException(result.Message);

			return ConvertToSoapResult(result);
		}

		private IEnumerable<IServerCommandDescription<XElement>> PrepareCommands(SoapCommandDescription[] soapCommands)
		{
			return
				from sc in soapCommands
				select SoapCommandDescription<XElement>.Create(
					 sc.RequestID,
					 sc.Data != null ? XElement.Parse(sc.Data) : null,
					 ServerCommands.Find(sc.CommandName));
		}

		private SoapResultDescription ConvertToSoapResult(IProcessingResult<XElement> result)
		{
			var executed =
				from ecr in result.ExecutedCommandResults
				let data = ecr.Result.Data
				select SoapCommandResultDescription.Create(
					ecr.RequestID,
					ecr.Result.Status,
					ecr.Result.Message,
					data != null ? data.ToString() : null);

			return SoapResultDescription.Create(result.Message, executed.ToArray());
		}
	}
}
