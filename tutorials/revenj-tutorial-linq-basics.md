## Basics of LINQ provider

####ORDBMS LINQ provider

Revenj supports Postgres and Oracle in an OO way. To understand what that means, let's look at simple SELECT statement. Selecting all columns from a table is usually done with something like:

    SELECT t.* FROM Table t

Postgres supports alternative way to gather all columns from a table:

    SELECT t FROM Table t

This becomes handy when we have non-trivial queries:

    SELECT t1, t2 
    FROM Table1 t1 
    INNER JOIN Table2 t2 ON t1.ID = t2.ReferenceID

It basically allows us to map SQL queries to almost LINQ like expressions, instead of writing it in a way ORM tools usually write SQL queries. In Oracle to get the same behavior Revenj will work with Object tables/views and `VALUE(t)` instead of just `t`.

####Data sources

Revenj uses a convention based naming for data types, but this can be overridden with a custom name attribute. Convention states that entities are read from `_entity` view (Oracles uses uppercase naming convention), while snowflakes are read from view with same name. So if we have a simple DSL:

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

this means Revenj will expect a `"Todo"."Task_entity"` view and a `"Todo"."TaskList"` view for those two data sources. DSL Platform will therefore create table `"Todo"."Task"` and expected views. So when we do a query in Revenj such as:

    IDataContext ctx = ...
    IQueryable<Task> closedTasks = ctx.Query<Task>().Where(t => t.closed != null);
    int totalCount = ctx.Query<TaskList>().Count();

we will get a projection to specified views. By applying filters to `IQueryable` type we will get a conversion to the appropriate SQL from the LINQ provider. In the above example `closedTask` projection will be converted to:

    SELECT t FROM "Todo"."Task_entity" t WHERE t.closed IS NOT NULL

The query will be executed at the time of `IQueryable` materialization, such as calling a `.ToList()` on it.
The second query will be converted to something like:

    SELECT COUNT(*)::int FROM "Todo"."TaskList" t

since count result operator was used. To define read-only data source, SQL concept can be used, such as:

    module Legacy {
      SQL MySqlMapping 'SELECT id, name FROM LegacyTable' {
        int id;
        string name;
      }
    }

SQL `MySqlMapping` query will be validated during system startup, with a `SELECT * LIMIT 0` query. Custom attribute needs to be used on `Legacy.MySqlMapping` object for defining the actual query. 

####Reducing the amount of data returned

Since Revenj always works with the whole object from the database (DB and code map 1:1) it's transparent how it will behave. If one is interested in returning a subset of data from an aggregate, there are two ways to do it:

 * by defining a specialized type, such as snowflake and picking the interesting fields from it. So for example TaskList is an example where we want to pick only two out of three (actually four - hidden ID also exists) properties from Task aggregate. This is the recommended way, since you are expanding you model with use cases (even if they are purely for optimization reasons).

 * by forcing selection into a subquery. LINQ provider will allow non-SQL operations on the top-most SELECT since it will not try to convert the topmost projection to SQL. So to get around this when we want to reduce the amount of columns returned from a query we have to force selection into subquery. 

####Provider extensibility

LINQ provider was built with extensibility in mind, so in case of a missing feature/conversion or a bad SQL, it's rather easy to add specialized code for dealing with such scenario. Both Oracle and Postgres driver have a big chunk of .NET -> SQL conversion defined in their [plugins](https://github.com/ngs-doo/revenj/tree/master/csharp/Core/Revenj.Core/DatabasePersistence/Postgres/QueryGeneration/Plugins). For example DateTime.Date method, which converts timestamptz to date, is implemented in DateTimeMembers as [GetDate method](https://github.com/ngs-doo/revenj/blob/master/csharp/Core/Revenj.Core/DatabasePersistence/Postgres/QueryGeneration/Plugins/MemberSupport/DateTimeMembers.cs#L47). It's enough to add a DLL which exports supported signatures via [MEF](http://msdn.microsoft.com/en-us/library/dd460648%28v=vs.110%29.aspx) to import new behavior into Revenj.

####Support for complex queries

Since we are utilizing DB in an OO way, we can write some non-trivial queries which will be executed as a single query in the DB. Let's use simple master detail as an obvious example:

    module MasterDetail {
       aggregate Parent {
         string name;
       }
       aggregate Child {
         Parent *parent;
         string name;
       }
    }

If we want to load a `Parent` object which has a 0:N mapping of `Child` objects we can write LINQ such as:

    IDataContext ctx = ...
    var pairs = 
      (from p in ctx.Query<Parent>()
       let children = ctx.Query<Child>().Where(it => it.parentID == p.ID)
       select new { p, c = children.ToList() }).ToArray();

This will be executed as a single database query, without data duplication, using SQL similar to:

    SELECT 
      p, 
      array_agg(SELECT c 
                FROM "MasterDetail"."Child_entity" c 
                WHERE c."parentID" = p."ID") as c
    FROM "MasterDetail"."Parent_entity" p  

If you are defining complex aggregates (consisting from several entities), DSL Platform will prepare such a view so you don't need to combine multiple data sources in LINQ, but can just select from a single data source which has already prepared complex query in the DB (this will also improve performance, since LINQ provider will have less work to do).

####Lazy loading / expression evaluation

It's important to know that LINQ provider doesn't convert topmost projection to SQL. Thus lazy load should be avoided in topmost selection since it will be performed after SQL query and therefore will probably invoke multiple SQL queries again. 

But if lazy load is performed during filtering or in some subquery it might display "unexpected" behavior to the developer (if one is expecting lazy load to be converted into an explicit join). To better understand how LINQ driver handles lazy loading and expression in SQL let's define some DSL and look into its behavior:

    module Navigation {
      aggregate LinkedList(value) {
        int value;
        LinkedList? *next;
        calculated int square from 'it => it.value * it.value';
      }
    }

when we define DSL such as this, DB will prepare few helper functions, in this case `next(LinkedList)` and `square(LinkedList)`. They will be placed in public schema so they are available in SQL queries. We can now write SQL such as:

    SELECT ll, ll.next, ll.square FROM "Navigation"."LinkedList_entity" ll

where `ll.next` will invoke `next` function and `ll.square` will invoke `square` function defined for *LinkedList* view. Now that we understand that behavior, it's much easier to guess what will happen on lazy load or expression invocation inside LINQ query. For example:

    IDataContext ctx = ...
    var llThrees = from ll in ctx.Query<LinkedList>() where ll.square == 9 select ll;

will convert `ll.square` (which is a get property in C# code) into a function call to `square` function.
