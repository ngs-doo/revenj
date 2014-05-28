using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Net;
using System.Net.Mail;
using NGS.DomainPatterns;

namespace NGS.Features.Mailer
{
	public abstract class MailService : IMailService
	{
		private static readonly string SmtpServer;
		private static readonly int SmtpPort;
		private static readonly bool SmtpUseSSL;
		private static readonly string SmtpUsername;
		private static readonly string SmtpPassword;
		private static readonly int? SmtpMaxRetries;

		static MailService()
		{
			SmtpServer = ConfigurationManager.AppSettings["SmtpServer"];
			if (string.IsNullOrEmpty(SmtpServer))
				throw new ConfigurationErrorsException("Configuration is missing SmtpServer setting. Can't send email without it");
			if (!int.TryParse(ConfigurationManager.AppSettings["SmtpPort"], out SmtpPort))
				SmtpPort = 25;
			bool.TryParse(ConfigurationManager.AppSettings["SmtpUseSSL"], out SmtpUseSSL);
			SmtpUsername = ConfigurationManager.AppSettings["SmtpUsername"];
			SmtpPassword = ConfigurationManager.AppSettings["SmtpPassword"];
			int x;
			if (!int.TryParse(ConfigurationManager.AppSettings["SmtpMaxRetries"], out x))
				SmtpMaxRetries = x;
		}

		protected readonly Func<string, IMailMessage> Lookup;

		public MailService(IServiceLocator locator)
		{
			Contract.Requires(locator != null);

			this.Lookup = locator.Resolve<Func<string, IMailMessage>>();
		}

		protected abstract IMailMessage Create();
		protected abstract string[] Insert(IEnumerable<IMailMessage> messages);
		protected abstract void Update(IEnumerable<IMailMessage> messages);

		public string[] Queue(IEnumerable<MailMessage> messages, int? maxRetries)
		{
			var items = new List<IMailMessage>();
			foreach (var msg in messages)
			{
				var item = Create();
				item.Message = msg;
				var retries = maxRetries ?? SmtpMaxRetries;
				if (retries != null)
					item.RetriesAllowed = retries.Value;
				items.Add(item);
			}
			return Insert(items);
		}

		public bool TrySend(string uri)
		{
			var found = Lookup(uri);
			if (found == null)
				throw new ArgumentException("Can't find message {0}".With(uri));
			if (found.SentAt != null)
				throw new ArgumentException("Message {0} already sent.".With(uri));
			if (found.Attempts > found.RetriesAllowed)
				throw new ArgumentException("Message {0} retry limit. Maximum number of retries: {1}".With(uri, found.RetriesAllowed));
			var smtp = new SmtpClient(SmtpServer, SmtpPort);
			if (!string.IsNullOrWhiteSpace(SmtpUsername))
				smtp.Credentials = new NetworkCredential(SmtpUsername, SmtpPassword);
			smtp.EnableSsl = SmtpUseSSL;
			found.Attempts++;
			try
			{
				smtp.Send(found.Message);
				found.SentAt = DateTime.Now;
				return true;
			}
			catch (Exception ex)
			{
				found.Errors = found.Errors.Concat(new[] { ex.ToString() }).ToArray();
				return false;
			}
			finally
			{
				Update(new[] { found });
			}
		}

		public static void SendNow(MailMessage message)
		{
			var smtp = new SmtpClient(SmtpServer, SmtpPort);
			if (!string.IsNullOrWhiteSpace(SmtpUsername))
				smtp.Credentials = new NetworkCredential(SmtpUsername, SmtpPassword);
			smtp.EnableSsl = SmtpUseSSL;
			smtp.Send(message);
		}
	}
}
