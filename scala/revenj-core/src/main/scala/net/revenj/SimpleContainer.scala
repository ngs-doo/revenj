package net.revenj

import java.lang.reflect.{Constructor, GenericArrayType, GenericDeclaration, InvocationTargetException, ParameterizedType, TypeVariable, Type => JavaType}
import java.util.concurrent.CopyOnWriteArrayList

import net.revenj.extensibility.Container
import net.revenj.extensibility.InstanceScope
import net.revenj.extensibility.InstanceScope._
import net.revenj.patterns.ServiceLocator

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

private[revenj] class SimpleContainer private(private val parent: Option[SimpleContainer], resolveUnknown: Boolean, mirror: Mirror) extends Container {

  import SimpleContainer._

  def this(resolveUnknown: Boolean, loader: ClassLoader) {
    this(None, resolveUnknown, runtimeMirror(loader))
    registerGenerics[Option[_]]((locator, args) => locator.resolve(args(0)).toOption)
    registerGenerics[scala.Function0[_]]((locator, args) => () => locator.resolve(args(0)).getOrElse(throw new ResolutionException(s"Unable to resolve factory for: ${args(0)}", args(0))))
  }

  private val container = new mutable.HashMap[JavaType, CopyOnWriteArrayList[Registration[AnyRef]]]
  private val closeables = new CopyOnWriteArrayList[AutoCloseable]
  private var closed = false

  registerInstance[Container](this, handleClose = false)
  registerInstance[ServiceLocator](this, handleClose = false)

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
        if (tryInstance.isSuccess) {
          result = Some(tryInstance)
        } else tryInstance.failed.get match {
          case tie: InvocationTargetException if tie.getTargetException != null =>
            errors += Failure(tie.getTargetException)
          case _ =>
            errors += tryInstance
        }
      }
    }
    if (result.isEmpty && constructors.length == 0) {
      result = Some(Try {
        manifest.newInstance().asInstanceOf[AnyRef]
      })
    }
    result match {
      case Some(tryResult) =>
        addChainToError(tryResult, manifest)
      case _ =>
        errors.headOption match {
          case Some(tryResult) =>
            addChainToError(tryResult, manifest)
          case _ =>
            Failure(new ResolutionException(s"Unable to find constructors for: $manifest", manifest))
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
                  errors += Failure(new ResolutionException(s"Nested parametrized type: $nestedType is not an instance of Class<?>. Error while resolving constructor: ${info.ctor}", info.ctor.getDeclaringClass))
                } else {
                  val nestedMappings = new mutable.HashMap[JavaType, JavaType]
                  nestedMappings ++= typeInfo.mappings
                  val iter = nestedInfo.mappings.iterator
                  while (iter.hasNext) {
                    val (key, value) = iter.next()
                    nestedMappings += key -> nestedMappings.getOrElse(value, value)
                  }
                  val concreteTypeArgs = nestedType.getActualTypeArguments.flatMap {
                    case tv: TypeVariable[_] => nestedMappings.get(tv)
                    case o => Some(o)
                  }
                  val arg = if (concreteTypeArgs.length == nestedType.getActualTypeArguments.length) {
                    tryResolve(Utils.makeGenericType(nestedInfo.rawClass.get, concreteTypeArgs.toList), caller)
                  } else {
                    tryResolveTypeFrom(nestedInfo, nestedMappings, caller)
                  }
                  if (arg.isSuccess) {
                    args(i) = arg.get
                  } else {
                    success = false
                    errors += arg
                  }
                }
              case p@_ =>
                var c: Option[JavaType] = Some(p)
                while (c.isDefined && c.get.isInstanceOf[TypeVariable[_ <: GenericDeclaration]]) {
                  c = mappings.get(c.get)
                  if (c.isEmpty) {
                    success = false
                    errors += Failure(new ResolutionException(s"Unable to find mapping for $p", typeInfo.rawType))
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
        result match {
          case Some(tryResult) =>
            addChainToError(tryResult, typeInfo.paramType)
          case _ =>
            errors.headOption match {
              case Some(tryResult) =>
                addChainToError(tryResult, typeInfo.paramType)
              case _ =>
                Failure(new ResolutionException(s"Unable to find constructors for: ${typeInfo.rawType}", typeInfo.paramType))
            }
        }
      case _ =>
        Failure(new ResolutionException(s"Unable to find constructors for: ${typeInfo.rawType}", typeInfo.rawType))
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

  override def resolve(tpe: JavaType): Try[AnyRef] = {
    if (!closed) tryResolve(tpe, this)
    else {
      if (parent.isDefined) {
        parent.get.resolve(tpe)
      } else {
        Failure(new ReflectiveOperationException("Container has been closed"))
      }
    }
  }

  override def tryResolve[T: TypeTag]: Try[T] = {
    val ti = Utils.findTypeInfo(mirror.typeOf[T], mirror)
    resolve(ti.actual).map(_.asInstanceOf[T])
  }

  override def resolveClass[T](manifest: Class[T]): T = {
    if (!closed) {
      tryResolve(manifest, this) match {
        case Success(result) => result match {
          case t: T@unchecked => t
          case _ => throw new ReflectiveOperationException(s"Invalid type resolved. Expecting: ${manifest}. Resolved: ${result.getClass}")
        }
        case Failure(e) => throw e
      }
    } else {
      if (parent.isDefined) {
        parent.get.resolveClass(manifest)
      } else {
        throw new ReflectiveOperationException("Container has been closed")
      }
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
                    Failure(new ResolutionException(s"""$paramType is not an class and cannot be resolved since it's not registered in the container.
Try resolving implementation instead.""", paramType))
                  } else {
                    tryResolveClass(target, caller)
                  }
                } else if (target.isInterface) {
                  Failure(new ResolutionException(s"""$paramType is not registered in the container.
Since $paramType is an interface, it must be registered into the container.""", paramType))
                } else {
                  Failure(new ResolutionException(s"""$paramType is not registered in the container.
If you wish to resolve types not registered in the container, specify revenj.resolveUnknown=true in Properties configuration.""", paramType))
                }
              case _ =>
                Failure(new ResolutionException(s"$paramType is not an instance of Class<?> and cannot be resolved since it's not registered in the container.", paramType))
            }
          }

          def resolveArray(argumentType: JavaType) = {
            argumentType match {
              case pt: Class[_] =>
                tryResolveCollection(pt, argumentType, caller)
              case pt: ParameterizedType =>
                pt.getRawType match {
                  case rawClass: Class[_] =>
                    tryResolveCollection(rawClass, argumentType, caller)
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
            case pt: ParameterizedType if pt.getRawType == SimpleContainer.seqSignature =>
              resolveArray(pt.getActualTypeArguments.head).map(_.asInstanceOf[Array[_]].toSeq)
            case pt: ParameterizedType if pt.getRawType == SimpleContainer.immutableSeqSignature =>
              resolveArray(pt.getActualTypeArguments.head).map(_.asInstanceOf[Array[_]].toIndexedSeq)
            case pt: ParameterizedType =>
              val typeInfo = typeCache.getOrElseUpdate(pt, {
                new TypeInfo(pt)
              })
              if (typeInfo.rawClass.isEmpty) {
                Failure(new ResolutionException(s"$pt is not an instance of Class<?> and cannot be resolved", pt))
              } else {
                val ro = getRegistration(typeInfo.rawClass.get)
                if (ro.isDefined && ro.get.biFactory.isDefined && typeInfo.genericArguments.isDefined) {
                  Try(ro.get.biFactory.get(caller, typeInfo.genericArguments.get))
                } else if (ro.isDefined || resolveUnknown) {
                  if (typeInfo.constructors.isDefined && typeInfo.constructors.isEmpty && typeInfo.mappedType.isDefined) {
                    tryResolve(typeInfo.mappedType.get, caller)
                  } else {
                    val mappings = typeInfo.mappings
                    tryResolveTypeFrom(typeInfo, mappings, caller)
                  }
                } else if (typeInfo.rawClass.get.isInterface) {
                    Failure(new ResolutionException(s"""$pt and ${typeInfo.rawClass.get} are not registered in the container.
Since ${typeInfo.rawClass.get} is an interface, it must be registered into the container.""", paramType))
                } else {
                  Failure(new ResolutionException(s"""$pt and ${typeInfo.rawClass.get} are not registered in the container.
If you wish to resolve types not registered in the container, specify revenj.resolveUnknown=true in Properties configuration.""", paramType))
                }
              }
            case _ =>
              resolveClass()
          }
        }
    }
  }

  private def tryResolveCollection(container: Class[_], element: JavaType, caller: SimpleContainer): Try[AnyRef] = {
    val registrations = new mutable.LinkedHashSet[Registration[AnyRef]]
    var current: Option[SimpleContainer] = Some(caller)
    do {
      current.get.container.get(element) match {
        case Some(found) =>
          var i = 0
          while (i < found.size) {
            registrations.add(found.get(i))
            i += 1
          }
        case _ =>
      }
      current = current.get.parent
    } while (current.isDefined)
    if (registrations.isEmpty) {
      Success(java.lang.reflect.Array.newInstance(container, 0))
    } else {
      val result = java.lang.reflect.Array.newInstance(container, registrations.size).asInstanceOf[Array[AnyRef]]
      var i = 0
      val iter = registrations.iterator
      var fail = Option.empty[Throwable]
      while (fail.isEmpty && i < registrations.size) {
        val it = iter.next()
        val item = resolveRegistration(it, caller)
        if (item.isSuccess) {
          result(i) = item.get
        } else {
          val error = item.failed.get
          val message = {
            val msg = error.getMessage
            if (msg == null && error.getCause != null) error.getCause.getMessage
            else msg
          }
          val description = if (it.manifest.isDefined && it.manifest.get != it.signature) s" (${it.manifest.get})" else ""
          val underlyingError = error match {
            case re: ResolutionException if re.chain.size == 1 && re.originalError.isDefined =>
              re.originalError.get
            case _ =>
              error
          }
          fail = Some(new ReflectiveOperationException(s"Unable to resolve ${it.signature}$description. Error: $message", underlyingError))
        }
        i += 1
      }
      fail.map(Failure.apply).getOrElse(Success(result))
    }
  }

  private def prepareRegistration(registration: Registration[AnyRef], caller: SimpleContainer) = {
    if (registration.lifetime == Singleton) (registration.owner, registration)
    else if (registration.owner eq caller) (this, registration)
    else (caller, registration.prepareSingleton(caller))
  }

  private def resolveRegistration(registration: Registration[AnyRef], caller: SimpleContainer): Try[AnyRef] = {
    if (registration.instance.isDefined && ((registration.owner eq caller) || (registration.lifetime eq InstanceScope.Singleton))) {
      Success(registration.instance.get)
    } else if (registration.singleFactory.isDefined) {
      Try {
        if (registration.lifetime != Transient) {
          val (self, reg) = prepareRegistration(registration, caller)
          if (reg.instance.isDefined) {
            reg.instance.get
          } else self.synchronized {
            if (reg.instance.isDefined) {
              reg.instance.get
            } else if (reg.promoting) {
              throw new ResolutionException(s"Unable to resolve: ${registration.signature}. Circular dependencies in signature detected", registration.signature)
            } else {
              reg.promoting = true
              val instance = reg.singleFactory.get(self)
              instance match {
                case closeable: AutoCloseable =>
                  self.closeables.add(closeable)
                case _ =>
              }
              reg.promoteToSingleton(instance)
              instance
            }
          }
        } else {
          registration.singleFactory.get(this)
        }
      }
    } else {
      if (registration.lifetime != Transient) {
        val (self, reg) = prepareRegistration(registration, caller)
        if (reg.instance.isDefined) {
          Success(reg.instance.get)
        } else self.synchronized {
          if (reg.instance.isDefined) {
            Success(reg.instance.get)
          } else if (reg.promoting) {
            Failure(new ResolutionException(s"Unable to resolve: ${registration.signature}. Circular dependencies in signature detected", registration.signature))
          } else if (reg.manifest.isDefined) {
            reg.promoting = true
            val tryInstance = self.tryResolveClass(reg.manifest.get, self)
            if (tryInstance.isSuccess) {
              tryInstance.get match {
                case closeable: AutoCloseable =>
                  self.closeables.add(closeable)
                case _ =>
              }
              reg.promoteToSingleton(tryInstance.get)
            }
            tryInstance
          } else {
            Failure(new ResolutionException(s"Unable to resolve: ${registration.signature}", registration.signature))
          }
        }
      } else if (registration.manifest.isDefined) {
        tryResolveClass(registration.manifest.get, caller)
      } else {
        Failure(new ResolutionException(s"Unable to resolve: ${registration.signature}", registration.signature))
      }
    }
  }

  private def addToRegistry[T](registration: Registration[T]): this.type = {
    val registrations = container.getOrElseUpdate(registration.signature, {
      new CopyOnWriteArrayList[Registration[AnyRef]]()
    })
    typeNameMappings.put(registration.name, registration.signature)
    registrations.add(registration.asInstanceOf[Registration[AnyRef]])
    this
  }

  override def registerType[T](manifest: JavaType, implementation: Class[T], lifetime: InstanceScope = Transient): this.type = {
    val clSym = mirror.classSymbol(implementation)
    if (clSym.isModuleClass) {
      val instance = mirror.reflectModule(clSym.module.asModule).instance.asInstanceOf[T]
      addToRegistry(new Registration[T](manifest, this, instance, true))
    } else {
      addToRegistry(new Registration[T](manifest, this, implementation, lifetime))
    }
  }

  override def registerAs[T, S <: T](lifetime: InstanceScope = Transient)(implicit manifest: TypeTag[T], implementation: ClassTag[S]): this.type = {
    val ti = Utils.findTypeInfo(manifest.tpe, mirror)
    val clSym = mirror.classSymbol(implementation.runtimeClass)
    if (clSym.isModuleClass) {
      val instance = mirror.reflectModule(clSym.module.asModule).instance.asInstanceOf[T]
      addToRegistry(new Registration[T](ti.actual, this, instance, true))
      if (ti.actual != ti.erased) {
        addToRegistry(new Registration[T](ti.erased, this, instance, true))
      }
    } else {
      addToRegistry(new Registration(ti.actual, this, implementation.runtimeClass, lifetime))
      if (ti.actual != ti.erased) {
        addToRegistry(new Registration(ti.erased, this, implementation.runtimeClass, lifetime))
      }
    }
    this
  }

  override def registerInstance[T: TypeTag](service: T, handleClose: Boolean = false): this.type = {
    if (handleClose && service.isInstanceOf[AutoCloseable]) {
      closeables.add(service.asInstanceOf[AutoCloseable])
    }
    val ti = Utils.findTypeInfo(mirror.typeOf[T], mirror)
    addToRegistry(new Registration[T](ti.actual, this, service, parent.isEmpty))
    if (ti.actual != ti.erased) {
      addToRegistry(new Registration[T](ti.erased, this, service, parent.isEmpty))
    }
    this
  }

  override def registerFunc[T: TypeTag](factory: Container => T, lifetime: InstanceScope = Transient): this.type = {
    val ti = Utils.findTypeInfo(mirror.typeOf[T], mirror)
    addToRegistry(new Registration[T](ti.actual, this, factory, lifetime))
    if (ti.actual != ti.erased) {
      addToRegistry(new Registration[T](ti.erased, this, factory, lifetime))
    }
    this
  }

  override def registerFuncAs[T](manifest: JavaType, factory: Container => T, lifetime: InstanceScope = Transient): this.type = {
    addToRegistry(new Registration[T](manifest, this, factory, lifetime))
    this
  }

  override def registerInstance[T](manifest: JavaType, factory: () => T): this.type = {
    val resolution: Container => T = _ => factory.apply()
    addToRegistry(new Registration[T](manifest, this, resolution, InstanceScope.Singleton))
  }

  override def registerInstanceAs[T](manifest: JavaType, instance: T): this.type = {
    addToRegistry(new Registration[T](manifest, this, instance, parent.isEmpty))
  }

  override def registerGenerics[T: TypeTag](factory: (Container, Array[JavaType]) => T, lifetime: InstanceScope = Transient): this.type = {
    addToRegistry(new Registration[T](mirror.runtimeClass(mirror.typeOf[T]), this, factory, lifetime))
  }

  override def createScope(): Container = {
    new SimpleContainer(Some(this), resolveUnknown, mirror)
  }

  def close(): Unit = {
    closed = true
    container.clear
    var i = 0
    while (i < closeables.size()) {
      closeables.get(i).close()
      i += 1
    }
    closeables.clear()
  }
}

private object SimpleContainer {

  private val classCache = new TrieMap[Class[_], Array[CtorInfo]]
  private val typeCache = new TrieMap[JavaType, TypeInfo]
  private val typeNameMappings = new TrieMap[String, JavaType]
  private val seqSignature = classOf[scala.collection.Seq[_]]
  private val immutableSeqSignature = classOf[scala.collection.immutable.Seq[_]]

  private class ResolutionException(msg: String, location: JavaType, val originalError: Option[Throwable] = None) extends ReflectiveOperationException(msg) {
    val chain = new mutable.ArrayBuffer[JavaType]()
    chain += location

    override def getMessage: String = {
      if (chain.size > 1) {
        val sb = new java.lang.StringBuilder()
        sb.append(super.getMessage).append("\nResolution chain: ")
        chain.reverse.foreach(c => sb.append(c).append(" -> "))
        sb.setLength(sb.length() - 4)
        sb.toString
      } else super.getMessage
    }
  }

  private def addChainToError(tryResult: Try[AnyRef], manifest: JavaType): Try[AnyRef] = {
    if (tryResult.isFailure) {
      tryResult.failed.get match {
        case re: ResolutionException =>
          re.chain += manifest
          tryResult
        case ex: Throwable =>
          Failure(new ResolutionException(ex.getMessage, manifest, Some(ex)))
      }
    } else {
      tryResult
    }
  }

  private class CtorInfo(val ctor: Constructor[_]) {
    val rawTypes: Array[Class[_]] = ctor.getParameterTypes
    val genTypes: Array[JavaType] = ctor.getGenericParameterTypes
  }

  private class TypeInfo(val paramType: ParameterizedType) {
    val rawType: JavaType = paramType.getRawType
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
    val name: String = paramType.toString
    val mappedType: Option[JavaType] = typeNameMappings.get(name)
  }

  private class Registration[T](
    val parent: Option[Registration[T]],
    val signature: JavaType,
    val owner: SimpleContainer,
    val manifest: Option[Class[T]],
    var instance: Option[T],
    val singleFactory: Option[Container => T],
    val biFactory: Option[(Container, Array[JavaType]) => T],
    val lifetime: InstanceScope) {

    val name: String = signature.getTypeName

    def this(signature: JavaType, owner: SimpleContainer, manifest: Class[T], lifetime: InstanceScope) {
      this(None, signature, owner, Some(manifest), None, None, None, lifetime)
    }

    def this(signature: JavaType, owner: SimpleContainer, instance: T, singleton: Boolean) {
      this(None, signature, owner, None, Some(instance), None, None, if (singleton) Singleton else Context)
    }

    def this(signature: JavaType, owner: SimpleContainer, factory: Container => T, lifetime: InstanceScope) {
      this(None, signature, owner, None, None, Some(factory), None, lifetime)
    }

    def this(signature: JavaType, owner: SimpleContainer, factory: (Container, Array[JavaType]) => T, lifetime: InstanceScope) {
      this(None, signature, owner, None, None, None, Some(factory), lifetime)
    }

    var promoted: Boolean = instance.isDefined
    var promoting: Boolean = false

    def promoteToSingleton(value: T): Unit = {
      this.instance = Some(value)
      promoted = true
      promoting = false
    }

    def prepareSingleton(caller: SimpleContainer): Registration[T] = {
      val registration = new Registration[T](Some(this), signature, caller, manifest, None, singleFactory, biFactory, Context)
      caller.addToRegistry(registration)
      registration
    }

    override def hashCode(): Int = {
      if (parent.isEmpty) super.hashCode()
      else parent.get.hashCode()
    }

    override def equals(obj: scala.Any): Boolean = {
      obj match {
        case reg: Registration[T] => parent.getOrElse(this) eq reg.parent.getOrElse(reg)
        case _ => false
      }
    }
  }

}