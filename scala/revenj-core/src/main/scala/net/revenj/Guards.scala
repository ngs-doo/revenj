package net.revenj

import scala.collection.GenTraversableLike
import scala.collection.generic.CanBuildFrom

object Guards {

  def checkCollectionNulls[T <: AnyRef](values: Traversable[T]): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        if (iterator.next() eq null) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkArrayNulls[T <: AnyRef](values: Array[T]): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        if (values(i) eq null) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkCollectionNulls[T <: AnyRef](values: Option[Traversable[T]]): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionNulls(values.get)
  }

  def checkArrayNulls[T <: AnyRef](values: Option[Array[T]]): Unit = {
    if ((values ne null) && values.isDefined) checkArrayNulls(values.get)
  }

  def checkCollectionOptionRefNulls[T <: AnyRef](values: Traversable[Option[T]]): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        val oit = iterator.next()
        if ((oit eq null) || oit.isDefined && (oit.get eq null)) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkArrayOptionRefNulls[T <: AnyRef](values: Array[Option[T]]): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        val oit = values(i)
        if ((oit eq null) || oit.isDefined && (oit.get eq null)) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkCollectionOptionValNulls[T <: AnyVal](values: Traversable[Option[T]]): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        if (iterator.next() eq null) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkArrayOptionValNulls[T <: AnyVal](values: Array[Option[T]]): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        if (values(i) eq null) {
          throw new IllegalArgumentException("Null value found at index \"" + i + "\".")
        }
        i += 1
      }
    }
  }

  def checkCollectionOptionRefNulls[T <: AnyRef](values: Option[Traversable[Option[T]]]): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionOptionRefNulls(values.get)
  }

  def checkArrayOptionRefNulls[T <: AnyRef](values: Option[Array[Option[T]]]): Unit = {
    if ((values ne null) && values.isDefined) checkArrayOptionRefNulls(values.get)
  }

  def checkCollectionOptionValNulls[T <: AnyVal](values: Option[Traversable[Option[T]]]): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionOptionValNulls(values.get)
  }

  def checkArrayOptionValNulls[T <: AnyVal](values: Option[Array[Option[T]]]): Unit = {
    if ((values ne null) && values.isDefined) checkArrayOptionValNulls(values.get)
  }

  def checkScale(value: BigDecimal, scale: Int): Unit = {
    if (value ne null) {
      try {
        value.setScale(scale)
      } catch {
        case e: ArithmeticException =>
          throw new IllegalArgumentException("Decimal places allowed: " + scale + ". Value: " + value, e)
      }
    }
  }

  def checkScale(value: Option[BigDecimal], scale: Int): Unit = {
    if ((value ne null) && value.isDefined) checkScale(value.get, scale)
  }

  def checkCollectionScale(values: Traversable[BigDecimal], scale: Int): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        val it = iterator.next()
        if (it ne null) {
          try {
            it.setScale(scale)
          } catch {
            case e: ArithmeticException =>
              throw new IllegalArgumentException("Invalid value for element at index " + i + ". Decimal places allowed: " + scale + ". Value: " + it, e)
          }
        }
        i += 1
      }
    }
  }

  def checkArrayScale(values: Array[BigDecimal], scale: Int): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        val it = values(i)
        if (it ne null) {
          try {
            it.setScale(scale)
          } catch {
            case e: ArithmeticException =>
              throw new IllegalArgumentException("Invalid value for element at index " + i + ". Decimal places allowed: " + scale + ". Value: " + it, e)
          }
        }
        i += 1
      }
    }
  }

  def checkCollectionOptionScale(values: Traversable[Option[BigDecimal]], scale: Int): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        val oit = iterator.next()
        if ((oit ne null) && oit.isDefined) {
          val it = oit.get
          try {
            it.setScale(scale)
          } catch {
            case e: ArithmeticException =>
              throw new IllegalArgumentException("Invalid value for element at index " + i + ". Decimal places allowed: " + scale + ". Value: " + it, e)
          }
        }
        i += 1
      }
    }
  }

  def checkArrayOptionScale(values: Array[Option[BigDecimal]], scale: Int): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        val oit = values(i)
        if ((oit ne null) && oit.isDefined) {
          val it = oit.get
          try {
            it.setScale(scale)
          } catch {
            case e: ArithmeticException =>
              throw new IllegalArgumentException("Invalid value for element at index " + i + ". Decimal places allowed: " + scale + ". Value: " + it, e)
          }
        }
        i += 1
      }
    }
  }

  def checkCollectionScale(values: Option[Traversable[BigDecimal]], scale: Int): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionScale(values.get, scale)
  }

  def checkArrayScale(values: Option[Array[BigDecimal]], scale: Int): Unit = {
    if ((values ne null) && values.isDefined) checkArrayScale(values.get, scale)
  }

  def checkCollectionOptionScale(values: Option[Traversable[Option[BigDecimal]]], scale: Int): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionOptionScale(values.get, scale)
  }

  def checkArrayOptionScale(values: Option[Array[Option[BigDecimal]]], scale: Int): Unit = {
    if ((values ne null) && values.isDefined) checkArrayOptionScale(values.get, scale)
  }

  def setScale(value: BigDecimal, scale: Int): BigDecimal = {
    if (value ne null) {
      value.setScale(scale, BigDecimal.RoundingMode.HALF_UP)
    } else {
      value
    }
  }

  def setScale(value: Option[BigDecimal], scale: Int): Option[BigDecimal] = {
    if ((value ne null) && value.isDefined) {
      Some(setScale(value.get, scale))
    } else {
      value
    }
  }

  def setCollectionScale[That, Repr](values: GenTraversableLike[BigDecimal, Repr], scale: Int)(implicit bf: CanBuildFrom[Repr, BigDecimal, That]): That = {
    if (values != null) {
      val builder = bf()
      val iterator = values.toIterator
      while (iterator.hasNext) {
        builder += setScale(iterator.next(), scale)
      }
      builder.result
    } else {
      null.asInstanceOf[That]
    }
  }

  def setArrayScale(values: Array[BigDecimal], scale: Int): Array[BigDecimal] = {
    if (values ne null) {
      val buffer = new Array[BigDecimal](values.length)
      var i = 0
      while (i < values.length) {
        buffer(i) = setScale(values(i), scale)
        i += 1
      }
      buffer
    } else {
      null
    }
  }

  def setCollectionOptionScale[That, Repr](values: GenTraversableLike[Option[BigDecimal], Repr], scale: Int)(implicit bf: CanBuildFrom[Repr, Option[BigDecimal], That]): That = {
    if (values != null) {
      val builder = bf()
      val iterator = values.toIterator
      while (iterator.hasNext) {
        builder += setScale(iterator.next(), scale)
      }
      builder.result
    } else {
      values.asInstanceOf[That]
    }
  }

  def setArrayOptionScale(values: Array[Option[BigDecimal]], scale: Int): Array[Option[BigDecimal]] = {
    if (values ne null) {
      val buffer = new Array[Option[BigDecimal]](values.length)
      var i = 0
      while (i < values.length) {
        buffer(i) = setScale(values(i), scale)
        i += 1
      }
      buffer
    } else {
      null
    }
  }

  def setCollectionScale[That, Repr](values: Option[GenTraversableLike[BigDecimal, Repr]], scale: Int)(implicit bf: CanBuildFrom[Repr, BigDecimal, That]): Option[That] = {
    if ((values ne null) && values.isDefined) {
      Some(setCollectionScale(values.get, scale))
    } else {
      values.asInstanceOf[Option[That]]
    }
  }

  def setArrayScale(values: Option[Array[BigDecimal]], scale: Int): Option[Array[BigDecimal]] = {
    if ((values ne null) && values.isDefined) {
      Some(setArrayScale(values.get, scale))
    } else {
      values
    }
  }

  def setCollectionOptionScale[That, Repr](values: Option[GenTraversableLike[Option[BigDecimal], Repr]], scale: Int)(implicit bf: CanBuildFrom[Repr, Option[BigDecimal], That]): Option[That] = {
    if ((values ne null) && values.isDefined) {
      Some(setCollectionOptionScale(values.get, scale))
    } else {
      values.asInstanceOf[Option[That]]
    }
  }

  def setArrayOptionScale(values: Option[Array[Option[BigDecimal]]], scale: Int): Option[Array[Option[BigDecimal]]] = {
    if ((values ne null) && values.isDefined) {
      Some(setArrayOptionScale(values.get, scale))
    } else {
      values
    }
  }

  def checkLength(value: String, length: Int): Unit = {
    if ((value ne null) && value.length > length) {
      throw new IllegalArgumentException("Maximum length allowed: " + length + ". Value: " + value)
    }
  }

  def checkLength(value: Option[String], length: Int): Unit = {
    if ((value ne null) && value.isDefined) checkLength(value.get, length)
  }

  def checkCollectionLength(values: Traversable[String], length: Int): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        val it = iterator.next()
        if ((it ne null) && it.length > length) {
          throw new IllegalArgumentException("Invalid value for element at index " + i + ". Maximum length allowed: " + length + ". Value: " + it)
        }
        i += 1
      }
    }
  }

  def checkArrayLength(values: Array[String], length: Int): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        val it = values(i)
        if ((it ne null) && it.length > length) {
          throw new IllegalArgumentException("Invalid value for element at index " + i + ". Maximum length allowed: " + length + ". Value: " + it)
        }
        i += 1
      }
    }
  }

  def checkCollectionOptionLength(values: Traversable[Option[String]], length: Int): Unit = {
    if (values ne null) {
      var i = 0
      val iterator = values.toIterator
      while (iterator.hasNext) {
        val oit = iterator.next()
        if ((oit ne null) && oit.isDefined) {
          val it = oit.get
          if ((it ne null) && it.length > length) {
            throw new IllegalArgumentException("Invalid value for element at index " + i + ". Maximum length allowed: " + length + ". Value: " + it)
          }
        }
        i += 1
      }
    }
  }

  def checkArrayOptionLength(values: Array[Option[String]], length: Int): Unit = {
    if (values ne null) {
      var i = 0
      while (i < values.length) {
        val oit = values(i)
        if ((oit ne null) && oit.isDefined) {
          val it = oit.get
          if ((it ne null) && it.length > length) {
            throw new IllegalArgumentException("Invalid value for element at index " + i + ". Maximum length allowed: " + length + ". Value: " + it)
          }
        }
        i += 1
      }
    }
  }

  def checkCollectionLength(values: Option[Traversable[String]], length: Int): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionLength(values.get, length)
  }

  def checkArrayLength(values: Option[Array[String]], length: Int): Unit = {
    if ((values ne null) && values.isDefined) checkArrayLength(values.get, length)
  }

  def checkCollectionOptionLength(values: Option[Traversable[Option[String]]], length: Int): Unit = {
    if ((values ne null) && values.isDefined) checkCollectionOptionLength(values.get, length)
  }

  def checkArrayOptionLength(values: Option[Array[Option[String]]], length: Int): Unit = {
    if ((values ne null) && values.isDefined) checkArrayOptionLength(values.get, length)
  }
}
