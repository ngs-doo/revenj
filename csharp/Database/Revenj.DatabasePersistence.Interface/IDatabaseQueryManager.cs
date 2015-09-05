using System;
using System.Diagnostics.Contracts;

namespace Revenj.DatabasePersistence
{
	/// <summary>
	/// ADO.NET driver manager.
	/// Create new database drivers and close existing ones.
	/// </summary>
	public interface IDatabaseQueryManager
	{
		/// <summary>
		/// Start new ADO.NET driver.
		/// If query is created with a transaction, it must be explicitly committed.
		/// </summary>
		/// <param name="withTransaction">use transaction</param>
		/// <returns>database query</returns>
		IDatabaseQuery StartQuery(bool withTransaction);
		/// <summary>
		/// Close existing ADO.NET driver.
		/// If query is created with a transaction provide true for success to commit transaction.
		/// </summary>
		/// <param name="query">database query</param>
		/// <param name="success">was query successful</param>
		void EndQuery(IDatabaseQuery query, bool success);
	}
	/// <summary>
	/// Utilities for ADO.NET driver manager.
	/// </summary>
	public static class DatabaseQueryManagerHelper
	{
		/// <summary>
		/// Start database query with a transaction.
		/// </summary>
		/// <param name="manager">query manager</param>
		/// <returns>ADO.NET driver</returns>
		public static IDatabaseQuery BeginTransaction(this IDatabaseQueryManager manager)
		{
			Contract.Requires(manager != null);

			return manager.StartQuery(true);
		}
		/// <summary>
		/// Start database query without a transaction.
		/// </summary>
		/// <param name="manager">query manager</param>
		/// <returns>ADO.NET driver</returns>
		public static IDatabaseQuery CreateQuery(this IDatabaseQueryManager manager)
		{
			Contract.Requires(manager != null);

			return manager.StartQuery(false);
		}
		/// <summary>
		/// Commit started transaction.
		/// </summary>
		/// <param name="manager">query manager</param>
		/// <param name="query">ADO.NET driver</param>
		public static void Commit(this IDatabaseQueryManager manager, IDatabaseQuery query)
		{
			Contract.Requires(manager != null);
			Contract.Requires(query != null);

			if (!query.InTransaction)
				throw new ArgumentException("Only queries in transaction can be committed");
			manager.EndQuery(query, true);
		}
		/// <summary>
		/// Rollback started transaction.
		/// </summary>
		/// <param name="manager">query manager</param>
		/// <param name="query">ADO.NET driver</param>
		public static void Rollback(this IDatabaseQueryManager manager, IDatabaseQuery query)
		{
			Contract.Requires(manager != null);
			Contract.Requires(query != null);

			if (!query.InTransaction)
				throw new ArgumentException("Only queries in transaction can be rolled back");
			manager.EndQuery(query, false);
		}
	}
}
