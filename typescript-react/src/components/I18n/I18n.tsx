import * as React from 'react';

export interface II18nContext {
  customizeLabel: (path: string, value: string) => Promise<any>;
  hasPermissions: boolean;
  localize: (path: string) => string | undefined;
}

const defaultI18n: II18nContext = {
  customizeLabel: (_path, _value) => Promise.resolve({}),
  hasPermissions: false,
  localize: (_path) => undefined,
};

export const I18nContext = React.createContext<II18nContext>(defaultI18n);

export const InternationalisationProvider = I18nContext.Provider;
export const Internationalised = I18nContext.Consumer;

export interface IWithI18n {
  localize: II18nContext['localize'];
}
