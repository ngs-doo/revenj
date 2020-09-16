import * as React from 'react';
import { connect } from 'react-redux';
import { getFormValues } from 'redux-form';

import { ApiContext } from '../Api/ApiContext';
import { Notifier, INotificationContext } from '../Notification/NotificationContext';
import { get, set } from '../../util/FunctionalUtils/FunctionalUtils';
import { ISubmittable, ListPresenter as ListPresenterProvider, IListPresenterContext } from '../Form/Context';

interface IListPresenterPagination<T> {
  offsetKey: DeepKeyOf<T>;
  limitKey: DeepKeyOf<T>;
  totalCountKey: DeepKeyOf<T>;
}

export interface IListPresenterPublicProps<T, R extends DeepKeyOf<T>> {
  presenterName?: string;
  domainObject: ISubmittable<T>;
  onSubmit?: (data: T, generatedHandler: ISubmittable<T>['submit']) => Promise<T>;
  onExportError?: (error: any) => void;
  requestOnMountWith?: T;
  resultField?: R;
  pagination?: IListPresenterPagination<T>;
}

interface IListPresenterStateProps<T> {
  values?: T;
}

interface IListPresenter<T, R extends DeepKeyOf<T>> extends React.PropsWithChildren<IListPresenterPublicProps<T, R>>, IListPresenterStateProps<T> {}

export interface IListPresenterState<T, R extends DeepKeyOf<T>> {
  isLoaded: boolean;
  isLoading: boolean;
  isExporting: boolean;
  formData?: T; // Filled up on regular submits
  items: DeepTypeOf<T, R>;
  page: number;
  perPage: number;
  totalCount?: number;
}

const mapStateToProps = (state: IObjectAny, ownProps: IListPresenterPublicProps<any, any>): IListPresenterStateProps<any> => ({
  values: getFormValues(ownProps.presenterName ?? ownProps.domainObject.domainObjectName)(state),
});

class ListPresenterBare<T, R extends DeepKeyOf<T>> extends React.PureComponent<IListPresenter<T, R>, IListPresenterState<T, R>> {
  public static contextType = ApiContext;

  public context: React.ContextType<typeof ApiContext>;

  public state: IListPresenterState<T, R> = {
    isExporting: false,
    isLoaded: false,
    isLoading: false,
    items: [] as DeepTypeOf<T, R>,
    page: 0,
    perPage: 20,
  };

  public componentDidMount() {
    if (this.props.requestOnMountWith != null) {
      this.onSubmit(this.props.requestOnMountWith);
    }
  }

  public render() {
    const { children } = this.props;

    return (
      <Notifier>
        {
          (context) => (
            <ListPresenterProvider value={this.getContext(context)}>
              {children}
            </ListPresenterProvider>
          )
        }
      </Notifier>
    );
  }

  private getContext = (notificationContext: INotificationContext): IListPresenterContext<T, R> => ({
    conceptName: this.props.presenterName != null ? this.props.presenterName : this.props.domainObject.domainObjectName,
    isExporting: this.state.isExporting,
    isLoaded: this.state.isLoaded,
    isLoading: this.state.isLoading,
    items: this.state.items as unknown as DeepTypeOf<T, R>,
    onChangePagination: this.onChangePagination,
    onClear: this.onClear,
    onExport: this.getOnExport(notificationContext),
    onSubmit: this.onSubmit,
    page: this.props.pagination ? this.state.page : undefined,
    perPage: this.props.pagination ? this.state.perPage : undefined,
    totalCount: this.props.pagination ? this.state.totalCount : undefined,
  })

  private onSubmit = async (data: T): Promise<T> => {
    const { domainObject, resultField, pagination, onSubmit } = this.props;
    if (this.state.isExporting) {
      return data;
    }

    this.setState({ isLoading: true, items: [] as DeepTypeOf<T, R> });
    try {
      const request = { ...data };

      if (pagination != null) {
        const offset = this.state.page * this.state.perPage;
        if (offset > 0) {
          set(request, pagination.offsetKey, offset as any); // Treat it as no offset if 0
        }
        set(request, pagination.limitKey, this.state.perPage as any);
      }

      const response = onSubmit
        ? await onSubmit(request, domainObject.submit)
        : await domainObject.submit(request);

      if (pagination != null) {
        const stateUpdate = {
          totalCount: get(response, pagination.totalCountKey),
        } as unknown as IListPresenterState<T, R>;
        this.setState(stateUpdate);
      }

      this.setState({
        formData: data, // Store form data to allow pagination to work
        isLoaded: true,
        items: get(response, resultField!)!,
      });

      return response;
    } catch (error) {
      console.error(error);
      throw error;
    } finally {
      this.setState({ isLoading: false });
    }
  }

  private onClear = () => this.setState({
    page: 0,
    perPage: 20,
  })

  private onChangePagination = (page: number, perPage: number) =>
    new Promise<T>((resolve, reject) => {
      const { formData } = this.state;
      this.setState({ page, perPage }, async () => {
        try {
          const result = await this.onSubmit(formData!);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      });
    })

  private getOnExport = (notify: INotificationContext) => async (templateName: string, customTemplate?: string, conceptOverride?: string, filterField?: string) => {
    const { domainObject, values: reduxFormValues } = this.props;
    const conceptName = conceptOverride != null ? conceptOverride : domainObject.domainObjectName;
    this.setState({ isExporting: true });
    try {
      const values = filterField ? { [filterField]: reduxFormValues } : reduxFormValues;
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

const ListPresenterConnected = connect(mapStateToProps)(ListPresenterBare);

export class ListPresenter<T, R extends DeepKeyOf<T>> extends React.PureComponent<IListPresenterPublicProps<T, R>> {
  public render() {
    return <ListPresenterConnected {...this.props as any} />;
  }
}
