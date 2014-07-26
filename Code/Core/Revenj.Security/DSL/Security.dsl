module Security
{
	root User(Name)
	{
		string(100) Name;
		Role(Name) *Role;
		string Password;
		bool IsAllowed;
		implements server 'Revenj.Security.IUser, Revenj.Security';
	}

	root Role(Name)
	{
		string(100) Name;
	}

	root InheritedRole(Name, ParentName)
	{
		string(100) Name;
		string(100) ParentName;
		Role(Name) *Role;
		Role(ParentName) *ParentRole;
		implements server 'Revenj.Security.IUserRoles, Revenj.Security';
	}

	role Administrator;

	root GlobalPermission(Name)
	{
		string(200) Name;
		bool IsAllowed;
		implements server 'Revenj.Security.IGlobalPermission, Revenj.Security';
	}

	root RolePermission(Name, RoleID)
	{
		string(200) Name;
		Role *Role;
		bool IsAllowed;
		implements server 'Revenj.Security.IRolePermission, Revenj.Security';
	}
}

server code <#
namespace Revenj
{
	using System;
	using Revenj.DomainPatterns;
	using Revenj.Extensibility;
	using System.ComponentModel.Composition;
	using SecUser = global::Security.User;

	[Export(typeof(ISystemAspect))]
	internal class SecurityHelperInitializer : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			SecurityHelper.Repository = new Lazy<IDataCache<SecUser>>(() => factory.Resolve<IDataCache<SecUser>>());
		}
	}

	public static partial class SecurityHelper
	{
		internal static Lazy<IDataCache<SecUser>> Repository;

		public static SecUser GetUser(this System.Security.Principal.IPrincipal principal)
		{
			return Repository.Value.Find(principal.Identity.Name);
		}
	}
}
#>;