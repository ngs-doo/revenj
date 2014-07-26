using System;
using System.Collections.Generic;
using System.Net.Mail;
using System.Text;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableMailMessage
	{
		private readonly bool IsBodyHtml;
		private readonly string Body;
		private readonly SerializableMailAddress From;
		private readonly SerializableMailAddress Sender;
		private readonly string Subject;
		private readonly Encoding BodyEncoding;
		private readonly Encoding SubjectEncoding;
		private readonly DeliveryNotificationOptions DeliveryNotificationOptions;
		private readonly SerializableCollection Headers;
		private readonly MailPriority Priority;

		private readonly List<SerializableMailAddress> To = new List<SerializableMailAddress>();
		private readonly List<SerializableMailAddress> CC = new List<SerializableMailAddress>();
		private readonly List<SerializableMailAddress> Bcc = new List<SerializableMailAddress>();
		private readonly List<SerializableMailAddress> ReplyToList = new List<SerializableMailAddress>();
		private readonly List<SerializableAlternateView> AlternateViews = new List<SerializableAlternateView>();
		private readonly List<SerializableAttachment> Attachments = new List<SerializableAttachment>();

		public SerializableMailMessage(MailMessage mailMessage)
		{
			IsBodyHtml = mailMessage.IsBodyHtml;
			Body = mailMessage.Body;
			Subject = mailMessage.Subject;
			From = new SerializableMailAddress(mailMessage.From);

			foreach (MailAddress ma in mailMessage.To)
				To.Add(new SerializableMailAddress(ma));

			foreach (MailAddress ma in mailMessage.CC)
				CC.Add(new SerializableMailAddress(ma));

			foreach (MailAddress ma in mailMessage.Bcc)
				Bcc.Add(new SerializableMailAddress(ma));

			foreach (Attachment att in mailMessage.Attachments)
				Attachments.Add(new SerializableAttachment(att));

			BodyEncoding = mailMessage.BodyEncoding;

			DeliveryNotificationOptions = mailMessage.DeliveryNotificationOptions;
			Headers = new SerializableCollection(mailMessage.Headers);
			Priority = mailMessage.Priority;

			foreach (MailAddress ma in mailMessage.ReplyToList)
				ReplyToList.Add(new SerializableMailAddress(ma));

			if (mailMessage.Sender != null)
				Sender = new SerializableMailAddress(mailMessage.Sender);

			SubjectEncoding = mailMessage.SubjectEncoding;

			foreach (AlternateView av in mailMessage.AlternateViews)
				AlternateViews.Add(new SerializableAlternateView(av));
		}

		public MailMessage GetMailMessage()
		{
			var mailMessage = new MailMessage
			{
				IsBodyHtml = IsBodyHtml,
				Body = Body,
				Subject = Subject,
				BodyEncoding = BodyEncoding,
				DeliveryNotificationOptions = DeliveryNotificationOptions,
				Priority = Priority,
				SubjectEncoding = SubjectEncoding,
			};

			if (From != null)
				mailMessage.From = From.GetMailAddress();

			foreach (var mailAddress in To)
				mailMessage.To.Add(mailAddress.GetMailAddress());

			foreach (var mailAddress in CC)
				mailMessage.CC.Add(mailAddress.GetMailAddress());

			foreach (var mailAddress in Bcc)
				mailMessage.Bcc.Add(mailAddress.GetMailAddress());

			foreach (var attachment in Attachments)
				mailMessage.Attachments.Add(attachment.GetAttachment());

			Headers.CopyTo(mailMessage.Headers);

			foreach (var mailAddress in ReplyToList)
				mailMessage.ReplyToList.Add(mailAddress.GetMailAddress());

			if (Sender != null)
				mailMessage.Sender = Sender.GetMailAddress();

			foreach (var alternateView in AlternateViews)
				mailMessage.AlternateViews.Add(alternateView.GetAlternateView());

			return mailMessage;
		}
	}
}