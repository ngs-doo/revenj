Revenj
======
C# backend components with deep DSL Platform integration.

##Infrastructure for invasive software composition with DSL Platform C# compiler:

 * database API - LINQ to Postgres and Oracle
 * serialization API - JSON/XML/Protocol buffers
 * security API - permissions and various access controls
 * extensibilty API - plugins, container, MEF,...
 * patterns API - repository, event sourcing, service locator, OLAP,...

##Common shared infrastructure:

 * logger API + NLog
 * various utilities
	* specialized stream to avoid LOH issues
	* docx/xlsx -> PDF conversion
	* reflection helpers

##Useful features:

 * transactional mailer
 * S3 integration

##Revenj containers:

 * Wcf server
 * Http server
 * Windows service
 
##Revenj components:

 * REST API
 * generic server commands for processing messages
 * in-memory caching layer


#Getting started:

Register at https://dsl-platform.com and create an external project. You will need a Postgres instance and .NET server/Mono.

Write some DSL describing your domain. DSL Platform will build/maintain all infrastructure related problems such as:

 * migrate and prepare database for use
 * build POCO / DTO matching your DSL
 * optimized relation <-> POCO conversion
 * POCO serialization in various formats
 * repositories with LINQ access to data
 * cache invalidation
 * optimized serialization converters

Download SQL script or let DSL Platform manage your database directly. Download GeneratedModel.dll and plug it into Revenj.
Now you have a fully functional server with lot's of features. 
Consume generated library from within C# server, or use it as REST server from other languages such as:

 * Scala
 * PHP
 * Java
 * ...

Have fun.

ps. few notes:

 * http server is easiest to set up; just overwrite GeneratedModel.dll in cache folder or reference it from another location
 * for convenience, security is disabled in default configuration
 * write DSL with the help of DDD for DSL Visual Studio plugin
