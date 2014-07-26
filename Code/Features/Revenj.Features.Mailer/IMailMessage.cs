using System;
using System.Net.Mail;
using Revenj.DomainPatterns;

namespace Revenj.Features.Mailer
{
	public interface IMailMessage : IAggregateRoot
	{
		Guid ID { get; }
		MailMessage Message { get; set; }
		DateTime? SentAt { get; set; }
		int Attempts { get; set; }
		int RetriesAllowed { get; set; }
		string[] Errors { get; set; }
	}
}
