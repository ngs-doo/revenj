import * as React from 'react';

export interface ILoaderContext {
  LoadingComponent: React.ComponentType<{}>;
}

const defaultContext: ILoaderContext = {
  LoadingComponent: () => <span>{`Loading ...`}</span>,
};

export const LoaderContext = React.createContext<ILoaderContext>(defaultContext);

export const Loading: React.FC<{}> = () => (
  <LoaderContext.Consumer>
    {
      ({ LoadingComponent }) => <LoadingComponent />
    }
  </LoaderContext.Consumer>
)
