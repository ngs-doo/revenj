import * as React from 'react';

import { IApiService, IBootConfig, initializeApplication } from '../../configuration/ApplicationConfiguration';
import { ApiConfiguration, IApiContext } from '../Api/ApiContext';
import { FieldRegistryContext, IFieldRegistryContext } from '../FieldRegistry/FieldRegistryContext';
import { II18nContext, InternationalisationProvider } from '../I18n/I18n';
import { INavigationContext, NavigationContext } from '../Navigation/NavigationContext';
import { INotificationContext, NotificationContext } from '../Notification/NotificationContext';
import { LoaderContext, ILoaderContext } from '../Loader/Loader';

export interface IDslApplication extends IApiContext, INavigationContext, INotificationContext, ILoaderContext, Omit<IFieldRegistryContext, 'visibility'> {
  api: IApiService;
  i18n: II18nContext;
  marshalling: IBootConfig;
  visibility?: IFieldRegistryContext['visibility'];
}

export interface IDslApplicationState {
  api: IApiContext;
  i18n: II18nContext;
  fields: IFieldRegistryContext;
  loading: ILoaderContext;
  navigation: INavigationContext;
  notification: INotificationContext;
}

export class DslApplication extends React.PureComponent<IDslApplication, IDslApplicationState> {
  public state: IDslApplicationState = {
    api: { ExportButton: this.props.ExportButton, onExport: this.props.onExport, getS3DownloadUrl: this.props.getS3DownloadUrl },
    i18n: { customiseLabel: this.props.i18n.customiseLabel, hasPermissions: this.props.i18n.hasPermissions, localize: this.props.i18n!.localize },
    fields: { Fields: this.props.Fields, defaults: this.props.defaults, validators: this.props.validators, visibility: this.props.visibility ?? {} },
    loading: { LoadingComponent: this.props.LoadingComponent },
    navigation: { Link: this.props.Link },
    notification: { notifyError: this.props.notifyError },
  };

  public constructor(props: IDslApplication) {
    super(props);
    initializeApplication(props.api, props.marshalling);
  }

  public componentDidUpdate(prevProps: IDslApplication) {
    if (this.props.ExportButton !== prevProps.ExportButton || this.props.onExport !== prevProps.onExport || this.props.getS3DownloadUrl !== prevProps.getS3DownloadUrl) {
      this.setState({
        api: { ExportButton: this.props.ExportButton, onExport: this.props.onExport, getS3DownloadUrl: this.props.getS3DownloadUrl },
      });
    }

    if (this.props.i18n.customiseLabel !== prevProps.i18n.customiseLabel || this.props.i18n.hasPermissions !== prevProps.i18n.hasPermissions || this.props.i18n.localize !== prevProps.i18n.localize) {
      this.setState({
        i18n: {
          customiseLabel: this.props.i18n.customiseLabel,
          hasPermissions: this.props.i18n.hasPermissions,
          localize: this.props.i18n.localize,
        },
      });
    }

    if (this.props.notifyError !== prevProps.notifyError) {
      this.setState({
        notification: { notifyError: this.props.notifyError },
      });
    }

    if (this.props.LoadingComponent !== prevProps.LoadingComponent) {
      this.setState({
        loading: { LoadingComponent: this.props.LoadingComponent },
      });
    }

    if (
      this.props.Fields !== prevProps.Fields ||
      this.props.defaults !== prevProps.defaults ||
      this.props.validators !== prevProps.validators ||
      this.props.visibility !== prevProps.visibility
    ) {
      this.setState({
        fields: { Fields: this.props.Fields, defaults: this.props.defaults, validators: this.props.validators, visibility: this.props.visibility ?? {} },
      });
    }

    if (this.props.Link !== prevProps.Link) {
      this.setState({
        navigation: { Link: this.props.Link },
      });
    }
  }

  public render() {
    const { children } = this.props;

    return (
      <ApiConfiguration value={this.state.api}>
        <NotificationContext.Provider value={this.state.notification}>
          <InternationalisationProvider value={this.state.i18n}>
            <LoaderContext.Provider value={this.state.loading}>
              <FieldRegistryContext.Provider value={this.state.fields}>
                <NavigationContext.Provider value={this.state.navigation}>
                  {children}
                </NavigationContext.Provider>
              </FieldRegistryContext.Provider>
            </LoaderContext.Provider>
          </InternationalisationProvider>
        </NotificationContext.Provider>
      </ApiConfiguration>
    );
  }
}
