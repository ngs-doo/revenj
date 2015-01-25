Revenj
======

Revenj is a *lightweight* framework for .NET/Mono with advanced LINQ support for Postgres and Oracle databases. 
It's ideal to use a REST-like service, but can also be used as a library from other frameworks such as ASP.NET. 

DSL Platform will use Invasive software composition to integrate with Revenj, so 
developers can focus on rich modeling (NoSQL/ER hybrid on top of ORDBMS).
DSL Platform will take care of various boilerplate and model evolution while Revenj will expose:

 * persistence
 * reporting
 * notifications
 * cache invalidation
 * event sourcing

and various other simple and complex features captured in the model.

##How it works

Domain is described using various modeling building blocks in a DSL, for example:

    module REST {
      aggregate Document(title) {
        string title;
        timestamp createdOn;
        Set<string(10)> tags { index; }
        Document? *parent;
        List<Part> parts;
        Map metadata;
        persistence { history; }
      }
      value Part {
        Type type;
        string content;
        Queue<string> footnotes;
      }
      enum Type {
        Documentation;
        Specification;
        Requirement;
      }
      snowflake<Document> DocumentList {
        title;
        createdOn;
        tags;
        order by createdOn desc;
      }
    }

Which gives you a DTO/POCO/POJO/POPO/POSO... across different languages, 
tables, types, views, functions specialized for supported ORDBMS, 
repositories, converters, serialization and various other boilerplate required by the supported frameworks.
There are also a lot more modeling concepts which go beyond basic persistence features and cover reporting/customization/high performance aspects of the application.

The biggest benefit shows when you start changing your model and DSL compiler gives you not only the new DLL, 
but also a SQL migration file which tries to preserve data if possible. 
SQL migration is created by the compiler analyzing differences between models, so you don't need to write manual migration scripts.

DSL compiler acts as a developer in your team which does all the boring work you would need to do, while providing high quality and high performance parts of the system.

##Getting started:

Setup a Postgres instance and a .NET/Mono.

Go through the tutorials:

 * [Setting up Visual studio and REST server](tutorials/revenj-tutorial-setup.md)
 * or [Setting up REST server on Linux](tutorials/revenj-tutorial-linux.md)
 * [Event sourcing in Revenj](tutorials/revenj-tutorial-event-sourcing.md)
 * [Using advanced Event sourcing features in Revenj](tutorials/revenj-tutorial-aggregate-events.md)
 * [NoSQL documents in relational database](tutorials/revenj-tutorial-nosql-documents.md)
 * [Notifications / SignalR integration](tutorials/revenj-tutorial-notifications.md)
 * [Security basics](tutorials/revenj-tutorial-security-basics.md)
 * [LINQ basics](tutorials/revenj-tutorial-linq-basics.md)

##Revenj features

Revenj doesn't try to be yet another framework, since it tries to reuse existing libraries whenever possible and is basically only a thin access layer to the domain captured in the DSL.

Some of the features/approaches available in the framework or precompiled library and just exposed through the framework:

 * advanced LINQ driver - no impedance mismatch between database objects and .NET classes
 * event sourcing - capture events in the system. Bulk process events. Replay events to reconstruct aggregate roots
 * notification infrastructure - integrate with SignalR for push notification or invalidate cache to keep data up-to-date
 * plugin based architecture - almost everything is a plugin, from REST commands to LINQ converters
 * signature based implementations - no need for convention or configuration, just add a new implementation or plugin and let it be picked up by the system
 * NoSQL modeling in relational database - let system automatically decompose and aggregate complex object hierarchies across several tables and data sources
 * AOP support - hot-patch your system with aspects when such scenario is most convenient
 * IOC/DI support - let container inject correct dependencies into your plugins or just provide alternative service implementation
 * permissions/security - inject security directly into DAL or handle it manually when appropriate
 * various serialization support - JSON/XML/Protobuf - others can be easily added
 * fast JSON serialization - let DSL Platform bake serialization directly into the model when JSON library is not fast enough
 * cache infrastructure - various caching features to provide excellent performance
 * WCF compatible REST API running on top of HttpListener
 * transactional mailer - queue mails while in transaction - rely on ACID offered by the databases
 * complex reporting features - provide several different data sources through a single DB call or a single REST call
 * integrate with S3 - store binary data outside of DB when required
 * and many others...

###Usage examples:

DSL model:

    module DAL {
      root ComplexObject {
        string name;
        Child[] children;
        timestamp modifiedAt { versioning; index; }
        List<VersionInfo> versions;
      }
      entity Child {
        int version;
        long? uncut;
        ip address;
        VersionInfo? info;        
      }
      value VersionInfo {
        Map dictionary;
        Stack<Date> dates;
        decimal(3) quantity;
        set<decimal(2)?>? numbers;
      }
      SQL Legacy "SELECT id, name FROM legacy_table" {
        int id;
        string name;
      }
      event CapturedAction {
        ComplexObject pointInTimeSnapshot;
        Set<int> points { index; }
        list<location>? locations;
      }
      report Aggregation {
        int[] inputs;
        int? maxActions;
        CapturedAction[] actionsMatchingInputs 'a => a.points.Overlaps(inputs)' limit maxActions;
        List<ComplexObject> last5objects 'co => true' order by modifiedAt desc limit 5;
        Legacy[] matchingLegacy 'l => inputs.Contains(l.id)';
      }
    }

results in same objects which can be consumed through IDataContext:

####LINQ query:

    IDataContext context = ...
    string matchingKey = ...
    var matchingObjects = context.Query<ComplexObject>().Where(co => co.versions.Any(v => v.dictionary.ContainsKey(matchingKey))).ToList();
    var legacyObjects = context.Query<Legacy>().Where(l => l.name.StartsWith(matchingKey)).ToArray();
    ...
    context.Update(matchingObjects);

####Lookup by identity:

ComplexObject is an aggregate root which is one of the objects identified by unique identity: URI.
URI is a string version of primary key; which mostly differs on composite primary keys.

    IDataContext context = ...
    string[] uris = ...
    var foundObjects = context.Find<ComplexObject>(uris);

####Listening for change:

LISTEN/NOTIFY from Postgres is utilized to provide on commit information about data change.

    IDataContext context = ...
    context.Track<CapturedAction>().Select(ca => ...);

####Populating report object:

Report can be used to capture various data sources at once and provide it as a single object.

    var report = new Aggregation { inputs = new [] { 1, 2, 3}, maxActions = 100 };
    var result = report.Populate(locator); //provide access to various dependencies

####No abstractions, using ADO.NET:

    IDatabaseQuery query = ...
    string rawSql = ...
    query.Execute(rawSql, params);

####Adding event handler:

Event handlers are picked up by their signatures during system initialization in appropriate aspect.
This means it's enough to write an implementation class and place DLL alongside others.

    class CapturedActionHandler : IDomainEventHandler<CapturedAction[]> {
      private readonly IDataContext context;
      public CapturedActionHandler(IDataContext context) { this.context = context; }
      public void Handle(CapturedAction[] inputs) {
        ...
      }
    }
    
####Exposing simple custom REST service:

To add a custom REST service it's enough to implement specialized typesafe signature.

    public class MyCustomService : IServerService<int[], List<ComplexObject>> {
      ...
      public MyCustomService(...) { ... }
      public List<ComplexObject> Execute(int[] arguments) { ... }      
    }

####Registering custom access permissions:

By default permissions are checked against the singleton IPermissionManager.
Custom permissions can be registered by hand if they don't really belong to the DSL.

    IPermissionManager permissions = ...
    permissions.RegisterFilter<CapturedAction>(it => false, "Admin", false); //return empty results for everybody who are not in Admin role
    
##External tools and libraries

DSL can be written in Visual studio with the help of [DDD for DSL](http://visualstudiogallery.msdn.microsoft.com/5b8a140c-5c84-40fc-a551-b255ba7676f4) plugin.

Revenj can be also used as a NoSQL database through a REST API and consumed from other languages:

 * [Java/Android](https://github.com/ngs-doo/dsl-client-java)
 * [PHP](https://github.com/ngs-doo/dsl-client-php)
 * [Scala](https://github.com/ngs-doo/dsl-client-scala)

[Command line client](https://github.com/ngs-doo/dsl-compiler-client) and Eclipse plugin can be used to automate various aspects of the process or have IDE support on the *nix environments.

