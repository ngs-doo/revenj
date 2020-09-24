import * as React from 'react';

import { IExternalFormField } from '../Form/FormField';
import {
  FieldRegistryContext,
  IFieldRegistryContext,
  IVisibilityFunctions,
} from './FieldRegistryContext';

interface IWithVisibility {
  predicates: IVisibilityFunctions;
}

/**
 * @deprecated Prefer WithControlOverrides, which allows for fine-tuning of other fields, as well
 */
export const WithVisibility: React.FC<IWithVisibility> = React.memo(({ predicates, children }) => (
  <FieldRegistryContext.Consumer>
    {
      (ctx) => (
        <FieldRegistryContext.Provider value={{ ...ctx, visibility: { ...ctx.visibility, ...predicates }}}>
          {children}
        </FieldRegistryContext.Provider>
      )
    }
  </FieldRegistryContext.Consumer>
));

interface IWithControlOverrides extends Partial<Omit<IFieldRegistryContext, 'Fields'>> {
  Fields: {
    [key: string]: React.ComponentType<IExternalFormField<any, any, any>>;
  }
}

export const WithControlOverrides: React.FC<Partial<IWithControlOverrides>> = React.memo(({ Fields, validators, defaults, visibility, children }) => (
  <FieldRegistryContext.Consumer>
    {
      (ctx) => (
        <FieldRegistryContext.Provider
          value={{
            ...ctx,
            Fields: { ...ctx.Fields, ...Fields },
            validators: { ...ctx.validators, ...validators },
            defaults: { ...ctx.defaults, ...defaults },
            visibility: { ...ctx.visibility, ...visibility },
          }}
        >
          {children}
        </FieldRegistryContext.Provider>
      )
    }
  </FieldRegistryContext.Consumer>
));
