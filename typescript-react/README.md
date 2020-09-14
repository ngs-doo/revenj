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

| Name | Required | Type | Description |
|------|---------:|------|-------------|
| className | No | `string` | Class name |
| conceptOverride | No | `string` | Name of the command invoked to export, instead of the default. Can be passed in in user-level code |
| filterField | No | `string` | JSON path under which the submitted fields are. Used in cases where there is a UI form specified over some filter which resides deeper in a DSL structure |
| template | Yes | `string` | Name of the template used, specified via `templater` DSL concept |

:warning: Consider this property **unstable**. The current implementation requires more manual work than should be necessary.

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

An optional function of the type `(message: string) => string`. It will be invoked, if specified, to process strings. The implementation is entirely up the function, and it may use some special token to identify what to localize.

:warning: The API is currently not invoked everywhere in the framework, and full support will be added in the future

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

TODO

### <a id="Link"></a> `Link`

TODO

## <a id="dsl"></a> Core UI-related concepts

TODO

## Using generated code

TODO
