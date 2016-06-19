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
