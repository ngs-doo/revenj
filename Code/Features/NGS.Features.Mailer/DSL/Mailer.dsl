module Mailer
{
	guid root MailMessage
	{
		native<'NGS.Features.Mailer.Serialization.SerializableMailMessage, NGS.Features.Mailer'> NativeMessage;
		
		timestamp? SentAt;
		int Attempts;
		int RetriesAllowed;
		string[] Errors;

		implements server 'NGS.Features.Mailer.IMailMessage, NGS.Features.Mailer';
	}
}

register server Mailer.MailService as NGS.Features.Mailer.IMailService;

server code <#
namespace Mailer
{
	using NGS.DomainPatterns;
	using NGS.Features.Mailer;
	using System.Collections.Generic;
	using System.Linq;

	public class MailService : NGS.Features.Mailer.MailService
	{
		private readonly IPersistableRepository<MailMessage> Repository;

		public MailService(IServiceLocator locator) 
			: base(locator)
		{
			Repository = locator.Resolve<IPersistableRepository<MailMessage>>();
		}

		protected override IMailMessage Create() { return new MailMessage { RetriesAllowed = 3 }; }
		protected override string[] Insert(IEnumerable<IMailMessage> messages) 
		{ 
			return Repository.Insert(from MailMessage msg in messages select msg); 
		}
		protected override void Update(IEnumerable<IMailMessage> messages) 
		{ 
			Repository.Update(from MailMessage msg in messages select msg); 
		}
	}
}
#>;

server code <#
namespace Mailer
{
	partial class MailMessage
	{
		System.Net.Mail.MailMessage NGS.Features.Mailer.IMailMessage.Message 
		{ 
			get { return NativeMessage != null ? NativeMessage.GetMailMessage() : null; }
			set
			{
				if (value == null) throw new System.ArgumentNullException("Message cannot be null");
				NativeMessage = new NGS.Features.Mailer.Serialization.SerializableMailMessage(value);
			}
		}
	}
} #>;