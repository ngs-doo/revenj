module Mailer
{
	guid root MailMessage
	{
		native<'Revenj.Features.Mailer.Serialization.SerializableMailMessage, Revenj.Features.Mailer'> NativeMessage;
		
		timestamp? SentAt;
		int Attempts;
		int RetriesAllowed;
		string[] Errors;

		implements server 'Revenj.Features.Mailer.IMailMessage, Revenj.Features.Mailer';
	}
}

register server Mailer.MailService as Revenj.Features.Mailer.IMailService;

server code <#
namespace Mailer
{
	using Revenj.DomainPatterns;
	using Revenj.Features.Mailer;
	using System.Collections.Generic;
	using System.Linq;

	public class MailService : Revenj.Features.Mailer.MailService
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
		System.Net.Mail.MailMessage Revenj.Features.Mailer.IMailMessage.Message 
		{ 
			get { return NativeMessage != null ? NativeMessage.GetMailMessage() : null; }
			set
			{
				if (value == null) throw new System.ArgumentNullException("Message cannot be null");
				NativeMessage = new Revenj.Features.Mailer.Serialization.SerializableMailMessage(value);
			}
		}
	}
} #>;