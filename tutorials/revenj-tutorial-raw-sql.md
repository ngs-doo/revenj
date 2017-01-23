## Using SQL directly from Revenj

####ORM and SQL abstraction

While Revenj data layer supports wide range of use cases, it's neither a complete replacement for SQL, nor does it try to be.
Main purpose of SQL abstraction is to provide convenient and fast object/SQL conversion, most notably for converting complex object graphs into database structures and vice versa. 
Unlike most ORMs which are focused on object/relational conversion, due to focus on Object-relational databases, Revenj doesn't suffer from [object/relational impedance mismatch](http://blogs.tedneward.com/post/the-vietnam-of-computer-science/).

While Revenj data layer could behave as just another ORM, that is not its primary purpose, nor it's optimized for such modeling.
While it's perfectly fine to entirely bypass Revenj typesafe data layer and access data directly through JDBC/ADO.NET the most important thing to keep in mind is that model evolution is being compromised by writing manual SQL, since changes to the model will not be automatically verified or refactorable.

####SQL concept

DSL Platform offers SQL concept as a convenient shortcut for dropping down to SQL layer, while maintaining most of the safeties and performance benefits offered through Revenj. SQL concept can be used in two primary ways:

 * as read only data source
 * as writeable data source

An example of read only data source is:

    SQL ComplexSql <#
      SELECT a.column1, b.column2, c.column3
      FROM TableA a
      LEFT JOIN TableB b ON a.id = b.id
      CROSS JOIN TableC c
    #> {
      int column1;
      string column2;
      decimal? column3;
    }

When SQL is defined in such a way in DSL model this will allow for several benefits:

 * during startup such query will be executed (with LIMIT 0) to validate the actual SQL
 * optimized converters will be built based on the concept definition
 * such data source can be used in other parts of the system, such as reports, OLAP cubes,...

While these simplistic read only queries cover some features not supported out of the box by Revenj, they still fall short for some other custom use cases when the best solution is to fall back to raw JDBC/ADO.NET.

####Pre-existing tables

Although most benefits of DSL Platform can be extracted on [Greenfield projects](https://en.wikipedia.org/wiki/Greenfield_project) where database schema can be managed by DSL Platform, Revenj can also be used on existing database.
If SQL concept is defined with a table name and primary keys it will be considered writeable; which means it can be used as standard aggregate root within Revenj.
An example of such definition is:

    SQL LegacyTable module.table(key1, key2) {
      int key1;
      string key2;
      timestamp? at;
      decimal amount from originalColumnName;
      float ratio;
    }

Writeable SQL concept is a superset of read-only SQL data source, so besides being used for [bulk reading](revenj-tutorial-bulk-reading.md) or [basic data analysis](revenj-tutorial-olap-basics.md) in can be also used for bulk writing, or just as another ORM.

Bulk writing is invoked every time multiple objects are changed at once, for example:

    DataContext ctx = ....
    LegacyTable lt1 = new LegacyTable().setKey1(1).setKey2("a").setRatio(4);
    LegacyTable lt2 = new LegacyTable().setKey1(2).setKey2("a").setRatio(5);
    ctx.create(Arrays.asList(lt1, lt2)); //will immediately save both objects to the database in a single roundtrip