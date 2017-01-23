using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Linq.Expressions;
using System.Security.Principal;
using Revenj.Extensibility;

namespace Revenj.Security
{
	internal class PermissionManager : IPermissionManager, IDisposable
	{
		private readonly IObjectFactory ObjectFactory;

		private static bool DefaultPermissions;

		static PermissionManager()
		{
			var dp = ConfigurationManager.AppSettings["Permissions.OpenByDefault"];
			DefaultPermissions = string.IsNullOrEmpty(dp) ? true : bool.Parse(dp);
		}

		private bool PermissionsChanged = true;
		private Dictionary<string, bool> GlobalPermissions;
		private Dictionary<string, List<Pair>> RolePermissions;
		private readonly Dictionary<Type, List<Filter>> RegisteredFilters = new Dictionary<Type, List<Filter>>();

		private Dictionary<string, bool> Cache = new Dictionary<string, bool>();

		private readonly IDisposable GlobalSubscription;
		private readonly IDisposable RoleSubscription;

		private class Filter
		{
			public object Expression;
			public object Predicate;
			public string Role;
			public bool Inverse;
		}

		private class Pair
		{
			public string Name;
			public bool IsAllowed;
		}

		public PermissionManager(
			IObjectFactory objectFactory,
			IObservable<Lazy<IGlobalPermission>> globalChanges,
			IObservable<Lazy<IRolePermission>> roleChanges)
		{
			Contract.Requires(objectFactory != null);
			Contract.Requires(globalChanges != null);
			Contract.Requires(roleChanges != null);

			this.ObjectFactory = objectFactory;
			GlobalSubscription = globalChanges.Subscribe(_ => PermissionsChanged = true);
			RoleSubscription = roleChanges.Subscribe(_ => PermissionsChanged = true);
		}

		private void CheckPermissions()
		{
			if (!PermissionsChanged)
				return;
			using (var scope = ObjectFactory.CreateInnerFactory())
			{
				var globals = scope.Resolve<IQueryable<IGlobalPermission>>();
				var roles = scope.Resolve<IQueryable<IRolePermission>>();

				GlobalPermissions =
					globals.ToList()
					.ToDictionary(it => it.Name, it => it.IsAllowed);
				RolePermissions =
					(from dop in roles.ToList()
					 group dop by dop.Name into g
					 let values = g.Select(it => new Pair { Name = it.RoleID, IsAllowed = it.IsAllowed })
					 select new { g.Key, values })
					.ToDictionary(it => it.Key, it => it.values.ToList());
			}
			Cache = new Dictionary<string, bool>();
			PermissionsChanged = false;
		}

		private bool CheckOpen(string[] parts, int len)
		{
			if (len < 0)
				return DefaultPermissions;
			bool isOpen;
			if (GlobalPermissions.TryGetValue(string.Join(".", parts.Take(len)), out isOpen))
				return isOpen;
			return CheckOpen(parts, len - 1);
		}

		public bool CanAccess(string identifier, IPrincipal user)
		{
			CheckPermissions();
			bool isAllowed;
			var target = identifier ?? string.Empty;
			var id = user != null ? user.Identity.Name + ":" + target : target;
			if (Cache.TryGetValue(id, out isAllowed))
				return isAllowed;
			var parts = target.Split('.');
			isAllowed = CheckOpen(parts, parts.Length);
			if (user != null)
			{
				List<Pair> permissions;
				for (int i = parts.Length; i >= 0; i--)
				{
					var subName = string.Join(".", parts.Take(i));
					if (RolePermissions.TryGetValue(subName, out permissions))
					{
						var found =
							permissions.Find(it => user.Identity.Name == it.Name)
							?? permissions.Find(it => user.IsInRole(it.Name));
						if (found != null)
						{
							isAllowed = found.IsAllowed;
							break;
						}
					}
				}
			}
			var newCache = new Dictionary<string, bool>(Cache);
			newCache[id] = isAllowed;
			Cache = newCache;
			return isAllowed;
		}

		public IQueryable<T> ApplyFilters<T>(IPrincipal user, IQueryable<T> data)
		{
			List<Filter> registered;
			if (RegisteredFilters.TryGetValue(typeof(T), out registered))
			{
				var result = data;
				foreach (var r in registered)
					if (user.IsInRole(r.Role) != r.Inverse)
						result = result.Where((Expression<Func<T, bool>>)r.Expression);
				return result;
			}
			return data;
		}

		public T[] ApplyFilters<T>(IPrincipal user, T[] data)
		{
			List<Filter> registered;
			if (RegisteredFilters.TryGetValue(typeof(T), out registered))
			{
				var result = new List<T>(data);
				foreach (var r in registered)
					if (user.IsInRole(r.Role) != r.Inverse)
						result = result.FindAll((Predicate<T>)r.Predicate);
				return result.ToArray();
			}
			return data;
		}

		private class Unregister : IDisposable
		{
			private readonly Action Command;
			public Unregister(Action command) { this.Command = command; }
			public void Dispose() { Command(); }
		}

		public IDisposable RegisterFilter<T>(Expression<Func<T, bool>> filter, string role, bool inverse)
		{
			var target = typeof(T);
			List<Filter> registered;
			if (!RegisteredFilters.TryGetValue(target, out registered))
				RegisteredFilters[target] = registered = new List<Filter>();
			var func = filter.Compile();
			Predicate<T> pred = arg => func(arg);
			var newFilter = new Filter { Expression = filter, Role = role, Inverse = inverse, Predicate = pred };
			registered.Add(newFilter);
			return new Unregister(() => registered.Remove(newFilter));
		}

		public void Dispose()
		{
			GlobalSubscription.Dispose();
			RoleSubscription.Dispose();
		}
	}
}
