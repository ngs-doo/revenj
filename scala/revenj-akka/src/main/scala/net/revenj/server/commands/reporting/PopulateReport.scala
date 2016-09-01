package net.revenj.server.commands.reporting

import net.revenj.patterns._
import net.revenj.serialization.Serialization
import net.revenj.server.commands.reporting.PopulateReport.Argument
import net.revenj.server.{CommandResult, ReadOnlyServerCommand}

import scala.concurrent.Future

class PopulateReport(domainModel: DomainModel) extends ReadOnlyServerCommand {

  override def execute[TInput, TOutput](
    locator: ServiceLocator,
    input: Serialization[TInput],
    output: Serialization[TOutput],
    data: TInput): Future[CommandResult[TOutput]] = {

    val arg = input.deserializeRuntime(data, classOf[Argument[TInput]], data.getClass)
    lazy val manifest = domainModel.find(arg.get.ReportName)
    if (!arg.isSuccess) {
      CommandResult.badRequest(arg.failed)
    } else if (manifest.isEmpty) {
      CommandResult.badRequest(s"Unable to find report type: ${arg.get.ReportName}")
    } else if (!classOf[Report[_]].isAssignableFrom(manifest.get)) {
      CommandResult.badRequest(s"Specified type is not a report: ${arg.get.ReportName}")
    } else {
      val report = input.deserializeRuntime[Report[_]](arg.get.Data, manifest.get)
      if (!report.isSuccess) {
        CommandResult.badRequest(s"Error deserializing report: ${arg.get.ReportName}. Reason: ${report.failed.get.getMessage}")
      } else {
        import scala.concurrent.ExecutionContext.Implicits.global
        report.get.populate(locator).map { result =>
          val response = output.serializeRuntime(result)
          if (response.isSuccess) {
            CommandResult[TOutput](Some(response.get), result.toString, 200)
          } else {
            CommandResult[TOutput](None, "Error serializing result.", 500)
          }
        }
      }
    }
  }
}

object PopulateReport {

  case class Argument[TFormat](
    ReportName: String,
    Data: TFormat)

}