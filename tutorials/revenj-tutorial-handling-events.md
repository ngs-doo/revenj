## Handling events in Java

.NET and JVM versions should have feature parity, so almost everything which applies to .NET can be transferred to JVM version.
This applies to [event sourcing](revenj-tutorial-event-sourcing.md) also.
Still, due to language differences, not all features are used in the same way.

In Java, Revenj has several signatures for events:

 * [identity](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/patterns/DomainEvent.java)
 * [handler](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/patterns/DomainEventHandler.java)
 * [event store](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/patterns/DomainEventStore.java)
 * [annotation](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/patterns/EventHandler.java) 

Events differentiate from aggregates in a way that they are write only, thus once saved to the database they cannot be modified (the only exception is the processedAt attribute which is used to indicate that the processing of the event has been completed).

While events are immutable once saved to the database, we usually want to take all kind of actions when events are being submitted (saved) to the system. 
While we can write our own services and implement our logic there, it's convenient to hook into event repository flow and write standalone pieces of logic which will be invoked during event persistence.
Event handlers are such injection points where we can implement simple business rules which can run:

 * before saving event(s) to the database
 * after saving event(s) to the database

### Vocabulary
 
Similarly to [dependency injection vocabulary](revenj-tutorial-dependency-injection.md) event handlers share the same underlying principle.
Therefore there are 4 distinct event handler signatures:

 * EVENT
 * EVENT[]
 * Callable&lt;EVENT&gt;
 * Callable&lt;EVENT[]&gt;

meaning that classes such as:

    public class EventHandler1 implements DomainEventHandler<EVENT> { ... }
    public class EventHandler2 implements DomainEventHandler<Callable<EVENT[]>> { ... }

will be invoked during submission of event to event store.
Handlers will run within the transaction of the submission, meaning if event is submitted within transaction scope, event handlers will share the same transaction scope.

If multiple events are submitted at once to the event store, event handler for collection will receive them all at once.
Event handlers with single event signature will be invoked for each event instance.

Events which are processed in event handlers on `Callable` signatures cannot be modified since they are called after the persistence to the database.
Handlers registered for plain POJO classes will have their modifications persisted to the database.
This can be useful in scenarios such as:

 * remove the sensitive information from the event before persistence, such as password
 * use event identity to save it in another place which is only available after the persistence

### Discovery

Unlike .NET which scans assembly in runtime for such signatures, JVM version uses `META-INF/services` for discovery of event handlers.
While developers can register their own services there, its recommended practice to use `@EventHandler` annotation for registering handlers into services.
[Revenj processor](https://github.com/ngs-doo/revenj/blob/master/java/revenj-core/src/main/java/org/revenj/processor/RevenjProcessor.java) will run during compilation and register such services.

For example, an event such as:

    module testing {
      event Tutorial {
        set<date> modifications;
        text markdown;
      }
    }

can be handled with an event handler such as:

    @org.revenj.patterns.EventHandler
	public class UpdateRssFeed implements DomainEventHandler<Callable<testing.Tutorial>> {
	  private final DataContext context;
	  public UpdateRssFeed(DataContext context) { this.context = context; }
      public void handle(Callable<testing.Tutorial> afterPersistance) {
	    //...
      }
    }

Since services are using special characters such as: &lt; and &gt; which can't be used inside `META-INF`, Revenj converts them to URI representation and therefore something along the lines of:

    org.revenj.patterns.DomainEventHandler%Ctesting.Tutorial%3E

with the content of

    package.UpdateRssFeed

is saved into `services` folder.

Unfortunately, JVM doesn't support multiple `DomainEventHandler` signatures in a single class, which in practice means that multiple classes must be created for processing different signatures.

Due to registration of event handler into the container, Revenj dependency injection will work on their constructors; therefore required services can be resolved in the constructor of the event handler.