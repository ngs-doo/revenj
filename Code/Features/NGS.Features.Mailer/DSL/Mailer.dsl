module Mailer
{
	guid root MailMessage
	{
		SerializableMailMessage SerializableMessage;
		
		timestamp? SentAt;
		int Attempts;
		int RetriesAllowed;
		string[] Errors;

		implements server 'NGS.Features.Mailer.IMailMessage, NGS.Features.Mailer';
	}

	value SerializableMailAddress
	{
		string Address;
		string? DisplayName;
	}

	value SerializableContentType
	{
		string Boundary;
		string CharSet;
		string MediaType;
		string Name;
		map Parameters;
	}

	value SerializableLinkedResource
	{
		string ContentId;
		url ContentLink;
		stream? ContentStream;
		SerializableContentType ContentType;
		int TransferEncoding;
	}

	value SerializableContentDisposition
	{
		timestamp CreationDate;
		string DispositionType;
		string FileName;
		bool Inline;
		timestamp ModificationDate;
		map Parameters;
		timestamp ReadDate;
		long Size;
	}

	value SerializableAttachment
	{
		string ContentId;
		SerializableContentDisposition ContentDisposition;
		SerializableContentType ContentType;
		stream ContentStream;
		int TransferEncoding;
		string Name;
		int? NameEncoding;
	}

	value SerializableAlternateView
	{
		url BaseUri;
		string ContentId;
		stream? ContentStream;
		SerializableContentType ContentType;
		List<SerializableLinkedResource> LinkedResources;
		int TransferEncoding;
	}

	value SerializableMailMessage
	{
		bool IsBodyHtml;
		string Body;
		SerializableMailAddress From;
		SerializableMailAddress? Sender;
		string Subject;
		int? BodyEncoding;
		int? SubjectEncoding;
		int DeliveryNotificationOptions;
		map Headers;
		int Priority;

		List<SerializableMailAddress> To;
		List<SerializableMailAddress> CC;
		List<SerializableMailAddress> Bcc;
		List<SerializableMailAddress> ReplyToList;
		List<SerializableAlternateView> AlternateViews;
		List<SerializableAttachment> Attachments;
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
	using System;
	using System.Text;
	using System.Net.Mail;
	using System.Net.Mime;
	using System.Collections;

	partial class MailMessage
	{
		System.Net.Mail.MailMessage NGS.Features.Mailer.IMailMessage.Message 
		{ 
			get 
			{
				var sm = SerializableMessage;
				var mm = new System.Net.Mail.MailMessage
				{
					IsBodyHtml = sm.IsBodyHtml,
					Body = sm.Body,
					Subject = sm.Subject,
					BodyEncoding = sm.BodyEncoding != null ? Encoding.GetEncoding(sm.BodyEncoding.Value) : null,
					DeliveryNotificationOptions = (DeliveryNotificationOptions)sm.DeliveryNotificationOptions,
					Priority = (MailPriority)sm.Priority,
					SubjectEncoding = sm.SubjectEncoding != null ? Encoding.GetEncoding(sm.SubjectEncoding.Value) : null,
				};
				if (sm.From != null)
					mm.From = sm.From.GetMailAddress();
				foreach (var mailAddress in sm.To)
					mm.To.Add(mailAddress.GetMailAddress());
				foreach (var mailAddress in sm.CC)
					mm.CC.Add(mailAddress.GetMailAddress());
				foreach (var mailAddress in sm.Bcc)
					mm.Bcc.Add(mailAddress.GetMailAddress());
				foreach (var attachment in sm.Attachments)
					mm.Attachments.Add(attachment.GetAttachment());
				foreach(var kv in sm.Headers)
						mm.Headers.Add(kv.Key, kv.Value);
				foreach (var mailAddress in sm.ReplyToList)
					mm.ReplyToList.Add(mailAddress.GetMailAddress());
				if (sm.Sender != null)
					mm.Sender = sm.Sender.GetMailAddress();
				foreach (var alternateView in sm.AlternateViews)
					mm.AlternateViews.Add(alternateView.GetAlternateView());
				return mm;
			}
			set
			{
				if (value == null) throw new System.ArgumentNullException("Message cannot be null");
				var msg = new SerializableMailMessage
				{
					IsBodyHtml = value.IsBodyHtml,
					Body = value.Body,
					Subject = value.Subject,
					From = new SerializableMailAddress(value.From),
					BodyEncoding = value.BodyEncoding != null ? (int?)value.BodyEncoding.CodePage : null,
					DeliveryNotificationOptions = (int)value.DeliveryNotificationOptions,
					Priority = (int)value.Priority,
					SubjectEncoding = value.SubjectEncoding != null ? (int?)value.SubjectEncoding.CodePage : null
				};
				foreach (string key in value.Headers.Keys)
					msg.Headers[key] = value.Headers[key];
				if (value.Sender != null)
					msg.Sender = new SerializableMailAddress(value.Sender);
				foreach (MailAddress ma in value.To)
					msg.To.Add(new SerializableMailAddress(ma));
				foreach (MailAddress ma in value.CC)
					msg.CC.Add(new SerializableMailAddress(ma));
				foreach (MailAddress ma in value.Bcc)
					msg.Bcc.Add(new SerializableMailAddress(ma));
				foreach (Attachment att in value.Attachments)
					msg.Attachments.Add(new SerializableAttachment(att));
				foreach (MailAddress ma in value.ReplyToList)
					msg.ReplyToList.Add(new SerializableMailAddress(ma));
				foreach (AlternateView av in value.AlternateViews)
					msg.AlternateViews.Add(new SerializableAlternateView(av));
				SerializableMessage = msg;	
			}
		}
	}

	partial class SerializableMailAddress
	{
		public SerializableMailAddress(System.Net.Mail.MailAddress address)
		{
			Address = address.Address;
			DisplayName = address.DisplayName;
		}

		public System.Net.Mail.MailAddress GetMailAddress()
		{
			return new System.Net.Mail.MailAddress(Address, DisplayName);
		}
	}

	partial class SerializableLinkedResource
	{
		public SerializableLinkedResource(LinkedResource linkedResource)
		{
			ContentId = linkedResource.ContentId;
			ContentLink = linkedResource.ContentLink;
			ContentType = new SerializableContentType(linkedResource.ContentType);
			TransferEncoding = (int)linkedResource.TransferEncoding;
			ContentStream = linkedResource.ContentStream;
		}

		public LinkedResource GetLinkedResource()
		{
			return new LinkedResource(ContentStream)
			{
				ContentId = ContentId,
				ContentLink = ContentLink,
				ContentType = ContentType.GetContentType(),
				TransferEncoding = (TransferEncoding)TransferEncoding
			};
		}
	}

	partial class SerializableContentType
	{
		public SerializableContentType(System.Net.Mime.ContentType ct)
		{
			Boundary = ct.Boundary;
			CharSet = ct.CharSet;
			MediaType = ct.MediaType;
			Name = ct.Name;
			foreach (string key in ct.Parameters.Keys)
				Parameters[key] = ct.Parameters[key];
		}

		public ContentType GetContentType()
		{
			var ct = new ContentType
			{
				Boundary = Boundary,
				CharSet = CharSet,
				MediaType = MediaType,
				Name = Name,
			};
			foreach(var kv in Parameters)
			{
				if (ct.Parameters.ContainsKey(kv.Key))
					ct.Parameters[kv.Key] = kv.Value;
				else
					ct.Parameters.Add(kv.Key, kv.Value);
			}
			return ct;
		}
	}

	partial class SerializableContentDisposition
	{
		public SerializableContentDisposition(ContentDisposition cd)
		{
			CreationDate = cd.CreationDate;
			DispositionType = cd.DispositionType;
			FileName = cd.FileName;
			Inline = cd.Inline;
			ModificationDate = cd.ModificationDate;
			foreach (string key in cd.Parameters.Keys)
				Parameters[key] = cd.Parameters[key];
			ReadDate = cd.ReadDate;
			Size = cd.Size;
		}

		public void CopyTo(ContentDisposition cd)
		{
			cd.CreationDate = CreationDate;
			cd.DispositionType = DispositionType;
			cd.FileName = FileName;
			cd.Inline = Inline;
			cd.ModificationDate = ModificationDate;
			cd.ReadDate = ReadDate;
			cd.Size = Size;
			foreach(var kv in Parameters)
			{
				if (cd.Parameters.ContainsKey(kv.Key))
					cd.Parameters[kv.Key] = kv.Value;
				else
					cd.Parameters.Add(kv.Key, kv.Value);
			}
		}
	}

	partial class SerializableAttachment
	{
		public SerializableAttachment(Attachment attachment)
		{
			ContentId = attachment.ContentId;
			ContentDisposition = new SerializableContentDisposition(attachment.ContentDisposition);
			ContentType = new SerializableContentType(attachment.ContentType);
			Name = attachment.Name;
			TransferEncoding = (int)attachment.TransferEncoding;
			NameEncoding = attachment.NameEncoding != null ? (int?)attachment.NameEncoding.CodePage : null;
			ContentStream = attachment.ContentStream;
		}

		public Attachment GetAttachment()
		{
			var attachment = new Attachment(ContentStream, Name)
			{
				ContentId = ContentId,
				ContentType = ContentType.GetContentType(),
				Name = Name,
				TransferEncoding = (TransferEncoding)TransferEncoding,
				NameEncoding = NameEncoding != null ? Encoding.GetEncoding(NameEncoding.Value) : null,
			};

			ContentDisposition.CopyTo(attachment.ContentDisposition);

			return attachment;
		}
	}

	partial class SerializableAlternateView
	{
		public SerializableAlternateView(AlternateView alternativeView)
		{
			BaseUri = alternativeView.BaseUri;
			ContentId = alternativeView.ContentId;
			ContentType = new SerializableContentType(alternativeView.ContentType);
			TransferEncoding = (int)alternativeView.TransferEncoding;
			ContentStream = alternativeView.ContentStream;

			foreach (var lr in alternativeView.LinkedResources)
				LinkedResources.Add(new SerializableLinkedResource(lr));
		}

		public AlternateView GetAlternateView()
		{
			var sav = new AlternateView(ContentStream)
			{
				BaseUri = BaseUri,
				ContentId = ContentId,
				ContentType = ContentType.GetContentType(),
				TransferEncoding = (TransferEncoding)TransferEncoding,
			};

			foreach (var linkedResource in LinkedResources)
				sav.LinkedResources.Add(linkedResource.GetLinkedResource());

			return sav;
		}
	}
}
#>;