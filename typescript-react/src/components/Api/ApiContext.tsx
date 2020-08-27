import * as React from 'react';

interface IExportButton {
  className?: string;
  conceptOverride?: string;
  filterField?: string;
  template: string;
}

export interface IApiContext {
  ExportButton: React.ComponentType<IExportButton>;
  getS3DownloadUrl: (s3: S3) => string;
  onExport: <T>(data: T, domainObjectName: string, templateName: string, customTemplate?: string) => Promise<any>;
}

export const ApiContext = React.createContext<IApiContext | undefined>(undefined);

export const ApiConfiguration = ApiContext.Provider;

export const ApiConsumer = ApiContext.Consumer;
