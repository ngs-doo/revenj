## Seting up Revenj in Java servlet

**Revenj** in Java supports Postgres database. 
Since Revenj understands advanced Postgres features, it can be used as [document-like database](https://en.wikipedia.org/wiki/Object-relational_database), but with types.
Mono/.NET is still required to set up deployment/WAR since DSL compiler is used for creating SQL migration and a jar file.
This setup will explain minimum procedure for converting [DSL](http://c2.com/cgi/wiki?DomainSpecificLanguage) model into jar running on top of Postgres through [Java servlet](https://en.wikipedia.org/wiki/Java_servlet), in a [REST-like](http://c2.com/cgi/wiki?RestArchitecturalStyle) style.

For setup we will need:

 * Mono/.NET on path - on Linux Mono usually comes with distribution and can be installed with apt-get install mono-complete; for Mac installer can be downloaded through [official webiste](http://www.mono-project.com/download/)
 * Postgres 9.1+ - [Postgres](http://www.postgresql.org/) for persistence
 * DSL command line client - requires Java and will download most of the required dependencies. Can be installed from [Github](https://github.com/ngs-doo/dsl-compiler-client/releases)
 * Revenj servlet - set of libraries packed in a single jar for easy deployment. Available off [Maven](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22revenj-servlet%22)
 * Java servlet - for running packed WAR. [Jetty](http://download.eclipse.org/jetty/) can be used, but any other servlet should do
 * DSL model - we will write DSL model example which will be converted into jar/SQL

###DSL model

Before we can use our REST endpoint we need to write some model which will be exposed through HTTP. While Revenj can be used in purely relational style, in practice most models are object-relational combination. [Wikipedia](https://en.wikipedia.org/wiki/Document-oriented_database) page about document databases contains a person/address example which we will replicate here.

XML defining the document looks like:

    <contact>
      <firstname>Bob</firstname>
      <lastname>Smith</lastname>
      <email type="Home">bob.smith@example.com</email>
      <phone type="Cell">(123) 555-0178</phone>
      <phone type="Work">(890) 555-0133</phone>
      <address>
        <type>Home</type>
        <street1>123 Back St.</street1>
        <city>Boys</city>
        <state>AR</state>
        <zip>32225</zip>
        <country>US</country>
      </address>
    </contact>
	
which can be roughly translated into similar DSL:

    module phonebook {
      aggregate Contact {
	    string firstName;
		string lastName;
		Email email;
		string(40)? cellPhone;
		string(40)? workPhone;
		Address address;
	  }
	  enum Type {
		Home;
		Work;
	  }
	  value Email {
		Type type;
		string address;
	  }
	  value Address {
		Type type;
		string street1;
		string city;
		string(2) state;
		string(10) zip;
		string(2) country;
	  }
    }

while this is not an ideal model neither a exact replicate it shows off several features which we can utilize during modeling:

 * Enum concept can be used to enumerate types
 * Value concept can be used to group several properties under an object which will be translated to an embedded type in database. This means it will not have its own table, but rather will live under some other table as a column or a property in some other column.

###Converting DSL model to SQL/jar

Now that we have our DSL model we can convert it into database schema and Java jar using DSL command line client. This can be done through a command which looks like:

    java -jar dsl-clc.jar dsl=phonebook.dsl revenj.java=phonebook-model.jar postgres=localhost/phonebook sql=. apply force download

where we reference our phonebook.dsl file saved with DSL model, specify our Postgres instalation and target database. `apply` will also run the migration script directly on the database so it's ready for use. javac/jar will be invoked for compiling the generated Java files into jar. For detailed instructions on how to use [dsl-clc readme](https://github.com/ngs-doo/dsl-compiler-client) can be referenced.

If everything works, after we enter Postgres credentials we should see something along the lines of:

    Downloading dsl-compiler.zip from DSL Platform...
    Revenj Java server not found in: ./revenj.java
    Downloading Revenj Java server from DSL Platform...
    Downloading revenj-java.zip from DSL Platform...
    Running javac for phonebook-model.jar ...
    Running jar for phonebook-model.jar...
    Compiled Revenj.Java library to: ./phonebook-model.jar
    Creating SQL migration for Postgres ...
    Migration saved to ./postgres-sql-migration-1458228284879.sql
	Applying migration...
	New object Contact will be created in schema phonebook
	...
	Database migrated and script renamed to: applied-postgres-sql-migration-1458222318396.sql

Which means that dsl-clc:

 * downloaded DSL compiler off DSL Platform website
 * downloaded Revenj.JVM jar dependencies for model compilation
 * run javac/jar on generated Java files using previously downloaded dependencies
 * created SQL migration script - which is created from clean slate database
 * Applied SQL script to the database

We can now connect to our database and explore various object which now exists in database, such as:

 * phonebook schema
 * Contact table
 * Email and Address types

To prepare WAR we need to package several things into it:

 * revenj.properties - containing at least connection string to the database
 * WEB-INF/web.xml - reference to Revenj servlet application which will be running
 * WEB-INF/lib/phonebook-model.jar - our DSL model converted into Java objects
 * WEB-INF/lib/revenj-servlet-version.jar - supporting Revenj servlet library

`revenj.properties` file should look like:

    revenj.jdbcUrl=jdbc:postgresql://localhost/phonebook?user=postgres&password=secret

Revenj doesn't need superuser account to run, so connection string can be changed accordingly.

`web.xml` file should look like:

    <web-app>
      <listener>
        <listener-class>org.revenj.server.servlet.Application</listener-class>
       </listener>
    </web-app>
	
where we reference [Revenj servlet application](https://github.com/ngs-doo/revenj/blob/master/java/revenj-servlet/src/main/java/org/revenj/server/servlet/Application.java) during startup.

DSL model library requires also Revenj servlet library which needs to be packed alongside in lib folder. Library can be downloaded off Maven.

Now that we have all the required WAR components we can create WAR by going into that folder and running

    jar -cvf phonebook.war *

which will package content of the current folder into `phonebook.war` file. This war can be now put into Jetty `webapps` folder. If everything is ok and our servlet is running on port 8080 we can access one of the commands through:

    http://localhost:8080/phonebook/Domain.svc/search/phonebook.Contact

which should result with `[]` from server since there are not any contacts in the database yet.
New contact can be added through `POST` command on URL:

    http://localhost:8080/phonebook/Crud.svc/phonebook.Contact

with content such as `{}`

we should get a response from server giving out surrogate ID created for such contact.
Now our previous search command should return an contact in list (although all fields will be empty).
