package net.revenj.patterns

trait Equality[T] {
	def deepEquals(right: T): Boolean
}
