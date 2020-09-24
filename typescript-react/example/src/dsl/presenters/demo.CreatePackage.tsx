import * as React from 'react';


import { FormType, IActionButton, Presenter } from 'revenj';
import { ICreatePackage as demoICreatePackage } from '../interface/demo.CreatePackage';
import { CreatePackage as demoCreatePackage } from '../class/demo.CreatePackage';
import { CreatePackage as CreatePackageItemView } from '../views/demo.CreatePackageCreatePackage';
import { IFormControlContext, PresenterForm, IPresenterFormPublicProps } from 'revenj';
import { CreatePresenter } from 'revenj';

import Role from '../security/Role';
import { demoCreatePackagePresenterName } from './demo.PresenterName';
import { demoCreatePackageDomainObjectName } from './demo.DomainObjectName';

export interface ICreatePackagePresenter {
  title?: string;
  userRoles: Set<Role>;
  actions?: IActionButton[];
  canNavigateTo: (formType: FormType, to?: any) => boolean;
  navigateTo: (formType: FormType, id?: any) => void;
  onSubmit?: (data: demoICreatePackage, generatedHandler: (original: demoICreatePackage) => Promise<demoICreatePackage>) => Promise<demoICreatePackage>;
  onForbidden: () => void;
  configuration?: IFormControlContext<demoICreatePackage>;
  onCancel?: () => void;
  initialValues?: Partial<demoICreatePackage>;
  formProps?: Partial<IPresenterFormPublicProps<demoICreatePackage>>;
  onSubmitSuccess?: (data: any) => void;
}

export interface ICreatePackagePresenterState {
  actions: IActionButton[];
}


export const roles = demoCreatePackage.roles;
export type DomainObject = demoICreatePackage;

export class CreatePackagePresenter extends React.PureComponent<ICreatePackagePresenter, ICreatePackagePresenterState> {
  public static presenterName: string = demoCreatePackagePresenterName;
  public static domainObjectName: string = demoCreatePackageDomainObjectName;
  public static roles = demoCreatePackage.roles;
  public static title = 'Enter Package';

  public state: ICreatePackagePresenterState = {
    actions: [],
  };

  public componentDidMount() {
    this.setState({ actions: this.getActions() });
  }

  public componentDidUpdate(prevProps: ICreatePackagePresenter) {
    if (prevProps.actions !== this.props.actions) {
      this.setState({ actions: this.getActions() });
    }
  }

  public render() {
    return (
      <CreatePresenter
        domainObject={demoCreatePackage}
        presenterName={CreatePackagePresenter.presenterName}
        onSubmit={this.props.onSubmit}
          onSubmitSuccess={this.props.onSubmitSuccess}
      >
	    <Presenter
          title={this.props.title != null ? this.props.title : 'Enter Package'}
          presenterName={CreatePackagePresenter.presenterName}
          domainObject={demoCreatePackage}
          userRoles={this.props.userRoles}
          onForbidden={this.props.onForbidden}
          actions={this.state.actions}
        >
          <PresenterForm initialValues={this.props.initialValues} onCancel={this.props.onCancel} configuration={this.props.configuration} {...this.props.formProps}><CreatePackageItemView /></PresenterForm>
        </Presenter>
       </CreatePresenter>
    );
  }

  private getActions = (): IActionButton[] => [
    ...(this.props.actions || []),
  ]
}
