import * as React from 'react';
import { FormErrors } from 'redux-form';

import { Serialized } from '../../marshalling/Marshaller';
import { FormType } from './interfaces';
import { IExternalFormField } from './FormField';

// TODO: Move to generated code?
export interface IGeneratedConcept<T> {
  readonly domainObjectName: string;
  readonly roles: any[];
  new(...args: any[]): T;
  serialize(data: T): Serialized<T>;
  deserialize(data: Serialized<T>): T;
}

export type ISubmittable<T> = IGeneratedConcept<T> & {
  submit(data: T, options?: any): Promise<T>; // TODO: Type options
};

export interface IListPresenterContext<T, R extends DeepKeyOf<T>> {
  conceptName: string;
  items: DeepTypeOf<T, R>;
  isLoaded: boolean;
  isLoading: boolean;
  isExporting: boolean;
  page?: number;
  perPage?: number;
  totalCount?: number;
  onSubmit: (data: T) => Promise<T>;
  onClear: () => void;
  onChangePagination: (page: number, perPage: number) => Promise<T>;
  onExport: (templateName: string, customTemplate?: string, conceptOverride?: string, filterField?: string) => Promise<void>;
}

export const ListPresenterContext = React.createContext<IListPresenterContext<any, any> | undefined>(undefined);

export const ListPresenter = ListPresenterContext.Provider;

export const ListPresenterComponent = ListPresenterContext.Consumer;

export interface IUpdatePresenterContext<T> {
  formType: FormType.View | FormType.Edit;
  conceptName: string;
  isLoading: boolean;
  isLoaded: boolean;
  isExporting: boolean;
  activeItem?: T;
  reload: () => Promise<void>;
  onSubmit: (data: T) => Promise<T>;
  onRequestItem: (key: string) => Promise<T>;
  onExport: (templateName: string, customTemplate?: string, conceptOverride?: string) => Promise<void>;
}

export const UpdatePresenterContext = React.createContext<IUpdatePresenterContext<any> | undefined>(undefined);

export interface ICreatePresenterContext<T> {
  formType: FormType.Create;
  conceptName: string;
  onSubmit: (data: T) => Promise<T>;
}

export const CreatePresenterContext = React.createContext<ICreatePresenterContext<any> | undefined>(undefined);

export interface IFormContext<T> {
  form: string;
  formType?: FormType;
  sectionName?: string;
  initialValues?: Partial<T>;
  readOnly?: boolean;
  submitErrors?: FormErrors<T>;
  reset: () => void;
  change: <K extends DeepKeyOf<T>>(field: K, value: DeepTypeOf<T, K>) => void;
}

export const FormContext = React.createContext<IFormContext<any> | undefined>(undefined);

export const FormContextProvider = FormContext.Provider;

export const FormElement = FormContext.Consumer;

export const FormValueContext = React.createContext<any>(undefined);

export interface IGlobalFormsContext {
  isGroupVisible: (marker: string) => boolean;
}

export const GlobalFormsContext = React.createContext<IGlobalFormsContext>({
  isGroupVisible: () => true,
});

export type FormControlDescriptor<T, K extends keyof T> = Partial<Omit<IExternalFormField<T, K, T[K]>, 'name'>> & { visible?: boolean; };

export type IFormControlContext<T> = {
  [K in keyof T]?: T[K] extends Array<any>
    ? FormControlDescriptor<T, K>
    : T[K] extends string | number | boolean
      ? FormControlDescriptor<T, K>
      : (IFormControlContext<T[K]> | FormControlDescriptor<T, K>)
}

export const FormControlContext = React.createContext<IFormControlContext<any> | undefined>(undefined);
