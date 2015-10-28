using System;
using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Security.Cryptography;
using System.Text;

namespace Revenj.Utility
{
	/// <summary>
	/// Access to temporary files.
	/// Default temporary folder is created at Revenj/Temp subfolder in system temporary folder
	/// Can be overridden with configuration settings: TemporaryPath
	/// </summary>
	public static class TemporaryResources
	{
		private static bool Initialized;
		private static string TempPath;

		static TemporaryResources()
		{
			var tp = ConfigurationManager.AppSettings["TemporaryPath"];
			TempPath = !string.IsNullOrEmpty(tp) && Directory.Exists(tp) ? tp : Path.Combine(Path.GetTempPath(), "Revenj", "Temp");
		}
		/// <summary>
		/// Create file with specified extension.
		/// File will be created in configured temporary folder.
		/// </summary>
		/// <param name="extension">created file extension</param>
		/// <returns>path to created file</returns>
		public static string CreateFile(string extension)
		{
			if (!Initialized)
				InitializeDirectory();
			if (extension != null && extension.Length > 0 && extension[0] != '.')
				extension = "." + extension;

			int retry = 5;
			while (--retry > 0)
			{
				var newFile = Path.Combine(TempPath, Path.GetRandomFileName() + extension);
				if (File.Exists(newFile))
					continue;
				File.Create(newFile).Dispose();
				return newFile;
			}
			throw new IOException("Failed to create temporary file - " + extension);
		}
		/// <summary>
		/// Create file in temporary folder with specified name.
		/// </summary>
		/// <param name="name">file name</param>
		/// <returns>full path to created file</returns>
		public static string TempFile(string name)
		{
			if (!Initialized)
				InitializeDirectory();

			var newFile = Path.Combine(TempPath, name);
			return newFile;
		}

		private static string GetTempPath()
		{
			var dd = AppDomain.CurrentDomain.DynamicDirectory;
			if (dd != null)
				return dd;

			if (!Initialized)
				InitializeDirectory();

			var temp = Path.Combine(TempPath, AppDomain.CurrentDomain.Id.ToString());
			if (!Directory.Exists(temp))
				Directory.CreateDirectory(temp);

			return temp;
		}
		/// <summary>
		/// Load assembly from provided stream. Assembly will be loaded into current AppDomain.
		/// Assembly will be saved to temporary folder and loaded from there.
		/// </summary>
		/// <param name="content">assembly content</param>
		/// <returns>loaded assembly</returns>
		public static Assembly LoadAssembly(Stream content)
		{
			if (!Initialized)
				InitializeDirectory();

			var cms = content is ChunkedMemoryStream ? content as ChunkedMemoryStream : new ChunkedMemoryStream(content);

			var hashCode = Convert.ToBase64String(MD5.Create().ComputeHash(cms)) + ".dll";
			var filename = Path.Combine(TempPath, hashCode.Replace('/', '_').Replace('\\', '_') + ".dll");

			cms.Position = 0;
			FileStream fs = null;
			if (!File.Exists(filename))
			{
				fs = new FileStream(filename, FileMode.Create, FileAccess.Write);
				cms.CopyTo(fs);
				fs.Close();
				return Assembly.LoadFrom(filename);
			}
			try
			{
				fs = new FileStream(filename, FileMode.Open, FileAccess.Read);
				if (cms.Equals(fs))
					return Assembly.LoadFrom(filename);
			}
			catch { }
			finally
			{
				if (fs != null)
					fs.Close();
			}
			//Warning LOH leak
			using (var ms = new MemoryStream((int)cms.Length))
			{
				cms.Position = 0;
				cms.CopyTo(ms);
				return Assembly.Load(ms.ToArray());
			}
		}

		private static string GetAssemblyName(byte[] content)
		{
			var tmp = CreateFile(".dll");
			File.WriteAllBytes(tmp, content);
			var fvi = FileVersionInfo.GetVersionInfo(tmp);
			File.Delete(tmp);
			return fvi.InternalName ?? fvi.OriginalFilename;
		}
		/// <summary>
		/// Load assembly from provided content. Assembly will be loaded into current AppDomain.
		/// Assembly will be saved to dynamic temporary folder and loaded from there.
		/// </summary>
		/// <param name="content">assembly content</param>
		/// <returns>loaded assembly</returns>
		public static Assembly LoadDynamicAssembly(byte[] content)
		{
			var name = GetAssemblyName(content);
			var filename = Path.Combine(GetTempPath(), name);

			if (!File.Exists(filename))
				File.WriteAllBytes(filename, content);
			try
			{
				if (File.ReadAllBytes(filename).SequenceEqual(content))
					return Assembly.LoadFile(filename);
			}
			catch { }
			return Assembly.Load(content);
		}
		/// <summary>
		/// Create copy of the file in the temporary folder.
		/// </summary>
		/// <param name="file">original file</param>
		/// <returns>path to copied file</returns>
		public static string CloneFile(string file)
		{
			var newFile = CreateFile(Path.GetExtension(file));
			File.Copy(file, newFile, true);
			return newFile;
		}

		private static void InitializeDirectory()
		{
			if (!Directory.Exists(TempPath))
				Directory.CreateDirectory(TempPath);

			Initialized = true;

			var files = Directory.EnumerateFiles(TempPath).ToList();
			files.ForEach(it =>
			{
				try { File.Delete(it); }
				catch { }
			});
			var dirs = Directory.EnumerateDirectories(TempPath).ToList();
			dirs.ForEach(it =>
			{
				try { Directory.Delete(it, true); }
				catch { }
			});
		}
		private static Type[] EmptyTypes = new Type[0];
		/// <summary>
		/// Create instance of specified type and populate it with
		/// random values.
		/// </summary>
		/// <param name="target">object type</param>
		/// <returns>object instance</returns>
		public static object CreateRandomObject(Type target)
		{
			var ctor = target.GetConstructor(EmptyTypes);
			//baked in serialization doesn't like uninitialized objects.
			try
			{
				object instance = ctor != null ? ctor.Invoke(null) : FormatterServices.GetUninitializedObject(target);
				var rnd = new Random();
				foreach (var p in instance.GetType().GetProperties())
				{
					var sm = p.GetSetMethod();
					if (sm != null && sm.IsPublic)
					{
						var pt = p.PropertyType;
						if (pt == typeof(string))
						{
							var sb = new StringBuilder();
							for (int i = 0; i < 10; i++)
								sb.Append((char)rnd.Next(65, 92));
							p.SetValue(instance, sb.ToString(), null);
						}
						else if (pt == typeof(DateTime))
							p.SetValue(instance, DateTime.Today, null);
						else if (pt == typeof(int))
							p.SetValue(instance, rnd.Next(1000, 10000), null);
						else if (pt == typeof(long))
							p.SetValue(instance, (long)rnd.Next(1000, 10000), null);
						else if (pt == typeof(decimal))
							p.SetValue(instance, rnd.Next(1000, 10000) / 10m, null);
						else if (pt == typeof(double))
							p.SetValue(instance, rnd.NextDouble() * 1000, null);
						else if (pt == typeof(float))
							p.SetValue(instance, rnd.Next(1000, 10000) / 10f, null);
						else if (pt == typeof(byte[]))
						{
							var buf = new byte[rnd.Next(1, 5)];
							rnd.NextBytes(buf);
							p.SetValue(instance, buf, null);
						}
					}
				}
				return instance;
			}
			catch
			{
				return ctor != null ? ctor.Invoke(null) : FormatterServices.GetUninitializedObject(target);
			}
		}
	}
}
