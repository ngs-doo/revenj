import * as React from 'react';

export interface INotificationContext {
  notifySuccess: (notification: string) => void;
  notifyWarning: (notification: string) => void;
  notifyError: (notification: string) => void;
}

const defaultContext: INotificationContext = {
  notifyError: console.error,
  notifyWarning: console.warn,
  notifySuccess: console.log,
};

export const NotificationContext = React.createContext<INotificationContext>(defaultContext);

export const Notifier = NotificationContext.Consumer;
