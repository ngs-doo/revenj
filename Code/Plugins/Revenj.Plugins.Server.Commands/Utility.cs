using System;
using System.Globalization;
using System.Security;
using System.Security.Principal;
using Revenj.DomainPatterns;
using Revenj.Security;

namespace Revenj.Plugins.Server.Commands
{
	internal static class Utility
	{
		public static Type FindNested(this IDomainModel dom, string type, string name)
		{
			return dom.Find(string.Format(CultureInfo.InvariantCulture, "{0}+{1}", type, name));
		}

		public static Type FindDataSourceAndCheckPermissions(
			this IDomainModel DomainModel,
			IPermissionManager Permissions,
			IPrincipal principal,
			string domainName)
		{
			if (string.IsNullOrEmpty(domainName))
				throw new ArgumentException("Domain object name not provided.");
			var domainObjectType = DomainModel.Find(domainName);
			if (domainObjectType == null)
				throw new ArgumentException("Couldn't find domain object: {0}".With(domainName));
			if (!typeof(IDataSource).IsAssignableFrom(domainObjectType))
				throw new ArgumentException(@"Specified type ({0}) is not a data source. 
Please check your arguments.".With(domainName));
			if (!Permissions.CanAccess(domainObjectType.FullName, principal))
				throw new SecurityException("You don't have permission to access: {0}.".With(domainName));
			return domainObjectType;
		}
	}
}
