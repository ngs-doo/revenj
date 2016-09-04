using System;
using System.Collections.Concurrent;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using Revenj.DatabasePersistence.Postgres.Npgsql;

namespace Revenj.DatabasePersistence.Postgres
{
	//TODO: internal!?
	public interface IConnectionPool
	{
		NpgsqlConnection Take(bool open);
		void Release(NpgsqlConnection connection, bool valid);
	}

	internal class PostgresConnectionPool : IConnectionPool, IDisposable
	{
		private readonly BlockingCollection<NpgsqlConnection> Connections = new BlockingCollection<NpgsqlConnection>(new ConcurrentBag<NpgsqlConnection>());

		public enum PoolMode
		{
			None,
			Wait,
			IfAvailable
		}

		private readonly PoolMode Mode = PoolMode.IfAvailable;
		private readonly ConnectionInfo Info;
		private readonly int Size;

		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Database");

		public PostgresConnectionPool(ConnectionInfo info)
		{
			this.Info = info;
			if (!int.TryParse(ConfigurationManager.AppSettings["Database.PoolSize"], out Size))
				Size = Math.Min(Environment.ProcessorCount, 20);
			if (!Enum.TryParse<PoolMode>(ConfigurationManager.AppSettings["Database.PoolMode"], out Mode))
			{
				//TODO: Mono has issues with BlockingCollection. use None as default
				int p = (int)Environment.OSVersion.Platform;
				if (p == 4 || p == 6 || p == 128)
					Mode = PoolMode.None;
				else
					Mode = PoolMode.IfAvailable;
			}
			if (Mode != PoolMode.None)
			{
				if (Size < 1) Size = 1;
				for (int i = 0; i < Size; i++)
					Connections.Add(info.GetConnection());
			}
		}


		public NpgsqlConnection Take(bool open)
		{
			NpgsqlConnection conn;
			switch (Mode)
			{
				case PoolMode.None:
					conn = Info.GetConnection();
					break;
				case PoolMode.Wait:
					conn = Connections.Take();
					break;
				default:
					if (!Connections.TryTake(out conn))
						conn = Info.GetConnection();
					break;
			}
			if (open)
			{
				try
				{
					if (conn.State == ConnectionState.Closed)
						conn.Open();
				}
				catch (Exception ex)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5010, ex.ToString());
					try { conn.Close(); }
					catch { }
					NpgsqlConnection.ClearAllPools();
					conn = Info.GetConnection();
					conn.Open();
				}
			}
			return conn;
		}

		public void Release(NpgsqlConnection connection, bool valid)
		{
			switch (Mode)
			{
				case PoolMode.None:
					try { connection.Close(); }
					catch (Exception ex)
					{
						if (valid)
							TraceSource.TraceEvent(TraceEventType.Error, 5011, "{0}", ex);
					}
					break;
				default:
					if (valid && connection.State == ConnectionState.Open && Connections.Count < Size)
					{
						Connections.Add(connection);
					}
					else
					{
						try { connection.Close(); }
						catch (Exception ex)
						{
							if (valid)
								TraceSource.TraceEvent(TraceEventType.Error, 5012, "{0}", ex);
						}
						if (Connections.Count < Size)
							Connections.Add(Info.GetConnection());
					}
					break;
			}
		}

		public void Dispose()
		{
			try
			{
				foreach (var con in Connections)
				{
					try { con.Close(); }
					catch (Exception ex) { TraceSource.TraceEvent(TraceEventType.Error, 5013, "{0}", ex); }
				}
				Connections.Dispose();
				NpgsqlConnection.ClearAllPools();
			}
			catch (Exception e) { TraceSource.TraceEvent(TraceEventType.Error, 5014, "{0}", e); }
		}
	}
}
