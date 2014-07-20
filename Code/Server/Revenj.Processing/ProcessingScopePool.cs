using System;
using System.Collections.Concurrent;
using System.Configuration;
using NGS.DatabasePersistence;
using NGS.Extensibility;

namespace Revenj.Processing
{
	public interface IProcessingScopePool
	{
		Scope Take(bool readOnly);
		void Release(Scope factory, bool valid);
	}

	public class Scope
	{
		public readonly IObjectFactory Factory;
		public readonly IDatabaseQuery Query;

		public Scope(IObjectFactory factory, IDatabaseQuery query)
		{
			this.Factory = factory;
			this.Query = query;
		}
	}

	public class ProcessingScopePool : IProcessingScopePool, IDisposable
	{
		private readonly BlockingCollection<Scope> Scopes = new BlockingCollection<Scope>(new ConcurrentBag<Scope>());

		public enum PoolMode
		{
			None,
			Wait,
			IfAvailable
		}

		private readonly PoolMode Mode = PoolMode.IfAvailable;
		private readonly int Size;

		private readonly IObjectFactory Factory;
		private readonly IDatabaseQueryManager Queries;

		public ProcessingScopePool(
			IObjectFactory factory,
			IDatabaseQueryManager queries,
			IExtensibilityProvider extensibilityProvider)
		{
			this.Factory = factory;
			this.Queries = queries;
			if (!int.TryParse(ConfigurationManager.AppSettings["Processing.PoolSize"], out Size))
				Size = 20;
			if (!Enum.TryParse<PoolMode>(ConfigurationManager.AppSettings["Processing.PoolMode"], out Mode))
				Mode = PoolMode.IfAvailable;
			var commandTypes = extensibilityProvider.FindPlugins<IServerCommand>();
			Factory.RegisterTypes(commandTypes, InstanceScope.Context);
			if (Mode != PoolMode.None)
			{
				if (Size < 1) Size = 1;
				for (int i = 0; i < Size; i++)
					Scopes.Add(SetupReadonlyScope());
			}
		}

		private Scope SetupReadonlyScope()
		{
			var inner = Factory.CreateScope(null);
			try
			{
				var query = Queries.StartQuery(false);
				inner.RegisterInstance(query);
				return new Scope(inner, query);
			}
			catch
			{
				inner.Dispose();
				throw;
			}
		}

		private Scope SetupWritableScope()
		{
			var id = Guid.NewGuid().ToString();
			var inner = Factory.CreateScope(id);
			try
			{
				var query = Queries.StartQuery(true);
				inner.RegisterInstance(query);
				inner.RegisterType(typeof(ProcessingContext), typeof(IProcessingEngine), InstanceScope.Singleton);
				return new Scope(inner, query);
			}
			catch
			{
				inner.Dispose();
				throw;
			}
		}

		public Scope Take(bool readOnly)
		{
			if (!readOnly)
				return SetupWritableScope();
			switch (Mode)
			{
				case PoolMode.None:
					return SetupReadonlyScope();
				case PoolMode.Wait:
					return Scopes.Take();
				default:
					Scope scope;
					if (!Scopes.TryTake(out scope))
						return SetupReadonlyScope();
					return scope;
			}
		}

		public void Release(Scope scope, bool valid)
		{
			switch (Mode)
			{
				case PoolMode.None:
					Queries.EndQuery(scope.Query, valid);
					scope.Factory.Dispose();
					break;
				default:
					if (valid && Scopes.Count < Size)
					{
						Scopes.Add(scope);
					}
					else
					{
						Queries.EndQuery(scope.Query, valid);
						scope.Factory.Dispose();
						if (Scopes.Count < Size)
							Scopes.Add(SetupReadonlyScope());
					}
					break;
			}
		}

		public void Dispose()
		{
			foreach (var s in Scopes)
			{
				Queries.EndQuery(s.Query, false);
				s.Factory.Dispose();
			}
			Scopes.Dispose();
		}
	}
}
