package net.revenj

import net.revenj.patterns.ReportAspect

class ReportClassAspect extends ReportAspect[ReportMe.Result, ReportMe] {
  override def before(report: ReportMe): ReportMe = {
    ReportClassHandler.calledBefore += 1
    report
  }
  override def after(result: ReportMe.Result): ReportMe.Result = {
    ReportClassHandler.calledAfter += 1
    result
  }
}
object ReportClassHandler {
  var calledBefore = 0
  var calledAfter = 0
}