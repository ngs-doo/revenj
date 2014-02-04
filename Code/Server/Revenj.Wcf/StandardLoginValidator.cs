using System.IdentityModel.Selectors;
using System.Security;
using System.ServiceModel;
using NGS;
using NGS.Security;

namespace Revenj.Wcf
{
	public class StandardLoginValidator : UserNamePasswordValidator
	{
		private readonly IAuthentication Authentication = ContainerWcfHost.Resolve<IAuthentication>();

		public override void Validate(string userName, string password)
		{
			var secure = new SecureString();
			if (password != null)
				foreach (var p in password)
					secure.AppendChar(p);
			if (!Authentication.IsAuthenticated(userName, secure))
				throw new FaultException("User {0} was not authenticated".With(userName));
		}
	}
}