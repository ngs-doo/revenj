package net.revenj.patterns

import net.revenj.patterns.DataChangeNotification.NotifyInfo

trait EagerNotification extends DataChangeNotification {
  def notify(info: NotifyInfo): Unit
}
