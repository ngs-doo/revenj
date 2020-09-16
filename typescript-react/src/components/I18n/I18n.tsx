import * as React from 'react';

export interface II18nContext {
  localize: (text: string, defaultValue?: string) => string;
}

const defaultI18n: II18nContext = {
  localize: (text) => text,
};

const I18nContext = React.createContext<II18nContext>(defaultI18n);

export const InternationalisationProvider = I18nContext.Provider;
export const Internationalised = I18nContext.Consumer;

export interface IWithI18n {
  localize: II18nContext['localize'];
}
