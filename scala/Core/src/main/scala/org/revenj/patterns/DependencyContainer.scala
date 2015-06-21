package org.revenj.patterns

import java.lang.reflect.{ Array => JArray }
import scala.reflect.runtime.universe._

import org.picocontainer.DefaultPicoContainer
import org.picocontainer.MutablePicoContainer

class DependencyContainer(
    container: MutablePicoContainer
  ) extends IContainer {

  def this() = this(new DefaultPicoContainer)

  register[IServiceLocator](this)
  register[IContainer](this)

  def resolve[T](implicit ev: TypeTag[T]): T =
    (ev.tpe match {
      case TypeRef(_, col, tpe :: Nil) if col == definitions.ArrayClass =>
        val clazz = ev.mirror.runtimeClass(tpe)
        val components = container.getComponents(clazz)
        val arr = JArray.newInstance(clazz, components.size()).asInstanceOf[Array[AnyRef]]
        components.toArray(arr)

      case TypeRef(_, tpe, _) =>
        container.getComponent(ev.tpe).ensuring(
          _ != null
        , "Container could not locate class of type: " + ev.tpe
        )

      case _ =>
        sys.error("Container could not locate class of type: " + ev.tpe)
    }).asInstanceOf[T]

  def register[TService <: TAs, TAs](instance: TService)(implicit evServ: TypeTag[TService], evAs: TypeTag[TAs]) = {
    container.addComponent(evAs.tpe, instance)
    this
  }

  def register[TService <: TAs, TAs](implicit evServ: TypeTag[TService], evAs: TypeTag[TAs]) = {
    val clazzService = evServ.mirror.runtimeClass(evServ.tpe)
    container.addComponent(evAs.tpe, clazzService)
    this
  }

  def createScope() =
    new DependencyContainer(container.makeChildContainer())
}
