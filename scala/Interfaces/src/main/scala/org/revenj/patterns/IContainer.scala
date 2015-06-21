package org.revenj.patterns

import scala.reflect.runtime.universe.TypeTag

trait IContainer extends IServiceLocator {
  def register[TService <: TAs: TypeTag, TAs: TypeTag]: this.type
  protected def register[TService <: TAs: TypeTag, TAs: TypeTag](instance: TService): this.type

  def register[TService: TypeTag] = register[TService, TService]
  def register[TService: TypeTag](instance: TService) = register[TService, TService](instance)

  def createScope(): IContainer
}
