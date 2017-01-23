## Basics of JINQ provider

####ORDBMS JINQ provider

Revenj.Java supports Postgres in an OO way. To understand what that means, let's look at simple SELECT statement. Selecting all columns from a table is usually done with something like:

    SELECT t.* FROM Table t

Postgres supports alternative way to gather all columns from a table:

    SELECT t FROM Table t

which makes a basics for mapping to simple POJO objects.

####Data sources

Revenj uses a convention based naming for data types. 
Convention states that entities are read from `_entity` view, while snowflakes are read from view with the same name. 
So if we have a simple DSL:

    module Todo {
      aggregate Task {
        int description;
        date created;
        date? closed;
      }
      snowflake<Task> TaskList {
        created;
        description;
        order by created desc;
      }
    }

this means Revenj will expect a `"Todo"."Task_entity"` view and a `"Todo"."TaskList"` view for those two data sources. 
DSL Platform will therefore create table `"Todo"."Task"` and expected views. 
So when we do a query in Revenj such as:

    DataContext ctx = ...
	Query<Task> closedTasks = ctx.query(Task.class).filter(t => t.getClosed() != null);
    long totalCount = ctx.query(TaskList.class).count();

we will get a projection to specified views. 
By applying filters to Revenj `Query` type we will get a conversion to the appropriate SQL from the JINQ provider. 
`Query` is Revenj alternative to Java8 `Stream` type, which is required so expressions can be serialized and analysed.
It should contain the same signatures so it should feel like working with `Stream`.
In the above example `closedTask` projection will be converted to:

    SELECT t FROM "Todo"."Task_entity" t WHERE t.closed IS NOT NULL

The query will be executed at the time of `Query` materialization, such as calling a `.list()` on it.
The second query will be converted to something like:

    SELECT COUNT(*) FROM "Todo"."TaskList" t

since count result operator was used.

####Reducing the amount of data returned

Since Revenj always works with the whole object from the database (DB and code map 1:1) it's transparent how it will behave. If one is interested in returning a subset of data from an aggregate, specialized type must be defined:

 * by defining a specialized type, such as snowflake and picking the interesting fields from it. So for example TaskList is an example where we want to pick only two out of three (actually four - hidden ID also exists) properties from Task aggregate. This is the recommended way, since you are expanding you model with use cases (even if they are purely for optimization reasons).

####Provider extensibility

JINQ provider was built with extensibility in mind, so in case of a missing feature/conversion or a bad SQL, it's rather easy to add specialized code for dealing with such scenario. Default extensibility is defined as service registration in appropriate Java file [plugins](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/resources/META-INF/services/org.revenj.database.postgres.jinq.transform.MethodHandlerVirtual). For example String.substring method, is implemented in SubstringHandler as [handle method](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/database/postgres/jinq/transform/handlers/SubstringHandler.java#L26). It's enough to add a jar which exports supported signatures via [Service loader](https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) to import new behavior into Revenj.

####Support for complex queries

Currently only simple queries are supported, meaning, no joins, grouping or other SQL like behavior can be simulated. Since Java version is work in progress, this will be improved in time.
Still, by defining snowflake which spans several tables, joins can be provided as a data source and JINQ can do simple filtering on it.

JINQ also has issues with more complex queries; in a sense of combination of various ORs and ANDs which can produce strange and often incorrect SQL statements. Best workaround in those cases is to split single expression into multiple ones. Instead of writing:

	ctx.query(Task.class)
      .filter(t => (t.getClosed() != null || t.getDescription().startsWith("test-")) 
                && (t.getCreated().after(LocalDate.of(2015, 1, 1)) || t.getDescription().startsWith("non-test-")));

alternative query can be written:

	ctx.query(Task.class)
      .filter(t => t.getClosed() != null || t.getDescription().startsWith("test-"))
      .filter(t => t.getCreated().after(LocalDate.of(2015, 1, 1)) || t.getDescription().startsWith("non-test-"));

####Bypassing JINQ

`query` method will work through JINQ, while `search` method will call database functions. 
Sometimes it's required to call `query` method (for example custom ordering is required or cube API is used) and if JINQ is unable to convert Java to SQL properly, there is an alternative way to get correct SQL.
DSL Platform will register custom handler for `test` method of specifications. During conversion through JINQ, this custom handler will be used instead of JINQ analyzer.
In code this would look something like:

    Task.CustomSpecification specification = new Task.CustomSpecification();
    List<Task> tasks = ctx.query(Task.class).filter(specification::test).list();

The `specification::test` part of the query will cause JINQ to use alternative query builder which supports much more complex queries.