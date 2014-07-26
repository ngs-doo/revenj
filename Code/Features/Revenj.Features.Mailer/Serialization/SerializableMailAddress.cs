using System;
using System.Net.Mail;

namespace Revenj.Features.Mailer.Serialization
{
	[Serializable]
	public class SerializableMailAddress
	{
		private readonly string Address;
		private readonly string DisplayName;

		public SerializableMailAddress(MailAddress address)
		{
			Address = address.Address;
			DisplayName = address.DisplayName;
		}

		public MailAddress GetMailAddress()
		{
			return new MailAddress(Address, DisplayName);
		}
	}
}