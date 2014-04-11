using System.IdentityModel.Selectors;
using System.ServiceModel;
using NGS;
using NGS.Security;

namespace Revenj.Wcf
{
	public class StandardLoginValidator : UserNamePasswordValidator
	{
		private readonly IAuthentication<string> Authentication = ContainerWcfHost.Resolve<IAuthentication<string>>();

		public override void Validate(string userName, string password)
		{
			if (!Authentication.IsAuthenticated(userName, password))
				throw new FaultException("User {0} was not authenticated".With(userName));
		}
	}
}