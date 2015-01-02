using System;
using System.Configuration;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net.Mail;
using System.Reactive.Linq;
using System.Threading;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj.Features.Mailer
{
	public class QueueProcessor
	{
		private bool IsAlive;
		private readonly IMailService MailService;
		private readonly IQueryableRepository<IMailMessage> Repository;
		private readonly IDataChangeNotification ChangeNotification;

		private IDisposable Subscription;

		private readonly object sync = new object();

		private static readonly TimeSpan BufferTimeout;
		private static readonly int BufferCount;
		private static readonly MailAddress ToAdminEmail;
		private static readonly MailAddress FromEmail;
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Mailer");

		static QueueProcessor()
		{
			int bt;
			if (!int.TryParse(ConfigurationManager.AppSettings["MailBufferTimeout"], out bt))
				bt = 30;
			BufferTimeout = TimeSpan.FromSeconds(bt);
			if (!int.TryParse(ConfigurationManager.AppSettings["MailBufferCount"], out BufferCount))
				BufferCount = 10;
			else if (BufferCount < 1)
				BufferCount = 1;
			var toEmail = ConfigurationManager.AppSettings["Mailer.Admin"];
			var fromEmail = ConfigurationManager.AppSettings["Mailer.From"] ?? "no-reply@" + Environment.MachineName;
			try
			{
				ToAdminEmail = new MailAddress(toEmail);
				FromEmail = new MailAddress(fromEmail);
			}
			catch { }
			if (ToAdminEmail == null || FromEmail == null)
				throw new ConfigurationErrorsException(@"Please define valid admin email settings: Mailer.Admin and Mailer.From. Example:
<appSettings>
	<add key=""Mailer.Admin"" value=""admin@example.com"" />
	<add key=""Mailer.From"" value=""no-reply@example.com"" />
</appSettings>");
		}

		public QueueProcessor(
			IMailService mailService,
			IQueryableRepository<IMailMessage> repository,
			IDataChangeNotification changeNotification)
		{
			Contract.Requires(mailService != null);
			Contract.Requires(repository != null);
			Contract.Requires(changeNotification != null);

			this.MailService = mailService;
			this.Repository = repository;
			this.ChangeNotification = changeNotification;
		}

		public void Start()
		{
			TraceSource.TraceEvent(TraceEventType.Start, 1011);
			IsAlive = true;
			Subscription =
				ChangeNotification.Track<IMailMessage>()
				.Buffer(BufferTimeout, BufferCount)
				.Subscribe(_ => Task.Factory.StartNew(ProcessAll));
			Task.Factory.StartNew(ProcessAll);
		}

		public void Stop()
		{
			TraceSource.TraceEvent(TraceEventType.Stop, 1011);
			IsAlive = false;
			if (Subscription != null)
				Subscription.Dispose();
			Subscription = null;
		}

		private void ProcessAll()
		{
			try
			{
				bool shouldRetry;
				lock (sync)
				{
					var notSent = Repository.Query(new NotSentSpecification()).ToList();
					if (notSent.Count > 0)
					{
						TraceSource.TraceEvent(
							TraceEventType.Verbose,
							1011,
							"Processing mail queue items ({0}): {1}",
							notSent.Count,
							string.Join(", ", notSent.Select(it => it.URI)));
					}
					else TraceSource.TraceEvent(TraceEventType.Verbose, 1011, "Mail queue empty");
					shouldRetry = notSent.Any(it => !MailService.TrySend(it.URI));
				}
				if (shouldRetry)
					for (int i = 0; i < 10; i++)
					{
						if (!IsAlive)
							break;
						Thread.Sleep(TimeSpan.FromSeconds(6));
					}
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Critical, 1011, "{0}", ex);
				var mm = new System.Net.Mail.MailMessage(FromEmail, ToAdminEmail) { Subject = "Fatal error sending email", Body = ex.ToString() };
				try { Revenj.Features.Mailer.MailService.SendNow(mm); }
				catch (Exception sendEx)
				{
					mm.Body += Environment.NewLine + sendEx.ToString();
					try { MailService.TrySend(MailService.Queue(mm)); }
					catch { }
				}
				Stop();
			}
		}
	}
}
