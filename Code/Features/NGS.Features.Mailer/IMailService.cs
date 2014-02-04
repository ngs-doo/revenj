using System.Collections.Generic;
using System.Linq;
using System.Net.Mail;

namespace NGS.Features.Mailer
{
	public interface IMailService
	{
		string[] Queue(IEnumerable<MailMessage> messages, int? maxRetries);
		bool TrySend(string uri);
	}

	public static class MailServiceHelper
	{
		public static string Queue(this IMailService service, MailMessage message)
		{
			return service.Queue(message, null);
		}
		public static string Queue(this IMailService service, MailMessage message, int? maxRetries)
		{
			return service.Queue(new[] { message }, maxRetries).FirstOrDefault();
		}
	}
}
