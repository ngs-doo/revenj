using System;
using System.Net.Mail;
using NGS.DomainPatterns;

namespace NGS.Features.Mailer
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
