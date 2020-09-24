import * as React from 'react';


import { FormType, IActionButton, Presenter } from 'revenj';
import { ISearchPackages as demoISearchPackages } from '../interface/demo.SearchPackages';
import { SearchPackages as demoSearchPackages } from '../class/demo.SearchPackages';
import { ISearchPackagesFilter as demoISearchPackagesFilter } from '../interface/demo.SearchPackagesFilter';
import { FiltersForm } from 'revenj';
import { SearchPackagesFilter as SearchPackagesFilterView } from '../views/demo.SearchPackagesSearchPackagesFilter';
import { FieldRegistryContext } from 'revenj';
import GridPackageVM, { getDefinition, IGridPackageVM } from '../grid/demo.SearchPackagesPackageVM';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { getTableActions, mergeColumnConfigs } from 'revenj';
import { EditPackagePresenter } from './demo.EditPackage';
import { ListPresenter } from 'revenj';

import Role from '../security/Role';
import { demoSearchPackagesPresenterName } from './demo.PresenterName';
import { demoSearchPackagesDomainObjectName } from './demo.DomainObjectName';

export interface ISearchPackagesPresenter {
  title?: string;
  userRoles: Set<Role>;
  actions?: IActionButton[];
  canNavigateTo: (formType: FormType, to?: any) => boolean;
  navigateTo: (formType: FormType, id?: any) => void;
  onSubmit?: (data: demoISearchPackages, generatedHandler: (original: demoISearchPackages) => Promise<demoISearchPackages>) => Promise<demoISearchPackages>;
  onForbidden: () => void;
  initialValues?: Partial<demoISearchPackagesFilter>;
  reportEntryCommandName?: string;
  gridProps?: Partial<IGridPackageVM>;
  definition?: IGridPackageVM['definition'];
  getIdentifier: (row: demoIPackageVM) => string;
  requestOnMountWith?: demoISearchPackages;
}

export interface ISearchPackagesPresenterState {
  actions: IActionButton[];
}


export const roles = demoSearchPackages.roles;
export type DomainObject = demoISearchPackages;

export class SearchPackagesPresenter extends React.PureComponent<ISearchPackagesPresenter, ISearchPackagesPresenterState> {
  public static presenterName: string = demoSearchPackagesPresenterName;
  public static domainObjectName: string = demoSearchPackagesDomainObjectName;
  public static roles = demoSearchPackages.roles;
  public static title = 'Search Packages';

  public state: ISearchPackagesPresenterState = {
    actions: [],
  };

  private extraGridProps: Partial<IGridPackageVM> = {
    pagination: true,
    };

  public static contextType = FieldRegistryContext;

  public context!: React.ContextType<typeof FieldRegistryContext>;

  public componentDidMount() {
    this.setState({ actions: this.getActions() });
  }

  public componentDidUpdate(prevProps: ISearchPackagesPresenter) {
    if (prevProps.actions !== this.props.actions) {
      this.setState({ actions: this.getActions() });
    }
  }

  public render() {
    return (
      <ListPresenter
        domainObject={demoSearchPackages}
        presenterName={SearchPackagesPresenter.presenterName}
        onSubmit={this.props.onSubmit}
          requestOnMountWith={this.props.requestOnMountWith}
          resultField={['packages'] as any}
      >
	    <Presenter
          title={this.props.title != null ? this.props.title : 'Search Packages'}
          presenterName={SearchPackagesPresenter.presenterName}
          domainObject={demoSearchPackages}
          userRoles={this.props.userRoles}
          onForbidden={this.props.onForbidden}
          actions={this.state.actions}
        filterField='filter'
        reportEntryCommandName={this.props.reportEntryCommandName}
        exportFile='SearchPackages'
        >
          <FiltersForm initialValues={this.props.initialValues} formUnderKey='filter'><SearchPackagesFilterView /></FiltersForm>
          <GridPackageVM definition={this.getDefinition()} { ...this.extraGridProps } {...this.props.gridProps} />
        </Presenter>
       </ListPresenter>
    );
  }

  private getActions = (): IActionButton[] => [
    ...(this.props.actions || []),
    {
      isVisible: () => this.props.canNavigateTo(FormType.Create)
        && (roles.length === 0 || roles.some((role) => this.props.userRoles.has(role))),
      onClick: () => this.props.navigateTo(FormType.Create),
      label: 'Create',
    },
  ]

  private getDefinition = () => mergeColumnConfigs(
    this.props.definition != null ? this.props.definition : getDefinition(this.context.Fields),
    getTableActions(this.props.navigateTo, this.props.canNavigateTo, this.props.userRoles, this.props.getIdentifier, { edit: EditPackagePresenter.roles,  view: EditPackagePresenter.roles, }),
  )
}
