import * as React from 'react';

export interface INotificationContext {
  notifyError: (notification: string) => void;
}

const defaultContext: INotificationContext = {
  notifyError: console.error,
};

export const NotificationContext = React.createContext<INotificationContext>(defaultContext);

export const Notifier = NotificationContext.Consumer;
