import * as React from 'react';
import { connect } from 'react-redux';
import { getFormValues } from 'redux-form';

import { FormType } from '../Form/interfaces';
import { ISubmittable, IUpdatePresenterContext, UpdatePresenterContext } from '../Form/Context';
import { ApiContext } from '../Api/ApiContext';
import { Notifier, INotificationContext } from '../Notification/NotificationContext';
import { Loading } from '../Loader/Loader';

interface IViewEditPresenterPublicProps<T> {
  isEdit: boolean;
  presenterName?: string;
  domainObject: ISubmittable<T>;
  activeKey: string;
  onSubmit?: (data: T, generatedHandler: (original: T) => Promise<T>) => Promise<T>;
  onSubmitSuccess?: (data: T) => void;
  onRequestItem: (key: string) => Promise<T>;
  onReceiveItem?: (item: T) => void;
}

interface IViewEditPresenterStateProps<T> {
  values?: T;
}

interface IViewEditPresenterContextProps {
  notify: INotificationContext;
}

interface IViewEditPresenter<T> extends React.PropsWithChildren<IViewEditPresenterPublicProps<T>>, IViewEditPresenterContextProps, IViewEditPresenterStateProps<T> {}

interface IViewEditPresenterState<T> {
  isExporting: boolean;
  isLoading: boolean;
  isLoaded: boolean;
  activeItem?: T;
}

const mapStateToProps = (state: any, ownProps: IViewEditPresenterPublicProps<any>): IViewEditPresenterStateProps<any> => ({
  values: getFormValues(ownProps.presenterName ?? ownProps.domainObject.domainObjectName)(state),
});

export class ViewEditPresenterBare<T> extends React.PureComponent<IViewEditPresenter<T>, IViewEditPresenterState<T>> {
  public static contextType = ApiContext;

  public context: React.ContextType<typeof ApiContext>;

  public state: IViewEditPresenterState<T> = {
    activeItem: undefined,
    isExporting: false,
    isLoaded: false,
    isLoading: false,
  };

  public componentDidMount() {
    this.loadItem();
  }

  public componentDidUpdate(prevProps: IViewEditPresenter<T>) {
    if (prevProps.activeKey !== this.props.activeKey) {
      this.loadItem();
    }
  }

  public render() {
    const { children } = this.props;
    const { isLoading, isLoaded } = this.state;

    return isLoaded && !isLoading ? (
      <UpdatePresenterContext.Provider value={this.getContext()}>
        {children}
      </UpdatePresenterContext.Provider>
    ) : <Loading />;
  }

  private loadItem = async () => {
    const { activeKey, notify, onReceiveItem, onRequestItem } = this.props;
    if (activeKey != null) {
      this.setState({ isLoading: true });
      try {
        const activeItem = await onRequestItem(activeKey);
        this.setState({ activeItem, isLoaded: true });
        if (onReceiveItem) {
          onReceiveItem(activeItem);
        }
      } catch (error) {
        console.error(error);
        notify.notifyError('Failed to load initial data');
      } finally {
        this.setState({ isLoading: false });
      }
    }
  }

  private getContext = (): IUpdatePresenterContext<T> => ({
    activeItem: this.state.activeItem,
    conceptName: this.props.presenterName != null ? this.props.presenterName : this.props.domainObject.domainObjectName,
    formType: this.props.isEdit ? FormType.Edit : FormType.View,
    isExporting: this.state.isExporting,
    isLoaded: this.state.isLoaded,
    isLoading: this.state.isLoading,
    onExport: this.onExport,
    onRequestItem: this.props.onRequestItem,
    onSubmit: this.onSubmit,
    reload: this.loadItem,
  })

  private onSubmit = async (data: T): Promise<T> => {
    const { onSubmit, onSubmitSuccess, domainObject } = this.props;
    if (this.state.isExporting) {
      return data;
    }

    const result = onSubmit
      ? await onSubmit(data, domainObject.submit)
      : await domainObject.submit(data);

    if (onSubmitSuccess) {
      onSubmitSuccess(result);
    }

    return result;
  }

  private onExport = async (templateName: string, customTemplate?: string, conceptOverride?: string) => {
    const { domainObject, notify, values } = this.props;
    const conceptName = conceptOverride != null ? conceptOverride : domainObject.domainObjectName;
    this.setState({ isExporting: true });
    try {
      const data = domainObject.serialize(values || {} as any);
      await this.context!.onExport(data, conceptName, templateName, customTemplate);
    } catch (error) {
      console.error(error);
      notify.notifyError('Failed to export the data!');
    } finally {
      this.setState({ isExporting: false });
    }
  }
}

const ViewEditPresenterConnected = connect(mapStateToProps)(ViewEditPresenterBare) as React.ComponentType<IViewEditPresenterPublicProps<any> & IViewEditPresenterContextProps>;

export class ViewEditPresenter<T> extends React.PureComponent<IViewEditPresenterPublicProps<T>> {
  public render() {
    return (
      <Notifier>
        {
          (notify) => (
            <ViewEditPresenterConnected {...this.props} notify={notify} />
          )
        }
      </Notifier>
    );
  }
}
