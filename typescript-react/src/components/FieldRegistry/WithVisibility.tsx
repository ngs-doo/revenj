import * as React from 'react';

import { IVisibilityFunctions, FieldRegistryContext } from './FieldRegistryContext';

interface IWithVisibility {
  predicates: IVisibilityFunctions;
}

export const WithVisibility: React.FC<IWithVisibility> = React.memo(({ predicates, children }) => (
  <FieldRegistryContext.Consumer>
    {
      (ctx) => (
        <FieldRegistryContext.Provider value={{ ...ctx, Visibility: { ...ctx.Visibility, ...predicates }}}>
          {children}
        </FieldRegistryContext.Provider>
      )
    }
  </FieldRegistryContext.Consumer>
));
