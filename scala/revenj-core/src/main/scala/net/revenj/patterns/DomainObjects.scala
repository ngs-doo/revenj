package net.revenj.patterns

/** Domain object that can be queried.
  * Server supports custom objects, such as SQL and LINQ objects
  * which are not entities, but can be queried using specifications
  * and other methods
  *
  * DSL example:
  * {{{
  * module Legacy {
  *   sql Town 'SELECT id, name FROM town' {
  *     Int    id;
  *     String name;
  *   }
  * }
  * }}}
  */
trait DataSource

/** Domain object uniquely represented by its URI.
  * Entity and snowflake are example of domain objects which are
  * identified by its identity, instead of attributes.
  * While entity does not implement Identifiable, aggregate root does.
  */
trait Identifiable extends DataSource {
  /** Domain object identity.
    * This identity can be used to lookup domain object
    *
    * @return domain object identity
    */
  def URI: String
}

/** Aggregate root is a meaningful object in the domain.
  * It can be viewed as a write boundary for entities and value objects
  * that will maintain write consistency.
  *
  * Usually it represents a single table, but can span several tables
  * and can be used like document or similar data structure.
  * Since every aggregate is also an entity, it has a unique
  * identification represented by its URI.
  *
  * DSL example:
  * {{{
  * module Todo {
  *   aggregate Task {
  *     Timestamp  startedAt;
  *     Timestamp? finishedAt;
  *     Int?       priority;
  *     Seq<Note>  notes;
  *   }
  *
  *   value Note {
  *     Date   entered;
  *     String remark;
  *   }
  * }
  * }}}
  */
trait AggregateRoot extends Identifiable

trait NestedValue[TValue] {
  def value: TValue
}
