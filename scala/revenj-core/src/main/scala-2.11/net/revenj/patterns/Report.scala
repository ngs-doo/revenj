package net.revenj.patterns

import scala.concurrent.Future

/** Report is concept for aggregating multiple calls into a single object.
  * By providing search arguments and specifying queries with search predicates,
  * order, limit and offset using LINQ data will be populated on the server.
  *
  * DSL example:
  * <pre>
  * module Blog {
  *   aggregate Post {
  *     DateTime createdAt { versioning; }
  *     String   author;
  *     String   content;
  *   }
  *
  *   report FindPosts {
  *     String? byAuthor;
  *     Date?   from;
  *     Set&lt;Post&gt; postsFromAuthor 'it => it.author == byAuthor' ORDER BY createdAt;
  *     Array&lt;Task&gt; recentPosts 'it => it.createdAt >= from' LIMIT 20 ORDER BY createdAt DESC;
  *   }
  * }
  * </pre>
  */
trait Report[T] {
  def populate(locator: ServiceLocator): Future[T]
}
