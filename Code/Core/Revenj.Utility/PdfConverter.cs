using System;
using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Linq;

namespace Revenj.Utility
{
	/// <summary>
	/// Utility for converting files to PDF.
	/// External PdfConverter utility will be invoked for conversion.
	/// Path to PdfConverter can't have whitespaces in it (since tool will be invoked using cmd.exe)
	/// PdfConverterTimeout specify maximum amount of time conversion can take (20 seconds by default).
	/// </summary>
	public static class PdfConverter
	{
		private static int PdfConverterTimeout;
		private static string ConverterPath;
		private static bool IsWindows;

		static PdfConverter()
		{
			var ct = ConfigurationManager.AppSettings["PdfConverterTimeout"];
			if (!int.TryParse(ct, out PdfConverterTimeout))
				PdfConverterTimeout = 20;
			ConverterPath = ConfigurationManager.AppSettings["PdfConverter"];
			if (string.IsNullOrEmpty(ConverterPath))
				throw new ConfigurationErrorsException("Missing configuration for PdfConverter");
			if (!File.Exists(ConverterPath))
				ConverterPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, ConverterPath);
			if (!File.Exists(ConverterPath))
				throw new ConfigurationErrorsException("Can't find PdfConverter application");
			if (ConverterPath.Any(c => char.IsWhiteSpace(c)))
				throw new ConfigurationErrorsException("PdfConverter must be on path without whitespace characters");
			var platform = System.Environment.OSVersion.Platform;
			IsWindows = platform != PlatformID.MacOSX && platform != PlatformID.Unix;
		}
		/// <summary>
		/// Convert provided file content to PDF. 
		/// Specify extension of the file.
		/// </summary>
		/// <param name="content">file content</param>
		/// <param name="ext">file extension</param>
		/// <returns>PDF converted file</returns>
		public static byte[] Convert(byte[] content, string ext)
		{
			var from = TemporaryResources.CreateFile(ext);
			var to = from + ".pdf";
			File.WriteAllBytes(from, content);
			RunConverter(from);
			var result = File.ReadAllBytes(to);
			File.Delete(from);
			File.Delete(to);
			return result;
		}
		/// <summary>
		/// Convert provided stream content to PDF.
		/// Specify extension of the file
		/// </summary>
		/// <param name="content">file content</param>
		/// <param name="ext">file extension</param>
		/// <param name="disposeStream">dispose provided stream after conversion</param>
		/// <returns>PDF converted stream</returns>
		public static Stream Convert(Stream content, string ext, bool disposeStream)
		{
			var from = TemporaryResources.CreateFile(ext);
			var to = from + ".pdf";
			var fs = new FileStream(from, FileMode.Create, FileAccess.Write);
			content.CopyTo(fs);
			fs.Close();
			if (disposeStream)
				content.Dispose();
			RunConverter(from);
			var cms = ChunkedMemoryStream.Create();
			using (var f = new FileStream(to, FileMode.Open, FileAccess.Read))
			{
				f.CopyTo(cms);
			}
			File.Delete(from);
			File.Delete(to);
			cms.Position = 0;
			return cms;
		}

		private static void RunConverter(string from)
		{
			Process process;
			if (IsWindows)
			{
				//Workaround for Windows. On Windows Process can hang. Use cmd.exe to avoid such issue instead.
				process =
					Process.Start(
						new ProcessStartInfo
						{
							FileName = "cmd.exe",
							Arguments = string.Format("/c {0} \"{1}\"", ConverterPath, from),
							WindowStyle = ProcessWindowStyle.Hidden,
							UseShellExecute = false
						});
			}
			else
			{
				process =
					Process.Start(
						new ProcessStartInfo
						{
							FileName = ConverterPath,
							Arguments = "\"" + from + "\"",
							WindowStyle = ProcessWindowStyle.Hidden,
							UseShellExecute = false
						});
			}
			if (!process.WaitForExit(PdfConverterTimeout * 1000))
			{
				try { process.Dispose(); }
				catch { }
				throw new TimeoutException("Timeout creating PDF. ");
			}
		}
	}
}
