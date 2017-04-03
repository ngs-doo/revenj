package net.revenj

import java.lang.reflect.{Constructor, GenericArrayType, GenericDeclaration, ParameterizedType, TypeVariable, Type => JavaType}
import java.util.concurrent.CopyOnWriteArrayList

import net.revenj.SimpleContainer.Registration
import net.revenj.extensibility.Container

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

private[revenj] class SimpleContainer private(private val parent: Option[SimpleContainer], resolveUnknown: Boolean, mirror: Mirror) extends Container {

  def this(resolveUnknown: Boolean, loader: ClassLoader) {
    this(None, resolveUnknown, runtimeMirror(loader))
    registerGenerics[Option[_]]((locator, args) => locator.resolve(args(0)).toOption)
    registerGenerics[scala.Function0[_]]((locator, args) => () => locator.resolve(args(0)).getOrElse(throw new ReflectiveOperationException("Unable to resolve factory for: " + args(0))))
    registerFactory[Container](c => c.createScope(), singleton = false)
  }

  private val classCache = new TrieMap[Class[_], Array[CtorInfo]]
  private val typeCache = new TrieMap[JavaType, TypeInfo]
  private val typeNameMappings = new TrieMap[String, JavaType]

  private class TypeInfo(val paramType: ParameterizedType) {
    val rawType = paramType.getRawType
    val mappings = new mutable.HashMap[JavaType, JavaType]
    val (constructors, rawClass, genericArguments) = {
      rawType match {
        case rawClass: Class[_] =>
          val genericArguments = paramType.getActualTypeArguments
          val variables = rawClass.getTypeParameters
          var i = 0
          while (i < genericArguments.length) {
            mappings += variables(i) -> genericArguments(i)
            i += 1
          }
          val ctors = rawClass.getConstructors
          val constructors = new Array[CtorInfo](ctors.length)
          i = 0
          while (i < ctors.length) {
            constructors(i) = new CtorInfo(ctors(i))
            i += 1
          }
          (Some(constructors), Some(rawClass), Some(genericArguments))
        case _ => (None, None, None)
      }
    }
    val name = paramType.toString
    val mappedType = typeNameMappings.get(name)
  }

  private class CtorInfo(val ctor: Constructor[_]) {
    val rawTypes = ctor.getParameterTypes
    val genTypes = ctor.getGenericParameterTypes
  }

  private val container = new mutable.HashMap[JavaType, CopyOnWriteArrayList[Registration[AnyRef]]]
  private val closeables = new CopyOnWriteArrayList[AutoCloseable]

  private def tryResolveClass(manifest: Class[_], caller: SimpleContainer): Try[AnyRef] = {
    val constructors = classCache.getOrElseUpdate(manifest, {
      val ctors = manifest.getConstructors
      val arr = new Array[CtorInfo](ctors.length)
      var i = 0
      while (i < ctors.length) {
        arr(i) = new CtorInfo(ctors(i))
        i += 1
      }
      arr
    })
    var result: Option[Try[AnyRef]] = None
    val errors = new ArrayBuffer[Try[AnyRef]](0)
    var x = 0
    while (result.isEmpty && x < constructors.length) {
      val info = constructors(x)
      x += 1
      val genTypes = info.genTypes
      val args = new Array[AnyRef](genTypes.length)
      var success = true
      var i = 0
      while (success && i < genTypes.length) {
        val p = genTypes(i)
        val arg = tryResolve(p, caller)
        if (arg.isFailure) {
          success = false
          errors += arg
        } else {
          args(i) = arg.get
        }
        i += 1
      }
      if (success) {
        val tryInstance = Try {
          info.ctor.newInstance(args: _*).asInstanceOf[AnyRef]
        }
        if (tryInstance.isFailure) {
          errors += tryInstance
        } else {
          result = Some(tryInstance)
        }
      }
    }
    if (result.isEmpty && constructors.length == 0) {
      result = Some(Try {
        manifest.newInstance().asInstanceOf[AnyRef]
      })
    }
    result.getOrElse(errors.headOption.getOrElse(Failure(new ReflectiveOperationException(s"Unable to find constructors for: $manifest"))))
  }

  private def tryResolveType(paramType: ParameterizedType, caller: SimpleContainer): Try[AnyRef] = {
    val typeInfo = typeCache.getOrElseUpdate(paramType, {
      new TypeInfo(paramType)
    })
    if (typeInfo.rawClass.isEmpty) {
      Failure(new ReflectiveOperationException(s"$paramType is not an instance of Class<?> and cannot be resolved"))
    } else {
      getRegistration(typeInfo.rawClass.get) match {
        case Some(registration) if registration.biFactory.isDefined && typeInfo.genericArguments.isDefined =>
          Try {
            registration.biFactory.get(caller, typeInfo.genericArguments.get)
          }
        case _ =>
          if (typeInfo.constructors.isDefined && typeInfo.constructors.isEmpty && typeInfo.mappedType.isDefined) {
            tryResolve(typeInfo.mappedType.get, caller)
          } else {
            val mappings = typeInfo.mappings
            tryResolveTypeFrom(typeInfo, mappings, caller)
          }
      }
    }
  }

  private def tryResolveTypeFrom(typeInfo: TypeInfo, mappings: mutable.HashMap[JavaType, JavaType], caller: SimpleContainer): Try[AnyRef] = {
    typeInfo.constructors match {
      case Some(constructors) =>
        var result: Option[Try[AnyRef]] = None
        val errors = new ArrayBuffer[Try[AnyRef]](0)
        var x = 0
        while (result.isEmpty && x < constructors.length) {
          val info = constructors(x)
          x += 1
          var success = true
          val genTypes = info.genTypes
          val args = new Array[AnyRef](genTypes.length)
          var i = 0
          while (success && i < genTypes.length) {
            genTypes(i) match {
              case nestedType: ParameterizedType =>
                val nestedInfo = typeCache.getOrElseUpdate(nestedType, {
                  new TypeInfo(nestedType)
                })
                if (nestedInfo.rawClass.isEmpty) {
                  success = false
                  errors += Failure(new ReflectiveOperationException(s"Nested parametrized type: $nestedType is not an instance of Class<?>. Error while resolving constructor: ${info.ctor}"))
                } else if (nestedInfo.rawClass.get eq classOf[Option[_]]) {
                  args(i) = tryResolve(nestedInfo.genericArguments.get(0), caller).toOption
                } else {
                  val nestedMappings = new mutable.HashMap[JavaType, JavaType]
                  nestedMappings ++= typeInfo.mappings
                  val iter = nestedInfo.mappings.iterator
                  while(iter.hasNext) {
                    val (key, value) = iter.next()
                    nestedMappings += key -> nestedMappings.getOrElse(value, value)
                  }
                  val arg = tryResolveTypeFrom(nestedInfo, nestedMappings, caller)
                  if (arg.isFailure) {
                    success = false
                    errors += arg
                  } else {
                    args(i) = arg.get
                  }
                }
              case p@_ =>
                var c: Option[JavaType] = Some(p)
                while (c.isDefined && c.get.isInstanceOf[TypeVariable[_ <: GenericDeclaration]]) {
                  c = mappings.get(c.get)
                  if (c.isEmpty) {
                    success = false
                    errors += Failure(new ReflectiveOperationException(s"Unable to find mapping for $p"))
                  }
                }
                if (success) {
                  val arg = tryResolve(c.get, caller)
                  if (arg.isFailure) {
                    success = false
                    errors += arg
                  } else {
                    args(i) = arg.get
                  }
                }
            }
            i += 1
          }
          if (success) {
            val tryInstance = Try {
              info.ctor.newInstance(args: _*).asInstanceOf[AnyRef]
            }
            if (tryInstance.isFailure) {
              errors += tryInstance
            } else {
              result = Some(tryInstance)
            }
          }
        }
        result.getOrElse(errors.headOption.getOrElse(Failure(new ReflectiveOperationException(s"Unable to find constructors for: ${typeInfo.rawType}"))))
      case _ =>
        Failure(new ReflectiveOperationException(s"Unable to find constructors for: ${typeInfo.rawType}"))
    }
  }

  private def getRegistration(paramType: JavaType): Option[Registration[AnyRef]] = {
    container.get(paramType) match {
      case Some(registrations) =>
        Some(registrations.get(registrations.size - 1))
      case _ =>
        if (parent.isDefined) parent.get.getRegistration(paramType) else None
    }
  }

  def resolve(tpe: JavaType): Try[AnyRef] = tryResolve(tpe, this)

  def tryResolve[T: TypeTag]: Try[T] = {
    Utils.findType(typeOf[T], mirror) match {
      case Some(tpe) => resolve(tpe).map(_.asInstanceOf[T])
      case _ => Failure(new ReflectiveOperationException("Invalid type tag argument"))
    }
  }

  private def tryResolve(paramType: JavaType, caller: SimpleContainer): Try[AnyRef] = {
    getRegistration(paramType) match {
      case Some(registration) =>
        if (registration.biFactory.isDefined && paramType.isInstanceOf[ParameterizedType]) {
          val pt = paramType.asInstanceOf[ParameterizedType]
          val typeInfo = typeCache.getOrElseUpdate(paramType, {
            new TypeInfo(pt)
          })
          if (typeInfo.genericArguments.isDefined) {
            Try {
              registration.biFactory.get(caller, typeInfo.genericArguments.get)
            }
          } else {
            resolveRegistration(registration, caller)
          }
        } else {
          resolveRegistration(registration, caller)
        }
      case _ =>
        val basicReg = {
          typeNameMappings.get(paramType.toString) match {
            case Some(basicType) => getRegistration(basicType)
            case _ => None
          }
        }
        if (basicReg.isDefined) {
          resolveRegistration(basicReg.get, caller)
        } else {
          def resolveClass() = {
            paramType match {
              case target: Class[_] =>
                if (target.isArray) {
                  tryResolveCollection(target.getComponentType, target.getComponentType, caller)
                } else if (resolveUnknown) {
                  if (target.isInterface) {
                    Failure(new ReflectiveOperationException(s"""$paramType is not an class and cannot be resolved since it's not registered in the container.
Try resolving implementation instead."""))
                  } else {
                    tryResolveClass(target, caller)
                  }
                } else if (target.isInterface) {
                  Failure(new ReflectiveOperationException(s"""$paramType is not registered in the container.
Since $paramType is an interface, it must be registered into the container."""))
                } else {
                  Failure(new ReflectiveOperationException(s"""$paramType is not registered in the container.
If you wish to resolve types not registered in the container, specify revenj.resolveUnknown=true in Properties configuration."""))
                }
              case _ =>
                Failure(new ReflectiveOperationException(s"$paramType is not an instance of Class<?> and cannot be resolved since it's not registered in the container."))
            }
          }
          def resolveArray(argumentType: JavaType) = {
            argumentType match {
              case pt: Class[_] =>
                tryResolveCollection(pt, argumentType, caller)
              case pt: ParameterizedType =>
                pt.getRawType match {
                  case rawClass: Class[_] =>
                    tryResolveCollection(rawClass, pt, caller)
                  case _ =>
                    resolveClass()
                }
              case _ =>
                resolveClass()
            }
          }
          paramType match {
            case gat: GenericArrayType =>
              resolveArray(gat.getGenericComponentType)
            case pt: ParameterizedType if pt.getRawType == seqSignature =>
              resolveArray(pt.getActualTypeArguments.head).map(_.asInstanceOf[Array[_]].toSeq)
            case pt: ParameterizedType =>
              tryResolveType(pt, caller)
            case _ =>
              resolveClass()
          }
        }
    }
  }

  private val seqSignature = classOf[scala.collection.Seq[_]]

  private def tryResolveCollection(container: Class[_], element: JavaType, caller: SimpleContainer): Try[AnyRef] = {
    val registrations = new CopyOnWriteArrayList[Registration[AnyRef]]
    var current: Option[SimpleContainer] = Some(caller)
    do {
      current.get.container.get(element) match {
        case Some(found) => registrations.addAll(0, found)
        case _ =>
      }
      current = current.get.parent
    } while (current.isDefined)
    if (registrations.isEmpty) {
      Success(java.lang.reflect.Array.newInstance(container, 0))
    } else {
      val result = new mutable.ArrayBuffer[AnyRef](registrations.size)
      var i = 0
      while (i < registrations.size) {
        val item = resolveRegistration(registrations.get(i), caller)
        if (item.isSuccess) {
          result += item.get
        }
        i += 1
      }
      val instance = java.lang.reflect.Array.newInstance(container, result.size).asInstanceOf[Array[AnyRef]]
      i = 0
      while (i < instance.length) {
        instance(i) = result(i)
        i += 1
      }
      Success(instance)
    }
  }

  private def resolveRegistration(registration: Registration[AnyRef], caller: SimpleContainer): Try[AnyRef] = {
    if (registration.instance.isDefined) {
      Success(registration.instance.get)
    } else if (registration.singleFactory.isDefined) {
      Try {
        if (registration.singleton) {
          this synchronized {
            if (registration.promoted) {
              registration.instance
            } else {
              val instance = registration.singleFactory.get(this)
              instance match {
                case closeable: AutoCloseable =>
                  closeables.add(closeable)
                case _ =>
              }
              registration.promoteToSingleton(instance)
              instance
            }
          }
        } else {
          registration.singleFactory.get(this)
        }
      }
    } else {
      if (registration.singleton) {
        this synchronized {
          if (registration.promoted) {
            Success(registration.instance)
          } else if (registration.manifest.isDefined) {
            val tryInstance = tryResolveClass(registration.manifest.get, caller)
            if (tryInstance.isSuccess) {
              tryInstance.get match {
                case closeable: AutoCloseable =>
                  closeables.add(closeable)
                case _ =>
              }
              registration.promoteToSingleton(tryInstance.get)
            }
            tryInstance
          } else {
            Failure(new ReflectiveOperationException("Unable to resolve: " + registration))
          }
        }
      } else if (registration.manifest.isDefined) {
        tryResolveClass(registration.manifest.get, caller)
      } else {
        Failure(new ReflectiveOperationException(s"Unable to resolve: $registration"))
      }
    }
  }

  private def addToRegistry[T](paramType: JavaType, registration: Registration[T]): this.type = {
    val registrations = container.getOrElseUpdate(paramType, {
      new CopyOnWriteArrayList[Registration[AnyRef]]()
    })
    typeNameMappings.put(paramType.toString, paramType)
    registrations.add(registration.asInstanceOf[Registration[AnyRef]])
    this
  }

  override def registerType[T](manifest: JavaType, implementation: Class[T], singleton: Boolean = false): this.type = {
    val clSym = mirror.classSymbol(implementation)
    if(clSym.isModuleClass) {
      val instance = mirror.reflectModule(clSym.module.asModule).instance.asInstanceOf[T]
      addToRegistry(manifest, new Registration[T](this, instance))
    } else {
      addToRegistry(manifest, new Registration[T](this, implementation, singleton))
    }
  }

  override def registerAs[T, S <: T](singleton: Boolean = false)(implicit manifest: TypeTag[T], implementation: ClassTag[S]): this.type = {
    val signature = Utils.findType(manifest.tpe, mirror).getOrElse(throw new IllegalArgumentException(s"Unable to find Java type for $manifest"))
    val clSym = mirror.classSymbol(implementation.runtimeClass)
    if(clSym.isModuleClass) {
      val instance = mirror.reflectModule(clSym.module.asModule).instance.asInstanceOf[T]
      addToRegistry(signature, new Registration[T](this, instance))
    } else {
      addToRegistry(signature, new Registration(this, implementation.runtimeClass, singleton))
    }
  }

  override def registerInstance[T: TypeTag](service: T, handleClose: Boolean = false): this.type = {
    if (handleClose && service.isInstanceOf[AutoCloseable]) {
      closeables.add(service.asInstanceOf[AutoCloseable])
    }
    val paramType = Utils.findType(typeOf[T], mirror).getOrElse(throw new IllegalArgumentException(s"Unable to register ${typeOf[T]} to container"))
    addToRegistry(paramType, new Registration[T](this, service))
  }

  override def registerFactory[T: TypeTag](factory: Container => T, singleton: Boolean = false): this.type = {
    val paramType = Utils.findType(typeOf[T], mirror).getOrElse(throw new IllegalArgumentException(s"Unable to register ${typeOf[T]} to container"))
    addToRegistry(paramType, new Registration[T](this, factory, singleton))
  }

  override def registerGenerics[T: TypeTag](factory: (Container, Array[JavaType]) => T): this.type = {
    addToRegistry(mirror.runtimeClass(typeOf[T]), new Registration[T](this, factory, singleton = false))
  }

  override def createScope(): Container = {
    new SimpleContainer(Some(this), resolveUnknown, mirror)
  }

  def close(): Unit = {
    container.clear
    val it = closeables.iterator
    while (it.hasNext) {
      it.next().close()
    }
    closeables.clear()
  }
}

private object SimpleContainer {

  private class Registration[T]
  (
    owner: SimpleContainer,
    val manifest: Option[Class[T]],
    var instance: Option[T],
    val singleFactory: Option[Container => T],
    val biFactory: Option[(Container, Array[JavaType]) => T],
    val singleton: Boolean) {

    def this(owner: SimpleContainer, manifest: Class[T], singleton: Boolean) {
      this(owner, Some(manifest), None, None, None, singleton)
    }

    def this(owner: SimpleContainer, instance: T) {
      this(owner, None, Some(instance), None, None, singleton = true)
    }

    def this(owner: SimpleContainer, factory: Container => T, singleton: Boolean) {
      this(owner, None, None, Some(factory), None, singleton)
    }

    def this(owner: SimpleContainer, factory: (Container, Array[JavaType]) => T, singleton: Boolean) {
      this(owner, None, None, None, Some(factory), singleton)
    }

    var promoted: Boolean = false

    def promoteToSingleton(value: T): Unit = {
      promoted = true
      this.instance = Some(value)
    }
  }

}