## Understanding Revenj built-in Dependency Injection

**Revenj** is a plugin-based architecture, which means an open system and therefore DI simplifies a lot of things.
Revenj can also be used as a library; in a project which already has an existing DI framework.
In both cases Revenj can be used without even knowing that it has an [internal/simple DI](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/SimpleContainer.java) built-in.

Revenj models come with a Boot setup which wires all the dependencies together.
While this could be done without the container, it's much simplified with the use of container; and allows customization after the setup. Boot mostly consists from registering various services and their signatures into the container.
Most of the times, model setup is transparent to the developers, since if `Revenj.setup` API is used, configuration is done implicitly through `ServiceLoader` instead of directly by calling `Boot.configure`.

###Vocabulary

Revenj.JVM DI is influenced by DI in .NET world and as such has features similar to [Autofac relationships](http://nblumhardt.com/2010/01/the-relationship-zoo/).
Built in relationships are:

 * Optional&lt;T&gt; - running on top of Java8, Optional stands for resolve dependency if available. Instead of throwing exception if dependency is not available in the container, an Optional.empty will be returned instead
 * Callable&lt;T&gt; - similar to Lazy&lt;T&gt; in .NET, Callable stands for resolve dependency later. Since ctors should express their dependencies explicitly, instead of taking ServiceLocator dependency, preferred way is to take a Callable dependency for something which is not always used.
 * Observable&lt;T&gt; - Revenj is integrated with [Reactive Java](https://github.com/ReactiveX/RxJava) library; and listening to notifications for some type is as simple as taking dependency on Observable for that type
 * Array[T] - collections can be resolve through array. This is useful for resolving plugins

New relationships can be registered to the container, even ones depending on generics.
Revenj only supports constructor based injections and it doesn't need the use of annotations, such as `@Inject`.
Dependencies such as Optional&lt;Observable&lt;Callable&lt;TYPE&gt;&gt;&gt; work in Revenj since Java ctor arguments are not erased, so correct dependency can be reconstructed based only on `class` information.

This allows Revenj to correctly resolve constructors such as:

    public Service(
        Optional<Connection> transaction,
        Callable<RarelyUsedService> rarelyUsedService,
        Observable<User> userChangeNotifications,
        Plugin[] plugins,
        OtherService otherService) { ... }

where scoping rules are followed; which can be translated into intentions such as:

 * resolve current transaction if within transaction
 * lazy resolve expensive service which is rarely used
 * resolve notification stream onto User type
 * resolve all registrations for Plugin type
 * resolve other service using its scoping rules (singleton/transient/...)

###Scoping and contexts

Revenj supports and encourages usage of scoping and nesting - which allows for convenient implementations of various features, such as unit of work.

Container will always start resolution from the context in which the service was requested. If there are multiple services or dependant services being resolved, this means that resolution path will always start from referenced context. 
This allows overriding registrations within the nested context, even registrations such as singletons.

An example of such usage would be a `java.sql.Connection` class, which (for example) when resolved in topmost context would return a new Connection instance. When we want to create a transcation context we will open up a nested context, start the transaction on a connection and register that connection instance in the nested context. Therefore every component resolved from within that context will get the same connection instance, instead of different one.

In practice contexts are usually bound to thread or some similar mechanism, but container allows for user defined contexts.

Basic scoping rules are supported:

 * singleton - same instance will be returned (unless a new registration is done for that specific signature)
 * transient - a new instance will be returned
 * factory - registration defined how an instance will be constructed

Along with contexts this allows various scenarios which are not really supported or encouraged in standard Java DI libraries.

###Best practices/FAQ

`Revenj.setup` will return an instance of the container. Multiple Revenj instances can be simultaneously started that way (for example to different databases).
When using Revenj as a library from other frameworks, such as Spring, most of the time it's enough to wire into the DI of such framework relevant services, such as `DataContex` and maybe `ServiceLocator`.
For Spring there is already [predefined startup API](https://github.com/ngs-doo/revenj/blob/master/java/revenj-spring/src/main/java/org/revenj/spring/RevenjStartup.java) which can be used for initialization.

Developers not used to DI will often try to create services using the new command, ie. 

    new UserRepository(locator)

instead of resolving services through the locator

    locator.resolve(UserRepository.class)

Of course, it's preferred to have such dependencies in the constructor, instead of resolving them through the locator.

While Revenj container supports reflection based, which it's very performant (it only pays 2x penalty due) for best performance it's recommended to use Java @Singleton and @Inject attributes. Revenj processor will wire up components at compile time, meaning there won't be any reflection penalty.

Revenj will only resolve from public constructors in the order of constructor definition. Therefore if constructor order is important when there are multiple public constructors. If @Inject is used on specific constructor, Revenj will construct object instance using the specified constructor.

It's preferred to register services into the container, but if that is not possible, resolve unknown option can be used. It can be enabled in `revenj.properties` file through

    revenj.resolveUnknown=true

Only services should be requested in the constructor. If there are value objects they should be arguments for the methods. In case of exception, when state instance is shared within the context, nested context can be used for such purpose (eg. `java.sql.Connection` is an example of such state which is shared within the unit of work, without polluting the API).
