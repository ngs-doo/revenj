using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Security.Principal;
using System.Text;
using Revenj.Api;
using Revenj.Utility;

namespace Revenj.Http
{
	internal class HttpSocketContext : IRequestContext, IResponseContext
	{
		private static readonly Encoding UTF8 = Encoding.UTF8;
		private static readonly Encoding ASCII = Encoding.ASCII;

		private static readonly byte[][] HttpResponse10 = new byte[406][];
		private static readonly byte[][] HttpResponse11 = new byte[406][];
		private static readonly byte[] ContentLength = Encoding.UTF8.GetBytes("Content-Length: ");
		private static readonly byte[] ZeroContentLength = ASCII.GetBytes("Content-Length: 0\r\n\r\n");
		private static readonly byte[] ContentType = ASCII.GetBytes("Content-Type: ");
		private static readonly byte[] PlainTextContentType = ASCII.GetBytes("Content-Type: text/plain; charset=UTF-8\r\n");
		private static readonly byte[] JsonContentType = ASCII.GetBytes("Content-Type: application/json\r\n");
		private static readonly byte[] ConnectionClose = ASCII.GetBytes("Connection: close\r\n");
		private static readonly byte[] ConnectionKeepAlive = ASCII.GetBytes("Connection: Keep-Alive\r\n");
		private static readonly byte[] ServerName = ASCII.GetBytes("Server: Revenj " + typeof(HttpSocketContext).Assembly.GetName().Version.ToString(3) + "\r\n");
		private static readonly byte[] DateNow = ASCII.GetBytes("Date: Fri, 13 Jul 2015 20:00:00 GMT\r\n");
		private static long LastDate = DateTime.MinValue.Second;
		private static readonly byte[][] DateDayNames = new byte[7][];
		private static readonly byte[][] DateNumbers = new byte[100][];
		private static readonly byte[][] DateDayMonths = new byte[12][];

		private static readonly byte Space = 32;
		private static readonly byte CR = 13;
		private static readonly byte LF = 10;

		static HttpSocketContext()
		{
			SetupResponse(HttpResponse10, "HTTP/1.0");
			SetupResponse(HttpResponse11, "HTTP/1.1");

			DateDayNames[0] = ASCII.GetBytes("Mon");
			DateDayNames[1] = ASCII.GetBytes("Tue");
			DateDayNames[2] = ASCII.GetBytes("Wed");
			DateDayNames[3] = ASCII.GetBytes("Thu");
			DateDayNames[4] = ASCII.GetBytes("Fri");
			DateDayNames[5] = ASCII.GetBytes("Sat");
			DateDayNames[6] = ASCII.GetBytes("Sun");

			DateDayMonths[0] = ASCII.GetBytes("Jan");
			DateDayMonths[1] = ASCII.GetBytes("Feb");
			DateDayMonths[2] = ASCII.GetBytes("Mar");
			DateDayMonths[3] = ASCII.GetBytes("Apr");
			DateDayMonths[4] = ASCII.GetBytes("May");
			DateDayMonths[5] = ASCII.GetBytes("Jun");
			DateDayMonths[6] = ASCII.GetBytes("Jul");
			DateDayMonths[7] = ASCII.GetBytes("Aug");
			DateDayMonths[8] = ASCII.GetBytes("Sep");
			DateDayMonths[9] = ASCII.GetBytes("Oct");
			DateDayMonths[10] = ASCII.GetBytes("Nov");
			DateDayMonths[11] = ASCII.GetBytes("Dec");

			for (int i = 0; i < 10; i++)
				DateNumbers[i] = ASCII.GetBytes("0" + i);
			for (int i = 10; i < 100; i++)
				DateNumbers[i] = ASCII.GetBytes(i.ToString());
		}

		private static void SetupResponse(byte[][] response, string method)
		{
			for (int i = 0; i < 406; i++)
				response[i] = ASCII.GetBytes(method + " " + (i + 100) + @"\r\n");
			response[0] = ASCII.GetBytes(method + " 100 Continue\r\n");
			response[100] = ASCII.GetBytes(method + " 200 OK\r\n");
			response[101] = ASCII.GetBytes(method + " 201 Created\r\n");
			response[102] = ASCII.GetBytes(method + " 202 Accepted\r\n");
			response[104] = ASCII.GetBytes(method + " 204 No Content\r\n");
			response[202] = ASCII.GetBytes(method + " 302 Found\r\n");
			response[204] = ASCII.GetBytes(method + " 304 Not Modified\r\n");
			response[300] = ASCII.GetBytes(method + " 400 Bad Request\r\n");
			response[301] = ASCII.GetBytes(method + " 401 Unauthorized\r\n");
			response[303] = ASCII.GetBytes(method + " 403 Forbidden\r\n");
			response[304] = ASCII.GetBytes(method + " 404 Not Found\r\n");
			response[305] = ASCII.GetBytes(method + " 405 Method Not Allowed\r\n");
			response[306] = ASCII.GetBytes(method + " 406 Not Acceptable\r\n");
			response[309] = ASCII.GetBytes(method + " 409 Conflict\r\n");
			response[311] = ASCII.GetBytes(method + " 411 Length Required\r\n");
			response[313] = ASCII.GetBytes(method + " 413 Request Entity Too Large\r\n");
			response[314] = ASCII.GetBytes(method + " 414 Request-URI Too Long\r\n");
			response[315] = ASCII.GetBytes(method + " 415 Unsupported Media Type\r\n");
			response[400] = ASCII.GetBytes(method + " 500 Internal Server Error\r\n");
			response[401] = ASCII.GetBytes(method + " 501 Not Implemented\r\n");
			response[403] = ASCII.GetBytes(method + " 503 Service Unavailable\r\n");
			response[405] = ASCII.GetBytes(method + " 505 HTTP Version Not Supported\r\n");
		}

		public readonly ChunkedMemoryStream Stream;
		private readonly byte[] Temp = new byte[4096];

		private int totalBytes;
		private int positionInTmp;

		public readonly int Limit;

		private readonly string Prefix;

		public HttpSocketContext(string prefix, int limit)
		{
			this.Prefix = prefix;
			this.Limit = limit;
			Stream = ChunkedMemoryStream.Static();
		}

		private int ReadUntil(Socket socket, byte match, int position)
		{
			int totalRead = positionInTmp;
			int retries = 0;
			do
			{
				for (int i = position; i < totalRead; i++)
				{
					if (Temp[i] == match)
					{
						totalBytes += totalRead - positionInTmp;
						positionInTmp = totalRead;
						return i;
					}
				}
				var size = socket.Receive(Temp, totalRead, Temp.Length - totalRead, SocketFlags.None);
				retries++;
				if (size == 0) return -1;
				totalRead += size;
			} while (retries < 100 && totalRead < Temp.Length);
			return -1;
		}

		public string HttpMethod;
		public string RawUrl;
		public string HttpProtocolVersion;
		public string AbsolutePath;

		private readonly Dictionary<string, string> RequestHeaders = new Dictionary<string, string>();
		private readonly Dictionary<string, string> ResponseHeaders = new Dictionary<string, string>();

		public string GetRequestHeader(string name)
		{
			string result;
			RequestHeaders.TryGetValue(name, out result);
			return result;
		}

		private byte[][] HttpResponse;
		private bool IsHttp10;
		private HttpStatusCode ResponseStatus;
		private long? ResponseLength;
		private string ResponseContentType;
		private UriTemplateMatch TemplateMatch;
		private bool ResponseIsJson;

		private static string ReadMethod(int len, byte[] buf)
		{
			if (len == 3)
			{
				if (buf[0] == 'G' && buf[1] == 'E' && buf[2] == 'T')
					return "GET";
				if (buf[0] == 'P' && buf[1] == 'U' && buf[2] == 'T')
					return "PUT";
			}
			else if (len == 4)
			{
				if (buf[0] == 'P' && buf[1] == 'O' && buf[2] == 'S' && buf[3] == 'T')
					return "POST";
			}
			return ASCII.GetString(buf, 0, len);
		}

		private string ReadProtocol(int end)
		{
			if (Temp[end - 8] == 32)
			{
				string method;
				var last = Temp[end];
				if (last == 48)
				{
					IsHttp10 = true;
					HttpResponse = HttpResponse10;
					method = "HTTP/1.0";
				}
				else if (last == 49)
				{
					IsHttp10 = false;
					HttpResponse = HttpResponse11;
					method = "HTTP/1.1";
				}
				else return null;
				if (Temp[end - 7] != (byte)'H') return null;
				if (Temp[end - 6] != (byte)'T') return null;
				if (Temp[end - 5] != (byte)'T') return null;
				if (Temp[end - 4] != (byte)'P') return null;
				if (Temp[end - 3] != (byte)'/') return null;
				if (Temp[end - 2] != (byte)'1') return null;
				if (Temp[end - 1] != (byte)'.') return null;
				return method;
			}
			HttpResponse = HttpResponse11;
			return null;
		}

		public bool Process(Socket socket)
		{
			positionInTmp = 0;
			totalBytes = 0;
			var methodEnd = ReadUntil(socket, Space, 0);
			if (methodEnd == -1) return ReturnError(socket, 505);
			HttpMethod = ReadMethod(methodEnd, Temp);
			var rowEnd = ReadUntil(socket, LF, methodEnd + 1);
			if (rowEnd == -1 || rowEnd < 12) return ReturnError(socket, 505);
			RequestHeaders.Clear();
			ResponseHeaders.Clear();
			HttpProtocolVersion = ReadProtocol(rowEnd - 2);
			if (HttpProtocolVersion == null)
			{
				ReturnError(socket, 505, "Only HTTP/1.1 and HTTP/1.0 supported (partially)", false);
				return false;
			}
			RawUrl = Encoding.UTF8.GetString(Temp, HttpMethod.Length + 1, rowEnd - 3 - HttpProtocolVersion.Length - HttpMethod.Length);
			int askSign = RawUrl.IndexOf('?');
			AbsolutePath = askSign == -1 ? RawUrl : RawUrl.Substring(0, askSign);
			ResponseStatus = HttpStatusCode.OK;
			ResponseLength = null;
			ResponseContentType = null;
			TemplateMatch = null;
			ResponseIsJson = false;
			do
			{
				var start = rowEnd + 1;
				rowEnd = ReadUntil(socket, CR, start);
				if (rowEnd == start) break;
				else if (rowEnd == -1) return ReturnError(socket, 414);
				else
				{
					int i = start;
					for (; i < rowEnd; i++)
						if (Temp[i] == ':')
							break;
					if (i == rowEnd) return ReturnError(socket, 414);
					string name = Encoding.ASCII.GetString(Temp, start, i - start);
					if (Temp[i + 1] == 32) i++;
					string value = Encoding.ASCII.GetString(Temp, i + 1, rowEnd - i - 1);
					var lowercase = name.ToLowerInvariant();
					RequestHeaders[lowercase] = value;
				}
				rowEnd++;
			} while (positionInTmp <= Temp.Length);
			if (HttpMethod == "POST" || HttpMethod == "PUT")
			{
				var cl = RequestHeaders["content-length"];
				long len = 0;
				if (cl != null)
				{
					if (!long.TryParse(cl, out len)) return ReturnError(socket, 411);
					if (len > Limit) return ReturnError(socket, 413);
				}
				else return ReturnError(socket, 411);
				rowEnd += 2;
				totalBytes = positionInTmp - rowEnd;
				Stream.SetLength(0);
				Stream.Write(Temp, rowEnd, totalBytes);
				while (totalBytes < len)
				{
					var size = socket.Receive(Temp);
					Stream.Write(Temp, 0, size);
					totalBytes += size;
				}
				Stream.Position = 0;
			}
			return true;
		}

		private RouteMatch Route;
		public IPrincipal Principal { get; private set; }

		internal void ForRouteWithAuth(RouteMatch route, IPrincipal principal)
		{
			this.Route = route;
			this.Principal = principal;
		}

		internal bool Return(Stream stream, Socket socket)
		{
			var http = HttpResponse[(int)ResponseStatus - 100];
			Buffer.BlockCopy(http, 0, Temp, 0, http.Length);
			var offset = http.Length;
			if (ResponseIsJson)
			{
				Buffer.BlockCopy(JsonContentType, 0, Temp, offset, JsonContentType.Length);
				offset += JsonContentType.Length;
			}
			else if (ResponseContentType != null)
			{
				offset = AddContentType(ResponseContentType, offset);
			}
			foreach (var kv in ResponseHeaders)
			{
				offset += Encoding.ASCII.GetBytes(kv.Key, 0, kv.Key.Length, Temp, offset);
				Temp[offset++] = 58;
				Temp[offset++] = 32;
				offset += Encoding.ASCII.GetBytes(kv.Value, 0, kv.Value.Length, Temp, offset);
				Temp[offset++] = 13;
				Temp[offset++] = 10;
			}
			bool keepAlive;
			if (IsHttp10)
			{
				if (GetRequestHeader("connection") == "Keep-Alive")
				{
					keepAlive = true;
					Buffer.BlockCopy(ConnectionKeepAlive, 0, Temp, offset, ConnectionKeepAlive.Length);
					offset += ConnectionKeepAlive.Length;
				}
				else
				{
					keepAlive = false;
					socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, false);
					Buffer.BlockCopy(ConnectionClose, 0, Temp, offset, ConnectionClose.Length);
					offset += ConnectionClose.Length;
				}
			}
			else
			{
				if (GetRequestHeader("connection") == "close")
				{
					keepAlive = false;
					socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, false);
					Buffer.BlockCopy(ConnectionClose, 0, Temp, offset, ConnectionClose.Length);
					offset += ConnectionClose.Length;
				}
				else keepAlive = true;
			}
			offset = AddServerAndDate(offset);
			var cms = stream as ChunkedMemoryStream;
			if (cms != null)
			{
				offset = AddContentLength(cms.Length, offset);
				socket.Send(Temp, offset, SocketFlags.None);
				cms.Send(socket);
				cms.Dispose();
			}
			else if (stream != null)
			{
				try
				{
					long len;
					if (ResponseLength != null)
						len = ResponseLength.Value;
					else if (stream.CanSeek)
						len = stream.Length;
					else
						throw new NotSupportedException("Chunked stream not implemented");
					offset = AddContentLength(len, offset);
					if (len + offset < Temp.Length)
					{
						int pos = 0;
						int size = (int)len;
						do
						{
							pos += stream.Read(Temp, pos + offset, size - pos);
						} while (pos < len);
						socket.Send(Temp, size + offset, SocketFlags.None);
					}
					else
					{
						int pos = 0;
						int size = (int)len;
						socket.Send(Temp, offset, SocketFlags.Partial);
						do
						{
							pos = stream.Read(Temp, 0, Temp.Length);
							socket.Send(Temp, pos, SocketFlags.None);
						} while (pos != 0);
					}
				}
				finally
				{
					stream.Dispose();
				}
			}
			else
			{
				Buffer.BlockCopy(ZeroContentLength, 0, Temp, offset, ZeroContentLength.Length);
				socket.Send(Temp, offset + ZeroContentLength.Length, SocketFlags.None);
			}
			return keepAlive;
		}

		internal bool ReturnError(Socket socket, int status)
		{
			ReturnError(socket, status, null, false);
			return false;
		}

		internal void ReturnError(Socket socket, int status, string message, bool withHeaders)
		{
			var http = HttpResponse[status - 100];
			Buffer.BlockCopy(http, 0, Temp, 0, http.Length);
			var offset = http.Length;
			Buffer.BlockCopy(ConnectionClose, 0, Temp, offset, ConnectionClose.Length);
			offset += ConnectionClose.Length;
			offset = AddServerAndDate(offset);
			socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, false);
			if (message == null)
			{
				Buffer.BlockCopy(ZeroContentLength, 0, Temp, offset, ZeroContentLength.Length);
				socket.Send(Temp, offset + ZeroContentLength.Length, SocketFlags.None);
				socket.Close();
				return;
			}
			if (withHeaders)
			{
				foreach (var kv in ResponseHeaders)
				{
					offset += Encoding.ASCII.GetBytes(kv.Key, 0, kv.Key.Length, Temp, offset);
					Temp[offset++] = 58;
					Temp[offset++] = 32;
					offset += Encoding.ASCII.GetBytes(kv.Value, 0, kv.Value.Length, Temp, offset);
					Temp[offset++] = 13;
					Temp[offset++] = 10;
				}
			}
			Buffer.BlockCopy(PlainTextContentType, 0, Temp, offset, PlainTextContentType.Length);
			offset += PlainTextContentType.Length;
			var len = Encoding.UTF8.GetByteCount(message);
			offset = AddContentLength(len, offset);
			offset += Encoding.UTF8.GetBytes(message, 0, message.Length, Temp, offset);
			socket.Send(Temp, offset, SocketFlags.None);
			socket.Close();
			return;
		}

		private int AddServerAndDate(int offset)
		{
			var date = DateTime.UtcNow;
			var ticks = date.Ticks / 10000000;
			if (LastDate != ticks)
			{
				var original = ticks;
				var sec = ticks % 60;
				ticks = ticks / 60;
				var min = ticks % 60;
				ticks = ticks / 60;
				var hour = ticks % 24;
				ticks = ticks / 24;
				var dayOfWeek = ticks % 7;
				var dayNameBuf = DateDayNames[dayOfWeek];
				var dayBuf = DateNumbers[date.Day];
				var monthBuf = DateDayMonths[date.Month];
				var yearBuf1 = DateNumbers[date.Year / 100];
				var yearBuf2 = DateNumbers[date.Year % 100];
				var hourBuf = DateNumbers[hour];
				var minBuf = DateNumbers[min];
				var secBuf = DateNumbers[sec];
				Buffer.BlockCopy(DateNow, 0, Temp, offset, 6);
				var start = offset + 6;
				Temp[start] = dayNameBuf[0];
				Temp[start + 1] = dayNameBuf[1];
				Temp[start + 2] = dayNameBuf[2];
				Temp[start + 3] = 44;
				Temp[start + 4] = 32;
				Temp[start + 5] = dayBuf[0];
				Temp[start + 6] = dayBuf[1];
				Temp[start + 7] = 32;
				Temp[start + 8] = monthBuf[0];
				Temp[start + 9] = monthBuf[1];
				Temp[start + 10] = monthBuf[2];
				Temp[start + 11] = 32;
				Temp[start + 12] = yearBuf1[0];
				Temp[start + 13] = yearBuf1[1];
				Temp[start + 14] = yearBuf2[0];
				Temp[start + 15] = yearBuf2[1];
				Temp[start + 16] = 32;
				Temp[start + 17] = hourBuf[0];
				Temp[start + 18] = hourBuf[1];
				Temp[start + 19] = 58;
				Temp[start + 20] = minBuf[0];
				Temp[start + 21] = minBuf[1];
				Temp[start + 22] = 58;
				Temp[start + 23] = secBuf[0];
				Temp[start + 24] = secBuf[1];
				Buffer.BlockCopy(Temp, start, DateNow, 6, 25);
				LastDate = original;
				Buffer.BlockCopy(DateNow, 31, Temp, start + 25, DateNow.Length - 31);
				Buffer.BlockCopy(ServerName, 0, Temp, offset + DateNow.Length, ServerName.Length);
				return offset + DateNow.Length + ServerName.Length;
			}
			Buffer.BlockCopy(DateNow, 0, Temp, offset, DateNow.Length);
			Buffer.BlockCopy(ServerName, 0, Temp, offset + DateNow.Length, ServerName.Length);
			return offset + DateNow.Length + ServerName.Length;
		}

		private int AddContentLength(long len, int offset)
		{
			Buffer.BlockCopy(ContentLength, 0, Temp, offset, ContentLength.Length);
			offset += ContentLength.Length;
			offset = Serialize(len, Temp, offset);
			Temp[offset] = 13;
			Temp[offset + 1] = 10;
			Temp[offset + 2] = 13;
			Temp[offset + 3] = 10;
			return offset + 4;
		}

		private static int Serialize(long value, byte[] target, int offset)
		{
			var abs = (int)value;
			var pos = 10 + offset;
			byte[] num;
			do
			{
				var div = abs / 100;
				var rem = abs - div * 100;
				num = DateNumbers[rem];
				target[pos--] = num[1];
				target[pos--] = num[0];
				abs = div;
				if (abs == 0) break;
			} while (pos > offset);
			if (num[0] == 48) pos++;
			var len = offset + 10 - pos;
			Buffer.BlockCopy(target, pos + 1, target, offset, len);
			return offset + len;
		}

		private int AddContentType(string type, int offset)
		{
			Buffer.BlockCopy(ContentType, 0, Temp, offset, ContentType.Length);
			offset += ContentType.Length;
			offset += Encoding.ASCII.GetBytes(type, 0, type.Length, Temp, offset);
			Temp[offset] = 13;
			Temp[offset + 1] = 10;
			return offset + 2;
		}

		string IRequestContext.Accept
		{
			get { return GetRequestHeader("accept"); }
		}

		long IRequestContext.ContentLength
		{
			get
			{
				var cl = GetRequestHeader("content-length");
				long len;
				long.TryParse(cl, out len);
				return len;
			}
		}

		string IRequestContext.ContentType
		{
			get { return GetRequestHeader("content-type"); }
		}

		Uri IRequestContext.RequestUri
		{
			get { return new Uri(Prefix + RawUrl); }
		}

		UriTemplateMatch IRequestContext.UriTemplateMatch
		{
			get
			{
				if (TemplateMatch == null)
					TemplateMatch = Route.CreateTemplateMatch();
				return TemplateMatch;
			}
			set { TemplateMatch = value; }
		}

		string IRequestContext.GetHeader(string name)
		{
			return GetRequestHeader(name);
		}

		string IResponseContext.ContentType
		{
			get { return ResponseContentType; }
			set
			{
				ResponseContentType = value;
				ResponseHeaders.Remove("Content-Type");
				ResponseIsJson = value == "application/json";
			}
		}

		long IResponseContext.ContentLength
		{
			get { return ResponseLength ?? -1; }
			set { ResponseLength = value; }
		}

		public void AddHeader(string type, string value)
		{
			ResponseHeaders[type] = value;
		}

		HttpStatusCode IResponseContext.StatusCode
		{
			get { return ResponseStatus; }
			set { ResponseStatus = value; }
		}
	}
}
