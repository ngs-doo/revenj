import * as React from 'react';

export interface ILinkComponent {
  className?: string;
  to: any;
}

export interface INavigationContext {
  Link: React.ComponentType<ILinkComponent>;
}

const defaultContext: INavigationContext = {
  Link: ({ to, children, className }) => <a href={to} className={className}>{children}</a>,
};

export const NavigationContext = React.createContext<INavigationContext>(defaultContext);
