package net.revenj.patterns

import scala.concurrent.Future

/** Service for searching and counting domain objects.
  * Search can be performed using {@link Specification specification},
  * paged using limit and offset arguments.
  *
  * @tparam T domain object type.
  */
trait SearchableRepository[T <: DataSource] {

  /** Returns an IndexedSeq of domain objects satisfying {@link Specification specification}
    * with up to <code>limit</code> results.
    * <code>offset</code> can be used to skip initial results.
    *
    * @param specification search predicate
    * @param limit         maximum number of results
    * @param offset        number of results to be skipped
    * @return              future to domain objects which satisfy search predicate
    */
  def search(
      specification: Option[Specification[T]] = None,
      limit: Option[Int] = None,
      offset: Option[Int] = None): Future[scala.collection.IndexedSeq[T]]

  /** Returns the number of elements satisfying provided specification.
    *
    * @param specification search predicate
    * @return              how many domain objects satisfies specification
    */
  def count(specification: Option[Specification[T]] = None): Future[Long]

  /** Check if any element satisfying provided specification exists.
    *
    * @param specification search predicate
    * @return              at least one element satisfies specification
    */
  def exists(specification: Option[Specification[T]] = None): Future[Boolean]
}
