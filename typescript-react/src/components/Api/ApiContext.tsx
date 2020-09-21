import * as React from 'react';

export interface IExportButton {
  /**
   * Basic styling passed to your component, it should apply on the top level
   */
  className?: string;
  /**
   * Whether the button is disabled (e.g., exporting or data not ready)
   */
  disabled?: boolean;
  /**
   * Unique string identifier of template. When using DSL, passed in from `templater` expression on a presenter.
   */
  templateType: string;
  /**
   * Function invoked when the button is clicked. The component may pass a custom template if your application
   * supports multi-template. Otherwise, it may just be invoked without any arguments. See `onExport` in DslApplication
   * configuration for the handler that may or may not support custom templates, which you implement yourself.
   */
  onDownload: (customTemplate?: string) => Promise<void>;
}

export interface IApiContext {
  /**
   * A button component that will be rendered on presenters that support exporting.
   * When using DSL, this equates to using the `templater` expression.
   */
  ExportButton: React.ComponentType<IExportButton>;
  /**
   * A function that resolves a download URL for an S3 resource.
   * It needs to be properly URL-encoded, and will be used inside of links.
   */
  getS3DownloadUrl: (s3: S3) => string;
  /**
   * A function that is called when an export process is started. It should run the process to its completion and start the actual download
   * in the browser.
   *
   * @argument data For exports that come from a presenter with a form, the current shape of the data (e.g., filters from a form), otherwise empty
   * @argument domainObjectName Name (with module prefix) of the domain object being exported
   */
  onExport: <T>(data: T, domainObjectName: string, templateName: string, customTemplate?: string) => Promise<void>;
}

export const ApiContext = React.createContext<IApiContext | undefined>(undefined);

export const ApiConfiguration = ApiContext.Provider;

export const ApiConsumer = ApiContext.Consumer;
