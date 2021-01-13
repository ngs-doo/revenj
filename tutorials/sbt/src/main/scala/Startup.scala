import net.revenj.server.WebServer

object Startup {
  def main(args: Array[String]): Unit = {
    WebServer.start("localhost", 8080)
  }
}