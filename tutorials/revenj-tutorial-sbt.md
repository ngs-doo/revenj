## Seting up Revenj in SBT with Scala

**Revenj** in Scala supports Postgres database. 
Since Revenj understands advanced Postgres features, it can be used as [document-like database](https://en.wikipedia.org/wiki/Object-relational_database), but with types.
Mono/.NET is still required during development since DSL compiler is used for creating SQL migration and for creating Scala files.
This setup will explain how to setup a simple project with [DSL](http://c2.com/cgi/wiki?DomainSpecificLanguage) model running on top of Postgres through [Akka](http://akka.io), in a [REST-like](http://c2.com/cgi/wiki?RestArchitecturalStyle) style.

For setup we will need:

 * Mono/.NET on path - on Linux Mono usually comes with distribution and can be installed with apt-get install mono-complete; for Mac installer can be downloaded through [official webiste](http://www.mono-project.com/download/)
 * Postgres 9.1+ - [Postgres](http://www.postgresql.org/) for persistence
 * SBT - plus DSL Platform plugin for SBT
 * Revenj - we will configure our dependencies in SBT
 * DSL model - we will write DSL model example

### DSL model

Before we can use our REST endpoint we need to write some model which will be exposed through HTTP. While Revenj can be used in purely relational style, in practice most models are object-relational combination. [Wikipedia](https://en.wikipedia.org/wiki/JSON) page about JSON has a simple example which we will replicate here.

JSON document looks like:

    {
      "firstName": "John",
      "lastName": "Smith",
      "isAlive": true,
      "age": 25,
      "address": {
        "streetAddress": "21 2nd Street",
        "city": "New York",
        "state": "NY",
        "postalCode": "10021-3100"
      },
      "phoneNumbers": [
        {
          "type": "home",
          "number": "212 555-1234"
        },
        {
          "type": "office",
          "number": "646 555-4567"
        },
        {
          "type": "mobile",
          "number": "123 456-7890"
        }
      ],
      "children": [],
      "spouse": null
    }
    
which can be roughly translated into similar DSL:

    module cms {
      aggregate Person {
        String firstName;
        String lastName;
        Boolean isAlive;
        Int age;
        Address address;
        List<PhoneNumber> phoneNumbers;
        List<Person> *children;
        Person? *spouse;
      }
      value Address {
        String streetAddress;
        String city;
        String(2)? state;
        String(40) postalCode;
      }
      enum PhoneType {
        home;
        office;
        mobile;
      }
      value PhoneNumber {
        PhoneType type;
        String(20) number;
      }
    }

while this is will result in slightly different JSON (due some extra fields) it replicates the intent of the original model and it shows off several features which we can utilize during modeling:

 * Enum concept can be used to enumerate types
 * Value concept can be used to group several properties under an object which will be translated to an embedded type in database. This means it will not have its own table, but rather will live under some other table as a column or a property in some other column.
 * References to other aggregates - when referencing other aggregates a pointer (`*`) must be used.
 * Self references

### Setting up the SBT project

To setup a minimalistic SBT project we need several sbt files:

    build.sbt
    project/plugins.sbt

Plugins file is used to bring in the SBT DSL plugin so we can use it inside SBT. Plugins file should contain a line:

    addSbtPlugin("com.dslplatform" % "sbt-dsl-platform" % "0.6.0")

Which will allow the usage of some dsl* tasks within SBT after we enable it in build.sbt which should end up looking like:

    import com.dslplatform.compiler.client.parameters.{Targets, Settings => DslSettings}

    libraryDependencies += "net.revenj" %% "revenj-akka" % "0.6.1"

    name := "revenj-sbt-example"
    version := "1.0"
    scalaVersion := "2.12.2"

    enablePlugins(SbtDslPlatformPlugin)
    dslNamespace := "example"
    dslLibraries := Map(Targets.Option.REVENJ_SCALA -> unmanagedBase.value / "dsl-model.jar")
    dslPostgres := "127.0.0.1:5432/revenj_sbt?user=revenj&password=revenj"
    dslApplyMigration := true
    dslSettings ++= Seq(DslSettings.Option.JACKSON)
    dslForce := true

This setup will configure DSL compiler so that:

 * base namespace for generated Scala files is `example`. For our `Person` object full name will be `example.cms.Person` as combination of both namespace, module and name
 * extra module jar file will be created in the `lib` folder. While this is not strictly required, a loot of tools don't work out of the box unless they can pick up such a dependency. This jar will contain all our compiled DSL model
 * Postgres database named `revenj_sbt` on localhost gets used and managed by SBT. Due to `dslApplyMigration` set to true it will also get migrated after a `dslMigrate` task is executed.
 * additional code is generated so [Jackson library](https://github.com/FasterXML/jackson-module-scala) can be used. Jackson is not often used in Scala projects due to problems with type system and primitives. But DSL compiler will inject additional boilerplate in the generated code so that such issues are mitigated.
 * force will simplify some operations by skipping on confirmations. Ideally it should not be used in real world projects

By default dsl settings for path to DSL will point to `dsl` folder. Unless we want to change it with `dslDslPath` we can put our cms dsl there. To compile jar we need to issue a `dslLibrary` task to SBT.
Output should look something along the lines of:

    > dslLibrary
    [info] Checking for latest compiler version due to download option
    [info] dsl-compiler.exe at latest version (2017-05-26)
    [info] Source for revenj.scala created in /home/rikard/revenj-sbt-example/target/dsl-temp/REVENJ_SCALA
    [info] Compiling 11 Scala sources to /home/rikard/revenj-sbt-example/target/scala-2.12/dsl-platform-classes...
    [warn] there were 8 deprecation warnings
    [warn] there were four deprecation warnings (since 0.5.3)
    [warn] there were 16 deprecation warnings (since 2.12.0)
    [warn] there were 28 deprecation warnings in total; re-run with -deprecation for details
    [warn] four warnings found
    [info] Packaging /home/rikard/revenj-sbt-example/target/scala-2.12/revenj-sbt-example-dsl_2.12-1.0.jar ...
    [info] Done packaging.
    [info] Generated library for target revenj.scala in /home/rikard/revenj-sbt-example/lib/dsl-model.jar
    [success] Total time: 27 s, completed Jun 1, 2017 10:53:35 AM

We also need to prepare database via `dslMigrate` task. If our DB user is a superuser we can create the DB through SBT. 
Output should look something along the lines of:

    > dslMigrate
    [info] Checking for latest compiler version due to download option
    [info] dsl-compiler.exe at latest version (2017-05-26)
    [warn] Error connecting to the database.
    [warn] FATAL: database "revenj_sbt" does not exist
    Create a new database revenj_sbt (y/N): y
    [info] Creating SQL migration for Postgres ...
    [info] Migration saved to /home/rikard/revenj-sbt-example/sql/postgres-sql-migration-1496309467408.sql
    [info] New object Person will be created in schema cms
    [info] New property ID will be created for Person in cms
    [info] New property firstName will be created for Person in cms
    [info] New property lastName will be created for Person in cms
    [info] New property isAlive will be created for Person in cms
    [info] New property age will be created for Person in cms
    [info] New property address will be created for Person in cms
    [info] New property phoneNumbers will be created for Person in cms
    [info] New property spouseID will be created for Person in cms
    [info] New object Address will be created in schema cms
    [info] New property streetAddress will be created for Address in cms
    [info] New property city will be created for Address in cms
    [info] New property state will be created for Address in cms
    [info] New property postalCode will be created for Address in cms
    [info] New object PhoneType will be created in schema cms
    [info] New enum label home will be added to enum object PhoneType in schema cms
    [info] New enum label office will be added to enum object PhoneType in schema cms
    [info] New enum label mobile will be added to enum object PhoneType in schema cms
    [info] New object PhoneNumber will be created in schema cms
    [info] New property type will be created for PhoneNumber in cms
    [info] New property number will be created for PhoneNumber in cms
    [info] Applying migration...
    [info] Database migrated and script renamed to: applied-postgres-sql-migration-1496309467408.sql
    [success] Total time: 20 s, completed Jun 1, 2017 11:31:08 AM

Created SQL migration will contain a lot of boilerplate but it will prepare DB in such a way so it can be used in an object-relational way.

To finish setting up the SBT project we need two more things:

 * startup object - entry point into the application which will spin up the server
 * resource configuration - to configure DB connection app will be using

`dslPostgres` setting is only used from SBT. The actual DB connection should be configured somewhere else, for example in resource folder using standard `application.conf` in `src/main/resources`.
It needs to contain url to the DB:

    revenj {
      jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/revenj_sbt?user=revenj&password=revenj"
    }

Which in this case is same as the one in `dslPostgres`.

An example entry point to the application can be set up such as `Startup.scala` in `src/main/scala`:

    import net.revenj.server.WebServer

    object Startup {
      def main(args: Array[String]): Unit = {
        WebServer.start("localhost", 8080)
      }
    }

Now we can start our app via `run` task in SBT. Output should look like:

    > run
    [info] Checking for latest compiler version due to download option
    [info] dsl-compiler.exe at latest version (2017-05-26)
    [info] Source for revenj.scala created in /home/rikard/revenj-sbt-example/target/dsl-temp/REVENJ_SCALA
    [info] Packaging /home/rikard/revenj-sbt-example/target/scala-2.12/revenj-sbt-example-dsl_2.12-1.0.jar ...
    [info] Done packaging.
    [info] Running Startup 
    Starting server at http://localhost:8080 ...
    Started server at http://localhost:8080

Now we can access Revenj urls via browser, eg: http://localhost:8080/Domain.svc/search/cms.Person which should list an empty JSON list.
New person can be added through `POST` command on URL:

    http://localhost:8080/Crud.svc/cms.Person

with content such as JSON from the example above which will insert an new person in the DB.

we should get a response from server giving out surrogate ID created for the person.
Now our previous search command should return an value in the list.
