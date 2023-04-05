import * as React from 'react';

import { IExternalFormField } from '../Form/FormField';
import { Validator } from '../validation';

export interface IFields {
  // Minimal set of components needed to cover all the DSL types
  Checkbox: React.ComponentType<IExternalFormField<any, any, boolean>>;
  Currency: React.ComponentType<IExternalFormField<any, any, MoneyStr>>;
  DatePicker: React.ComponentType<IExternalFormField<any, any, DateStr>>;
  DateTimePicker: React.ComponentType<IExternalFormField<any, any, TimestampStr>>;
  Decimal: React.ComponentType<IExternalFormField<any, any, DecimalStr>>;
  Double: React.ComponentType<IExternalFormField<any, any, Double>>;
  EnumSelect: React.ComponentType<IExternalFormField<any, any, any>>;
  Float: React.ComponentType<IExternalFormField<any, any, Double>>;
  Integer: React.ComponentType<IExternalFormField<any, any, Int>>;
  Link: React.ComponentType<IExternalFormField<any, any, string>>;
  Long: React.ComponentType<IExternalFormField<any, any, Long>>;
  Select: React.ComponentType<IExternalFormField<any, any, any>>;
  Multiselect: React.ComponentType<IExternalFormField<any, any, any[]>>;
  S3FileInput: React.ComponentType<IExternalFormField<any, any, S3>>;
  Text: React.ComponentType<IExternalFormField<any, any, string>>;
  Short: React.ComponentType<IExternalFormField<any, any, Short>>;
  LongText: React.ComponentType<IExternalFormField<any, any, TextStr>>;
  StringMap: React.ComponentType<IExternalFormField<any, any, StringMap>>;

  // Any additional components
  [key: string]: React.ComponentType<IExternalFormField<any, any, any>>;
}

export interface IVisibilityFunctions {
  [key: string]: (...args: any[]) => boolean;
}

// NOTE: This is not typesafe, unfortunately. It would be nice to figure out a way to constrain these in some way, otherwise
// it will be possible for the generated code to produce some weird results. This is not a problem per se, since we can make
// sure the generated code generally makes sense, but user-defined defaults and validators might be wonky
export interface IFieldRegistryContext {
  Fields: IFields;
  validators: { [key: string]: Validator<any, any, any> | ((...args: any[]) => Validator<any, any, any>) };
  defaults: { [key: string]: any };
  visibility: IVisibilityFunctions;
}

const ImplementationMissing: React.FC<IExternalFormField<any, any, any>> = () => {
  throw new Error('Implementation missing!');
};

const defaultContext: IFieldRegistryContext = {
  Fields: {
    Checkbox: ImplementationMissing,
    Currency: ImplementationMissing,
    DatePicker: ImplementationMissing,
    DateTimePicker: ImplementationMissing,
    Decimal: ImplementationMissing,
    Double: ImplementationMissing,
    EnumSelect: ImplementationMissing,
    Float: ImplementationMissing,
    Integer: ImplementationMissing,
    Link: ImplementationMissing,
    Long: ImplementationMissing,
    LongText: ImplementationMissing,
    Select: ImplementationMissing,
    Multiselect: ImplementationMissing,
    S3FileInput: ImplementationMissing,
    Short: ImplementationMissing,
    ShortText: ImplementationMissing,
    StringMap: ImplementationMissing,
    Text: ImplementationMissing,
  },
  defaults: {},
  validators: {},
  visibility: {},
};

export const FieldRegistryContext = React.createContext<IFieldRegistryContext>(defaultContext);
