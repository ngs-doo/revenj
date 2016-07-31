package net.revenj.patterns

/** Search predicate which can be used to filter domain objects from the remote
  * server using {@link SearchableRepository searchable repository}.
  *
  * Specification is defined in DSL with keyword {@code specification}
  * and a predicate.
  * Server can convert specification to SQL query on the fly or call
  * database function created at compile time. Other optimization techniques
  * can be used too.
  *
  * DSL example:
  * {{{
  * module Todo {
  *   aggregate Task {
  *     Timestamp createdOn;
  *     specification findBetween
  *         'it => it.createdOn >= after && it.createdOn <= before' {
  *       Date after;
  *       Date before;
  *     }
  *   }
  * }
  * }}}
  *
  * @tparam T domain object on which search will be performed.
  */
trait Specification[T <: DataSource] extends Function1[T, Boolean]
