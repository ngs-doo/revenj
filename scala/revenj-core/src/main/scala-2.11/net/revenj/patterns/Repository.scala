package net.revenj.patterns

import scala.concurrent.Future

/** Service for finding Identifiable domain objects.
  * Finding domain objects using their URI identity is the fastest way
  * retrieve an object from the repository.
  *
  * @tparam T IIdentifiable domain object type
  */
trait Repository[T <: Identifiable] {

  /** Returns a Seq of domain objects uniquely represented with their URIs.
    * Only found objects will be returned (Seq will be empty if no objects are found).
    *
    * @param uris sequence of unique identifiers
    * @return future to found domain objects
    */
  def find(uris: Seq[String]): Future[IndexedSeq[T]]

  /** Returns a domain object uniquely represented with its URI.
    * If object is not found, an exception will be thrown
    *
    * @param uri domain object identity
    * @return future to found domain object
    */
  def find(uri: String): Future[Option[T]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    require(uri ne null, "null value provided for URI")
    find(Seq(uri)).map(_.headOption)
  }
}
