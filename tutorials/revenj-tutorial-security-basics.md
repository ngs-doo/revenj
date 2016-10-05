## Setting up basic security

####Enabling security layers for REST projects (WCF and HTTP)

For convenience security is disabled by default. This is done by overriding default authentication provider and permission manager. In http-server project this is done with two lines in App.config/Web.config

    <add key="CustomAuth" value="Revenj.Http.NoAuth"/>

which redirects default http authentication from `Revenj.Http.HttpAuth` to `NoAuth` which authenticates everyone using guest principal. The second one is

    <components>
      <component type="Revenj.Http.NoAuth, Revenj.Http" service="Revenj.Security.IPermissionManager, Revenj.Security.Interface" />
    </components>

which redirects various permission checks from default `Revenj.Security.PermissionManager` to the same `NoAuth` class.

After security is enabled, HTTP project will support basic security (username:password base64 encoded) and slightly modified basic security where sha1 hash of the password is sent (instead of actual password).

They are supported with `Authorization` http header with `Basic` and `Hash` prefixes.

####Security is enabled by default for Revenj.Core

If Revenj is used as a library from some other framework such as ASP.NET, then security is not disabled by default. Since other framework will be used for authentication, http-server or WCF authentication provider is not even referenced. There is no need to override authorization provider (permission manager) since it will be used only for filtering access to repositories (and not checking if server command is allowed to be executed).

####Authorization service

[Permission manager](https://github.com/ngs-doo/revenj/blob/master/csharp/Core/Revenj.Core.Interface/Security/IPermissionManager.cs) is a singleton service which should be used to configure access to data available through repositories. It can be configured from DSL or by code. Depending on the DSL configuration, security checks will be injected directly into repository and executed as a part of SQL query. This means permission manager can be used to implement features such as multi-tenancy on a single database. To globally disable application of security checks during DB queries, default needs to be changed in DSL as:

    defaults { external permissions disabled; }

Some permission check can still be injected into repository when defined through DSL, by explicitly stating they need to be inserted into repository:

    module Todo
    {
        aggregate Task
        {
            Security.User *User; //has an implicit UserID field
            string Description;
        }
        permissions
        {
            filter Task	'it => it.UserID == Thread.CurrentPrincipal.Identity.Name' except Administrator { 
                repository; //force query to be injected into SQL layer 
            }
        }
    }

Above example is setting up authorization to Task aggregate so that only users have access to their own task (if Thread.CurrentPrincipal is populated with the authenticated user). This rule will be applied for all users which are not in Administrator role (due to `except` keyword). *Roles are read from `System.Security.Principal.IPrincipal` object.*

If we try to consume above repository (and we are not in Administrator role) for example with a query such as:

    IDataContext ctx = ...
    var myLatestTasks = ctx.Query<Task>().Take(10).ToArray();

SQL query will be executed which would look something like:

    SELECT t FROM "Todo"."Task_entity" WHERE t."UserID" == 'iUser' LIMIT 10

if we are authenticated with a username `iUser`.  

If multiple filters are applied, they will be executed as && operations when they satisfy the role condition.

Currently `IPermissionManager` lacks few methods to be able to replicate all authorization which can be defined in DSL, such as create/update/delete operations on an aggregate. *This is expected to be added soon.*

####Default security configuration

If Revenj is used as a REST framework `CanAccess` method will be used to check access to various objects and services. By configuring default permission manager configurations, complex scenarios can be supported out of the box. Default permission manager implementation is based on two tables for setting up security:

 * `IGlobalPermission` - for setting up global permissions
 * `IRolePermission` - for setting up role based permissions

Other than those, access can be changed from open by default to closed by default using `Permissions.OpenByDefault` app config value (true is default value = resulting in open access by default). Config should look something like:

    <configuration>
      <appSettings>
        <add key="Permissions.OpenByDefault" value="false" />
      </appSettings>
    </configuration>

[Default security DSL](https://github.com/ngs-doo/revenj/blob/master/csharp/Core/Revenj.Core/Security/DSL/Security.dsl) can be used to set up simple tables for defining security configuration. Identifiers follow simple dot pattern for defining security access, meaning to disable globally access to *Todo* module we can insert { Name = 'Todo', IsAllowed = false } into *GlobalPermission* table.
To grant access *User* role to *Todo.Task* aggregate we can override global configuration with an insert to { Name = 'Todo.Task', IsAllowed = true, RoleId = 'User' } *RolePermission* table.

Access to server commands can be controlled in the same way, just `Name` will be type name of that class (for example Name = 'Revenj.Plugins.Server.Commands.Create' represents a Crud create command found in [plugin project](https://github.com/ngs-doo/revenj/blob/master/csharp/Plugins/Revenj.Plugins.Server.Commands/CRUD/Create.cs))

*Since security configuration is loaded into memory and leverages notifications to invalidate itself, changing rows in the database directly will not have an effect on runtime configuration.* 

*Due to lazy notification signature: `IObservable<Lazy<IGlobalPermission>>` permission manager will work in Revenj.Core even when there is nothing registered for `IGlobalPermission`.*