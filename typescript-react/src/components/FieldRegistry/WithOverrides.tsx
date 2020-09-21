import * as React from 'react';

import { IVisibilityFunctions, IFieldRegistryContext, FieldRegistryContext } from './FieldRegistryContext';

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

export const WithControlOverrides: React.FC<Partial<IFieldRegistryContext>> = React.memo(({ Fields, validators, defaults, visibility, children }) => (
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
