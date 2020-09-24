import * as React from 'react';


import { FormType, IActionButton, Presenter } from 'revenj';
import { IEditPackage as demoIEditPackage } from '../interface/demo.EditPackage';
import { EditPackage as demoEditPackage } from '../class/demo.EditPackage';
import { EditPackage as EditPackageItemView } from '../views/demo.EditPackageEditPackage';
import { IFormControlContext, PresenterForm, IPresenterFormPublicProps } from 'revenj';
import { ViewEditPresenter } from 'revenj';

import Role from '../security/Role';
import { demoEditPackagePresenterName } from './demo.PresenterName';
import { demoEditPackageDomainObjectName } from './demo.DomainObjectName';

export interface IEditPackagePresenter {
  title?: string;
  userRoles: Set<Role>;
  actions?: IActionButton[];
  canNavigateTo: (formType: FormType, to?: any) => boolean;
  navigateTo: (formType: FormType, id?: any) => void;
  onSubmit?: (data: demoIEditPackage, generatedHandler: (original: demoIEditPackage) => Promise<demoIEditPackage>) => Promise<demoIEditPackage>;
  onForbidden: () => void;
  configuration?: IFormControlContext<demoIEditPackage>;
  onCancel?: () => void;
  initialValues?: Partial<demoIEditPackage>;
  formProps?: Partial<IPresenterFormPublicProps<demoIEditPackage>>;
  isEdit: boolean;
  onRequestItem: (key: string) => Promise<DomainObject>;
  onReceiveItem?: (item: DomainObject) => void;
  activeKey: string;
  onSubmitSuccess?: (data: DomainObject) => void;
}

export interface IEditPackagePresenterState {
  actions: IActionButton[];
}


export const roles = demoEditPackage.roles;
export type DomainObject = demoIEditPackage;

export class EditPackagePresenter extends React.PureComponent<IEditPackagePresenter, IEditPackagePresenterState> {
  public static presenterName: string = demoEditPackagePresenterName;
  public static domainObjectName: string = demoEditPackageDomainObjectName;
  public static roles = demoEditPackage.roles;
  public static title = 'Manage Package';

  public state: IEditPackagePresenterState = {
    actions: [],
  };

  public componentDidMount() {
    this.setState({ actions: this.getActions() });
  }

  public componentDidUpdate(prevProps: IEditPackagePresenter) {
    if (prevProps.actions !== this.props.actions) {
      this.setState({ actions: this.getActions() });
    }
  }

  public render() {
    return (
      <ViewEditPresenter
        domainObject={demoEditPackage}
        presenterName={EditPackagePresenter.presenterName}
        onSubmit={this.props.onSubmit}
          isEdit={this.props.isEdit}
          activeKey={this.props.activeKey!}
          onRequestItem={this.props.onRequestItem}
          onReceiveItem={this.props.onReceiveItem}
          onSubmitSuccess={this.props.onSubmitSuccess}
      >
	    <Presenter
          title={this.props.title != null ? this.props.title : 'Manage Package'}
          presenterName={EditPackagePresenter.presenterName}
          domainObject={demoEditPackage}
          userRoles={this.props.userRoles}
          onForbidden={this.props.onForbidden}
          actions={this.state.actions}
        >
          <PresenterForm initialValues={this.props.initialValues} onCancel={this.props.onCancel} configuration={this.props.configuration} {...this.props.formProps}><EditPackageItemView /></PresenterForm>
        </Presenter>
       </ViewEditPresenter>
    );
  }

  private getActions = (): IActionButton[] => [
    ...(this.props.actions || []),
    {
      isVisible: () => this.props.canNavigateTo(FormType.Edit, this.props.activeKey)
        && !this.props.isEdit
        && (roles.length === 0 || roles.some((role) => this.props.userRoles.has(role))),
      onClick: () => this.props.navigateTo(FormType.Edit, this.props.activeKey),
      label: 'Edit',
    },
  ]
}
