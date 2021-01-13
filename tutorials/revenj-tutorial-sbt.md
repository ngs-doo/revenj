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

    addSbtPlugin("com.dslplatform" % "sbt-dsl-platform" % "0.8.1")

Which will allow the usage of some dsl* tasks within SBT after we enable it in build.sbt which should end up looking like:

    import com.dslplatform.compiler.client.parameters.{Settings => DslSettings}
    import com.dslplatform.compiler.client.parameters.Targets.{Option => DslTarget}
    import com.dslplatform.sbt.SbtDslPlatformPlugin.autoImport.{dsl => revenj}

    name := "revenj-sbt-example"
    version := "1.0"
    scalaVersion := "2.13.4"

    enablePlugins(SbtDslPlatformPlugin)

    libraryDependencies += "net.revenj" %% "revenj-akka" % "1.1.1"

    Compile/revenj/dslNamespace := "example"
    Compile/revenj/dslSources += (DslTarget.REVENJ_SCALA -> sourceManaged.value / "main")
    Compile/revenj/dslPostgres := "127.0.0.1:5432/revenj_sbt?user=revenj&password=revenj"
    Compile/revenj/dslApplyMigration := true
    Compile/revenj/dslSettings ++= Seq(DslSettings.Option.JACKSON)
    Compile/revenj/dslResourcePath := Some((Compile/revenj/resourceDirectory).value / "META-INF" / "services")
    Compile/revenj/dslForce := true

This setup will configure DSL compiler so that:

 * base namespace for generated Scala files is `example`. For our `Person` object full name will be `example.cms.Person` as combination of both namespace, module and name
 * DSL managed sources will be created for REVENJ_SCALA target in source managed folder
 * Postgres database named `revenj_sbt` on localhost gets used and managed by SBT. Due to `dslApplyMigration` set to true it will also get migrated after a `dslMigrate` task is executed.
 * additional code is generated so [Jackson library](https://github.com/FasterXML/jackson-module-scala) can be used. Jackson is not often used in Scala projects due to problems with type system and primitives. But DSL compiler will inject additional boilerplate in the generated code so that such issues are mitigated.
 * force will simplify some operations by skipping on confirmations. Ideally it should not be used in real world projects

By default dsl settings for path to DSL will point to `dsl` folder. Unless we want to change it with `dslDslPath` we can put our cms dsl there. Our sources will be compiled along normal compilation process.
Output should look something along the lines of:

    > compile
    [info] Found 1 DSL files
    [info] Re-compiling DSL files...
    [info] Checking for latest compiler version due to download option
    [info] dsl-compiler.exe at latest version (2020-09-29)
    [info] Creating the source took 4 second(s)
    [info] Source for revenj.scala created in /home/revenj/revenj-sbt-example/target/scala-2.13/src_managed/main
    [success] Total time: 12 s, completed 13.01.2021. 19:38:53

We also need to prepare database via `dsl::dslMigrate` task. If our DB user is a superuser we can create the DB through SBT. 
Output should look something along the lines of:

    > dsl::dslMigrate
    [info] Checking for latest compiler version due to download option
    [info] dsl-compiler.exe at latest version (2020-09-29)
    [info] Creating SQL migration for Postgres ...
    [info] Running the migration took 4 second(s)
    [info] Migration saved to /home/revenj/revenj-sbt-example/sql/postgres-sql-migration-1610563274597.sql
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
    [info] Database migrated and script renamed to: applied-postgres-sql-migration-1610563274597.sql
    [success] Total time: 5 s, completed 13.01.2021. 19:41:14

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

Before we can start the app we need to setup modules which will be used. This is autowired by default via services which can be managed through `dsl::dslResource`. Resources should be added to `META-INF/services` path.

Now we can start our app via `run` task in SBT. Output should look like:

    > run
    [info] Found 1 DSL files
    [info] Running Startup 
    Starting server at http://localhost:8080 ...
    Started server at http://localhost:8080

Now we can access Revenj urls via browser, eg: http://localhost:8080/Domain.svc/search/cms.Person which should list an empty JSON list.
New person can be added through `POST` command on URL:

    http://localhost:8080/Crud.svc/cms.Person

with content such as JSON from the example above which will insert an new person in the DB.

we should get a response from server giving out surrogate ID created for the person.
Now our previous search command should return an value in the list.

Same project can be found in the [repository](/tutorials/sbt/)