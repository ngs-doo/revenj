package net.revenj

import java.util

final class TreePath private(private val value: String, private val parts: Array[String]) {

  override def hashCode: Int = value.hashCode

  override def equals(obj: Any): Boolean = obj.isInstanceOf[TreePath] && obj.asInstanceOf[TreePath].value == this.value || obj.isInstanceOf[String] && obj == this.value

  override def toString: String = value

  def plus(other: TreePath): TreePath = {
    if (other == null) this
    else if (this.value.length == 0) other
    else if (other.value.length == 0) this
    else {
      val values = util.Arrays.copyOf(this.parts, this.parts.length + other.parts.length)
      var i = 0
      while (i < other.parts.length) {
        values(this.parts.length + i) = other.parts(i)
        i += 1
      }
      new TreePath(this.value + "." + other.value, values)
    }
  }

  def isAncestor(other: TreePath): Boolean = {
    if (other == null) false
    else if (this.value == other.value) true
    else this.parts.length < other.parts.length && TreePath.compare(this.parts, other.parts, this.parts.length)
  }

  def isDescendant(other: TreePath): Boolean = {
    if (other == null) false
    else if (this.value == other.value) true
    else this.parts.length > other.parts.length && TreePath.compare(this.parts, other.parts, other.parts.length)
  }
}

object TreePath {
  val Empty: TreePath = new TreePath("", new Array[String](0))

  def create(value: String): TreePath = {
    if (value == null || value.isEmpty) Empty
    else {
      val parts = value.split("\\.")
      checkParts(parts)
      new TreePath(value, parts)
    }
  }

  private def checkParts(parts: Array[String]): Unit = {
    var i = 0
    while(i < parts.length) {
      val p = parts(i)
      i += 1
      var j = 0
      while (j < p.length) {
        if (!Character.isLetterOrDigit(p.charAt(j))) throw new IllegalArgumentException("Invalid value for part: " + p + ". Only letters and digits allowed for labels")
        j += 1
      }
    }
  }

  private def compare(left: Array[String], right: Array[String], count: Int): Boolean = {
    var i = 0
    var same = true
    while (same && i < count) {
      same = left(i) == right(i)
      i += 1
    }
    same
  }
}
