using System;
using NGS.DomainPatterns;
using NGS.Features.Mailer.Serialization;

namespace NGS.Features.Mailer
{
	public interface IMailMessage : IAggregateRoot
	{
		Guid ID { get; }
		SerializableMailMessage Message { get; set; }
		DateTime? SentAt { get; set; }
		int Attempts { get; set; }
		int RetriesAllowed { get; set; }
		string[] Errors { get; set; }
	}
}
