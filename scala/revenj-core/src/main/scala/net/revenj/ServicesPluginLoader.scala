package net.revenj

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{URL, URLEncoder}
import java.nio.charset.Charset

import net.revenj.extensibility.PluginLoader

import scala.collection.mutable.ArrayBuffer
import scala.reflect.runtime.universe._

private[revenj] class ServicesPluginLoader(loader: ClassLoader) extends PluginLoader {
  private val PREFIX: String = "META-INF/services/"
  private val UTF8: Charset = Charset.forName("UTF-8")
  private val mirror = runtimeMirror(loader)

  def find[T: TypeTag]: Seq[Class[T]] = {
    val plugins = new ArrayBuffer[Class[T]]()
    val scalaType = typeOf[T].dealias
    val javaTypeName = Utils.findType(scalaType, mirror).map(_.getTypeName).getOrElse(scalaType.toString)

    val fullName = PREFIX + URLEncoder.encode(javaTypeName, "UTF-8")
    val manifest = mirror.runtimeClass(typeOf[T].dealias).asInstanceOf[Class[T]]
    //TODO: release class loader to avoid locking up jars on Windows
    val configs = loader.getResources(fullName)
    while (configs.hasMoreElements) {
      val url = configs.nextElement
      lookupServices[T](manifest, url, plugins)
    }
    plugins
  }

  private def lookupServices[T](manifest: Class[T], u: URL, plugins: ArrayBuffer[Class[T]]): Unit = {
    val stream = u.openStream
    val reader = new BufferedReader(new InputStreamReader(stream, UTF8))
    try {
      var line: String = null
      while ({line = reader.readLine(); line != null}) {
        val ci = line.indexOf('#')
        if (ci >= 0) line = line.substring(0, ci)
        line = line.trim
        val n = line.length
        if (n != 0) {
          if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) throw new IOException("Invalid configuration for " + manifest + " in " + u)
          var cp = line.codePointAt(0)
          if (!Character.isJavaIdentifierStart(cp)) throw new IOException("Invalid configuration for " + manifest + " in " + u)
          var i = Character.charCount(cp)
          while (i < n) {
            cp = line.codePointAt(i)
            if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) throw new IOException("Invalid configuration for " + manifest + " in " + u)
            i += Character.charCount(cp)
          }
          val service = loader.loadClass(line)
          plugins += service.asInstanceOf[Class[T]]
        }
      }
    } finally {
      stream.close()
      reader.close()
    }
  }
}
