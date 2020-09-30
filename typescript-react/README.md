# revenj

Revenj is an opinionated front-end framework for React + TypeScript that takes away the repetitive steps of UI development.

It is meant to be paired with [DSL Platform](https://dsl-platform.com/) and DDD, allowing the developers to focus on business logic, and empowering even non-developer domain experts to shape the UI through the ubiquitous language of DSL Platform.

[![NPM](https://img.shields.io/npm/v/revenj.svg)](https://www.npmjs.com/package/revenj)

## Install

```bash
npm install --save revenj
```

or

```bash
yarn add revenj
```

## About

Revenj for React exposes a core front-end skeleton for designing your application, and will abstract away dealing with forms and tables/grid, as well as common flows like exporting tabular data, handling intermediary states when submitting, etc.

After configuring a top-level application, Revenj will take away most of the nitty-gritty details away from you, and provide predictable and consistent behaviour.

Revenj currently does not provide many UI customization options, but will allow you to configure your own UI controls, such as form elements. Some elements, such as buttons and general layout, are still embedded in the framework.

## Usage

To use Revenj in your application, you must wrap the entire application in the `<DslApplication>` component, which provides context parameters required for the application to work.

```tsx
import React, { Component } from 'react';
import { DslApplication } from 'revenj';

import 'revenj/dist/index.css';

export class App extends Component<{}> {
  public render() {
    return (
      <DslApplication
        ExportButton={ExportButton}
        getS3DownloadUrl={generateFileURL}
        onExport={onExport}
        marshalling={marshalling}
        api={api}
        localize={localize}
        LoadingComponent={Loading}
        notifyError={notifyError}
        validators={validators}
        defaults={defaults}
        Fields={Fields}
        Link={Link}
      >
        { /* Your app here */ }
      </DslApplication>
    )
  }
}
```

| Property | Required | Type | Short Description |
|----------|---------:|------|-------------------|
| ExportButton | Yes | `React.ComponentType<IExportButton>` | Component that is rendered for exports |
| getS3DownloadUrl | Yes | `(s3: S3) => string` | Provides download links for S3-like resources |
| onExport | Yes | Export Function | Downloads a resource |
| marshalling | Yes | `Marshaller` | Configured marshalling &ndash; (de-)serialization |
| api | Yes | `IApiService` | Service that handles submitting requests to the API |
| localize | **No** | `(msg: string) => string` | Localize or internationalize labels and titles |
| LoadingComponent | Yes | `React.ComponentType<{}>` | Loading component rendered during API calls |
| notifyError | Yes | `(message: string) => void` | Error notifier |
| validators | Yes | Validators | Validators to expose to DSL |
| defaults | Yes | Defaults | Defaults to expose to DSL |
| Fields | Yes | Fields | Fields to expose to DSL, with required default keys |
| Link | Yes | `React.ComponentType<ILink>` | Link component used to navigate |

## Property details

### <a id="export-button"></a> `ExportButton`

Component to render on [presenters](#presenter) or [report presenters](#report-presenter) that have a defined Templater export. It should render a button that starts an export on click. It receives the following properties:

```ts
interface IExportButton {
  /**
   * Basic styling passed to your component, it should apply on the top level
   */
  className?: string;
  /**
   * Whether the button is disabled (e.g., exporting or data not ready)
   */
  disabled?: boolean;
  /**
   * Unique string identifier of template. When using DSL, passed in from `templater` expression on a presenter.
   */
  templateType: string;
  /**
   * Function invoked when the button is clicked. The component may pass a custom template if your application
   * supports multi-template. Otherwise, it may just be invoked without any arguments. See `onExport` in DslApplication
   * configuration for the handler that may or may not support custom templates, which you implement yourself.
   */
  onDownload: (customTemplate?: string) => Promise<void>;
}
```

### <a id="s3"></a> `getS3DownloadUrl`

Resolves an download URL (as a string) for a received `S3` concept. This can, but does not have to be, an Amazon S3 bucket. The shape of the `S3` concept is as follows:

```ts
type S3 = {
  readonly bucket: string;
  readonly key: string;
  readonly length: Long;
  readonly name?: string;
  readonly mimeType?: string;
  readonly metadata?: { [key: string]: string; };
};
```

### <a id="on-export"></a> `onExport`

Asynchronous function invoked when an export process is started. It has the following shape, for a given command shape `T`:

```ts
(data: T, domainObjectName: string, templateName: string, customTemplate?: string) => Promise<any>;
```

The parameters are as follows:

* **data** - The shape of the data (filter or similar) passed to the export API
* **domainObjectName** - A unique name of the domain object, constructed as `{module.name}.{name}` (e.g., some command `SalesReport` in module `core` would be `core.SalesReport`)
* **templateName** - Unique name of a template used for this export, passed statically in DSL via the `templater` DSL concept
* **customTemplate** - Optional, some specification of using a template override instead of the default template, needed only if your application will allow for this, and passed in via the `ExportButton` component as specified above

The function should invoke the download/export process and awaits its end.

### <a id="marshalling"></a> `marshalling`

A Marshaller configuration. This configuration is used to handle serialization and deserialization of objects during API and export request handling. You can specify it when your application has some internal form representations that need to be mapped into their raw shape. If you have no such special cases, you can pass in an empty configuration. The default behaviour will do requiredness checks, handle default values, and do processing such as serializing and deserializing between sets and lists, as the former cannot be represented in JSON.

```ts
interface IBootConfig {
    /**
   * Register processing that happens after the default marshalling handers (like defaulting values and
   * mandatory checks) conclude.
   */
    after?: (Marshaller: Marshaller) => Marshaller;
    /**
   * Register processing that happens before the default marshalling handers (like defaulting values and
   * mandatory checks) start.
   */
    before?: (marshaller: Marshaller) => Marshaller;
}
```

Example of usage:

```ts
const marshalling = {
  before: (marshaller: Marshaller) => marshaller
    // Remove empty lists when serializing
    .registerSerializerMiddleware(
      // condition
      (it: any) => Array.isArray(it) && it.length === 0,
      // transformation
      (_it: any[]) => undefined,
    ),
};
```

See the documentation on the `Marshaller` class for further instructions.

:warning: Consider this property **unstable**. The marshalling is not type-safe and might be replaced in the future.

### <a id="api"></a> `api`

API submit handler. The shape is currently simply:

```ts
interface IApiService {
  onSubmit: <T>(nameWithModule: string, domainObject: Serialized<T>) =>
    Promise<Serialized<T>>;
}
```

The `onSubmit` function receives the full name of the object and the (already *serialized*) form values, and should handle the submitting process. The return value of this function will be awaited and properly deserialized by the framework.

In general, the function should handle the following:

1. Resolving an URL from the name
2. Calling the API
3. Handling any unexpected errors in a manner your application expects

### <a id="localize"></a> `localize`

An optional function of the type `(message: string) => string`. It will be invoked, if specified, to process strings. The implementation is entirely up the function, and it may use some special token to identify what to localize. For DSL, localization will be invoked for presenter titles, group titles, grid cell column titles, and field labels, **iff** the string starts with a prefix `i18n:`. Everything after `:` will be considered to be the identifier and will be passed to your `localize` implementation.

For example, if your field label is `i18n:global.username`, your localize function will be called with `'global.username'`, and will be expected to resolve a label for it.

If this function is not defined, all labels will be left unprocessed, and will merely return the original string.


### <a id="loading-component"></a> `LoadingComponent`

A React component to render when waiting for a resource to load. For example, when loading a list of items to display in a grid, or request initial values for an edit form. It receives no props.

### <a id="notify-error"></a> `notifyError`

A function of the `(message: string) => void` that is invoked by the framework to display an error to the user. This function is only invoked on internal errors that the framework cannot recover from, like failure to fetch data from an API to display initial values.

### <a id="validators"></a> `validators`

A dictionary of validators or validator factors for some string name:

```ts
interface IValidators {
  [key: string]: Validator<any, any, any> | ((...args: any[]) => Validator<any, any, any>)
}
```

where the validator is a function `(value, allValue) => string | undefined`, the result being an error if something is invalid.

This is a somewhat hacky way to expose validator functions to DSL. Validators that are available here will be accessible in DSL by their name, quoted, as per:

```dsl
validation Typescript 'myValidatorName';
```

The function (validator creator) is used when you wish to parametrise a validator, such as:

```dsl
validation Typescript 'lte(50)';
```

See [section about DSL concepts](#dsl) for more details.

### <a id="defaults"></a> `defaults`

A dictionary of default values for name. It is meant to expose special default values (like "today" for example) that are not static. The approach is hacky, and might be replaced in the future :warning:

Definition:

```ts
defaults={{ myDefault: 'test' }}
```

usage:

```dsl
some.field 'Label' {
  default Typescript 'defaults.myDefault'; // Defaults is crucial
}
```

See [section about DSL concepts](#dsl) for more details.

### <a id="Fields"></a> `Fields`

A registry of fields by name. These fields are meant to be used in code generated by DSL Platform. All fields must satisfy an the `IExternalFormField` interface, which specifies properties such as field name, label, whether the field is required or disabled, etc.

The certain fields *must* be defined, since these are the default controls that DSL-generated code will bind to, as defined by the type of the variable:

```ts
interface IFields {
  // Minimal set of components needed to cover all the DSL types
  Checkbox: React.ComponentType<IExternalFormField<any, any, boolean>>; // Boolean default
  Currency: React.ComponentType<IExternalFormField<any, any, MoneyStr>>; // Money default
  DatePicker: React.ComponentType<IExternalFormField<any, any, DateStr>>; // Date default
  DateTimePicker: React.ComponentType<IExternalFormField<any, any, TimestampStr>>; // Timestamp default
  EnumSelect: React.ComponentType<IExternalFormField<any, any, any>>; // Enum or enum collection default
  Link: React.ComponentType<IExternalFormField<any, any, string>>; // URL default
  Number: React.ComponentType<IExternalFormField<any, any, Numeric>>; // Int/Long/Short/Float/Double default
  Select: React.ComponentType<IExternalFormField<any, any, any>>; // Default for enum
  Multiselect: React.ComponentType<IExternalFormField<any, any, any[]>>; // Default for enum collection
  S3FileInput: React.ComponentType<IExternalFormField<any, any, S3>>; // Default for S3
  ShortText: React.ComponentType<IExternalFormField<any, any, string>>;
  Text: React.ComponentType<IExternalFormField<any, any, string>>; // Default for String
  Textarea: React.ComponentType<IExternalFormField<any, any, TextStr>>; // Default for Text

  // Any additional components
  [key: string]: React.ComponentType<IExternalFormField<any, any, any>>; // Custom components
}
```


### <a id="Link"></a> `Link`

A component that is rendered as a navigation link. The specific component you will use depends on what routing system you use inside of your application. It needs to satisfy the interface of

```ts
interface ILinkComponent {
  to: string | history.Location;
}
```

## <a id="dsl"></a> Core UI-related concepts

For a full overview of DSL, see [the documentation](https://docs.dsl-platform.com/how-dsl-works). This section provides a short and partial overview of the main UI concepts and how they integrate with this library.

All UI concepts are defined over domain objects. The core concept relevant for the UI is a `command`, as per DDD, which, from our perspective, represents an API call. The examples will be defined for the following structure:

```csharp
module demo {
  role CREATE_USER;
  role MANAGE_USER;

  struct Address {
    String street;
    String city;
    String postCode;
    String state;
    String country;
    Text? notes;
  }

  enum Gender {
    Male;
    Female;
    Other;
  }

  mixin UserMixin {
    String firstName;
    String? middleName;
    String lastName;
    String username;
    Gender gender;
    Date dateOfBirth;
    Address address;
  }

  command CreateUser {
    has mixin UserMixin;
    Int ID { server managed; }
    String password;
    String repeatPassword;
  }

  command EditUser {
    has mixin UserMixin;
    String? password;
    String? repeatPassword;
  }

  struct UserVM {
    has mixin UserMixin;
    Int ID;
  }

  struct SearchUsersFilter {
    String? query;
  }

  command SearchUsers {
    SearchUsersFilter filter;
    List<UserVM> users { server managed; }
  }
}
```

At the lowest level, we have the view concepts. These views are simple and contain minimal logic. We can define **item views** to represent a single concept. A simple example for the `Address` concept (note that we are using the same module and same name as the item view):

```csharp
module demo {
  item view Address {
    // field - label, it will bind to the default control for this type (Text), and will be required
    street 'Street and Number';
    city 'City';
    postCode 'Postal/Zip Code';
    state 'State/Region';
    /*
    * Explicitly binding to a custom control. If this control is not passed to <DslApplication> under the
    * Fields lookup, it will fail in runtime
    */
    bind country 'Country' to Country;
    // will auto-bind to Textarea and will _not_ be required, as the type is optional
    notes 'Additional Notes';
  }
}
```

We can also define group views, which represent sections with a title, for a concept. This is especially useful when a part of a structure is reused in multiple views. The control binding syntax is otherwise identical.

```csharp
module demo {
  group view UserPersonalInformation for UserMixin 'Personal Information' {
    firstName 'First Name';
    middleName 'Middle Name';
    lastName 'Last Name';
    // Will render a Select with the enumerated options
    gender 'Gender';
    /* Will render the Datepicker control, with the default value and validator as provided from
    * DslApplication configuration. If the keys are not present under `defaults` or `validators`,
    * a runtime error will occur
    */
    dateOfBirth 'Date of Birth' {
      default Typescript 'defaults.today()';
      validation Typescript 'notInTheFuture';
    }
  }
}
```

We can then reference this group view inside of an item view (or another group view). Group views can also be defined inline:

```csharp
module demo {
  item view CreateUserForm for CreateUser {
    // Use an existing named group view, rendering the fields inside of this item view
    use group view UserPersonalInformation;

    // Use an item view inside of an item view, as a title-less subsection
    use item view Address on address;

    // An inline group
    group 'Credentials' {
      username 'Username';
      bind password 'Password' to Password;
      // The validator must be defined in `validators` passed to `<DslApplication>
      bind repeatPassword 'Repeat Password' to Password {
        validation Typescript 'equals(it => it?.password)';
      }
    }
  }
}
```

Finally, we have the concept of a **grid view**, which represents a grid, or table, of items. The syntax is very similar, but unlike the item or group views, the label does not represent a control label, but instead the column title.

```csharp
module demo {
  grid view UserVM {
    ID 'Unique ID';
    firstName 'First Name';
    lastName 'Last Name';
    bind username 'Username' to LinkToUser;
  }
}
```

To make use of these "dumb" views, we need to introduce the Presenter concept, which will define the actual logic of it, and connect the views into a whole. When dealing with simple tabular reports, we can define a **report presenter**. This presenter will typically have a filters form, represented by an item view, and a grid view, for the results. Typically, we will define those inline, instead of using the more verbose syntax above:

```csharp
module demo {
  // the last argument is the page heading
  report presenter SearchUsersPresenter for SearchUsers 'User Report' {
    // Inline item view, will render a filters form
    item view {
      // We can use dot-notation for deep paths
      filter.query 'Query';
    }

    grid from users {
      fast search; // Will enable FE-only full-text search

      ID 'Unique ID';
      firstName 'First Name';
      lastName 'Last Name';
      bind username 'Username' to LinkToUser;
    }

    // Will render a button with text "Export" that will handle exporting
    // some UserReport, which is a unique identifier of the report
    templater 'Export' 'UserReport';
  }
}
```

For more complex cases, such as CRUD, we will make use of the **presenter** concept. These presenters can also be inter-connected and fulfill specific roles, such as being a "create page", or an "edit page".

```csharp
module demo {
  // Shorthand for SearchUsers for SearchUsers
  presenter SearchUsers 'Users' {
    // filter from <property>
    filter from filter {
      query 'Query';
    }

    grid from users {
      fast search; // Will enable FE-only full-text search

      ID 'Unique ID';
      firstName 'First Name';
      lastName 'Last Name';
      bind username 'Username' to LinkToUser;

      // References a dashboard and edit form (we will reuse the same
      // presenter), and will also render the view/edit buttons in the grid
      view action ManageUser;
      edit action ManageUser;
    }

    // References a presenter that will be used for the Create action,
    // and will render a create button inside of this presenter
    create action CreateUser;

    // Describes the role of this page - navigation is a "list of items" role
    actions {
      navigation;
    }
  }

  presenter CreateUser 'Create New User' {
    // Reference an existing item view
    use item view CreateUserForm;

    actions {
      save changes; // save changes is a "create form", for entering new data
    }
  }

  presenter ManageUser 'Manage User' {
    // Inline item view definition
    item view {
      use group view UserPersonalInformation;

      use item view Address on address;

      group 'Credentials' {
        username 'Username';
        bind password 'Password' to Password;
        bind repeatPassword 'Repeat Password' to Password {
          validation Typescript 'equals(it => it?.password)';
        }
      }
    }

    actions {
      change data; // edit action/page
      view switching; // view action/page
    }
  }
}
```

DSL also supports additional behaviour, such automated pagination support on the Front-End, but we will eschew going into such detail in this Readme.

## Using generated code

When using DSL Platform to generate your front-end, you will need to run the [CLI](https://github.com/ngs-doo/dsl-compiler-client) to generate your TypeScript code. Assuming your DSL code is in the `dsl` folder and you want the generated code to go into `src/dsl`, you can run the following:

```bash
java -jar dsl-clc.jar dsl=./dsl react=./src/dsl download
```

It will generate the following subfolders:

| Folder | Description |
|--------|-------------|
| class  | Typescript classes for your domain objects |
| enum   | Enumerations, generated as enum + namespace with helpers |
| grid   | Grid views, as React components |
| groups | Group views, as react components |
| interface | Typescript interfaces for your domain objects |
| presenters | Presenters and report presenters, as React components |
| security | Roles defined in the system, as an enumeration |
| views | Item views, as React components |

For us, the folder of particular interest is **presenters**. Inside, each presenter defined in DSL will have been mapped into a file, named `<module>.<presenterName>.tsx`. For example, in our application we will have `demo.CreateUser.tsx` or `demo.SearchUsersPresenter.tsx`.

The reports will handle things that are system-agnostic, such as handling the submit flow, validations, export, etc. However, it will require, as React props, application-specific configuration, such as how to handle navigation to other pages, how to load initial form data (on view/edit forms), and other such behaviours.

Since most of these behaviours will likely be the same across your application, it is suggested that you implemented a container/wrapper component that will handle the repetitive boilerplate for you.

:construction: An example application will be added in the future to outline a more concrete example.
