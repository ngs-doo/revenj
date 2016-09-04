using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics.Contracts;

namespace Revenj.DatabasePersistence
{
	/// <summary>
	/// ADO.NET database abstraction.
	/// Execute SQL queries on database.
	/// </summary>
	public interface IDatabaseQuery
	{
		/// <summary>
		/// Create new database command for specific ADO.NET driver.
		/// </summary>
		/// <returns>new command</returns>
		IDbCommand NewCommand();
		/// <summary>
		/// Is current query inside a transaction.
		/// </summary>
		bool InTransaction { get; }
		/// <summary>
		/// Execute query on the database without the regards for result.
		/// Connection and transaction information will be appended to the provided command.
		/// command.Execute() will be called.
		/// </summary>
		/// <param name="command">database command</param>
		/// <returns>base ExecuteNonQuery result</returns>
		int Execute(IDbCommand command);
		/// <summary>
		/// Execute query on the database and loop through the reader.
		/// Connection and transaction information will be appended to the provided command.
		/// command.ExecuteDataReader() will be called.
		/// </summary>
		/// <param name="command">database command</param>
		/// <param name="action">handle result returned from the database</param>
		void Execute(IDbCommand command, Action<IDataReader> action);
		/// <summary>
		/// Execute query on the database and fill DataSet with the result.
		/// Connection and transaction information will be appended to the provided command.
		/// </summary>
		/// <param name="command">command to execute</param>
		/// <param name="ds">data set to fill</param>
		/// <returns>how many rows were changed</returns>
		int Fill(IDbCommand command, DataSet ds);
	}
	/// <summary>
	/// Utilities for ADO.NET access.
	/// </summary>
	public static class DatabaseQueryHelper
	{
		/// <summary>
		/// Read field from the record.
		/// </summary>
		/// <typeparam name="T">field type</typeparam>
		/// <param name="dr">data reader</param>
		/// <param name="name">field name</param>
		/// <returns>field value</returns>
		public static T Field<T>(this IDataRecord dr, string name)
		{
			Contract.Requires(dr != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(name));

			var index = dr.GetOrdinal(name);
			return dr.IsDBNull(index) ? default(T) : (T)dr.GetValue(index);
		}
		/// <summary>
		/// Execute query on the database without the regards for result.
		/// Connection and transaction information will be appended to the provided command.
		/// </summary>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="parameters">SQL parameters</param>
		/// <returns>comand.Execute() result</returns>
		public static int Execute(
			this IDatabaseQuery query,
			string sql,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.CreateCommand(sql, parameters);
			return query.Execute(com);
		}
		/// <summary>
		/// Create database command using provided SQL and by
		/// replacing ? with parameter arguments.
		/// </summary>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="parameters">parameters to bind</param>
		/// <returns></returns>
		public static IDbCommand CreateCommand(
			this IDatabaseQuery query,
			string sql,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.NewCommand();
			com.CommandText = sql;
			if (parameters != null)
				foreach (var p in parameters)
				{
					var cp = com.CreateParameter();
					cp.Value = p;
					if (string.IsNullOrEmpty(cp.ParameterName))
						cp.ParameterName = ":p" + (com.Parameters.Count + 1);
					com.Parameters.Add(cp);
					var index = sql.IndexOf('?');
					sql = sql.Substring(0, index) + cp.ParameterName + sql.Substring(index + 1);
				}
			com.CommandText = sql;
			return com;
		}
		/// <summary>
		/// Execute query on the database and loop through the reader.
		/// Connection and transaction information will be appended to the provided command.
		/// Provided parameters will be inserted into the command.
		/// command.ExecuteDataReader() will be called.
		/// </summary>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="action">data reader converter</param>
		/// <param name="parameters">command parameters</param>
		public static void Execute(
			this IDatabaseQuery query,
			string sql,
			Action<IDataReader> action,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.CreateCommand(sql, parameters);
			query.Execute(com, action);
		}
		/// <summary>
		/// Execute query on the database and loop through the reader.
		/// Return list populated from instance factory.
		/// Connection and transaction information will be appended to the provided command.
		/// command.ExecuteDataReader() will be called.
		/// </summary>
		/// <typeparam name="T">result type</typeparam>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="instancer">object factory</param>
		/// <param name="parameters">additional command parameters</param>
		/// <returns>populated list</returns>
		public static List<T> Fill<T>(
			this IDatabaseQuery query,
			string sql,
			Func<IDataReader, T> instancer,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.CreateCommand(sql, parameters);
			var list = new List<T>();
			query.Execute(com, dr => list.Add(instancer(dr)));
			return list;
		}
		/// <summary>
		/// Execute query on the database and fill DataTable with the result.
		/// Connection and transaction information will be appended to the provided command.
		/// </summary>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="parameters">SQL additional parameters</param>
		/// <returns>populated table</returns>
		public static DataTable Fill(
			this IDatabaseQuery query,
			string sql,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.CreateCommand(sql, parameters);
			var ds = new DataSet();
			query.Fill(com, ds);
			return ds.Tables.Count > 0 ? ds.Tables[0] : new DataTable();
		}
		/// <summary>
		/// Execute query on the database and fill DataTable with the result.
		/// Connection and transaction information will be appended to the provided command.
		/// </summary>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="command">command to execute</param>
		/// <returns>populated table</returns>
		public static DataTable Fill(
			this IDatabaseQuery query,
			IDbCommand command)
		{
			Contract.Requires(query != null);
			Contract.Requires(command != null);

			var ds = new DataSet();
			query.Fill(command, ds);
			return ds.Tables.Count > 0 ? ds.Tables[0] : new DataTable();
		}
		/// <summary>
		/// Execute query on the database and return converted result.
		/// Connection and transaction information will be appended to the provided command.
		/// </summary>
		/// <typeparam name="T">result type</typeparam>
		/// <param name="query">ADO.NET driver</param>
		/// <param name="sql">SQL to execute</param>
		/// <param name="instancer">object factory</param>
		/// <param name="parameters"></param>
		/// <returns></returns>
		public static T Get<T>(
			this IDatabaseQuery query,
			string sql,
			Func<IDataReader, T> instancer,
			params object[] parameters)
		{
			Contract.Requires(query != null);
			Contract.Requires(!string.IsNullOrWhiteSpace(sql));

			var com = query.CreateCommand(sql, parameters);
			T t = default(T);
			query.Execute(com, dr => t = instancer(dr));
			return t;
		}
	}
}
