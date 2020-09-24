import React from 'react';
import { FormType } from 'revenj';
import { RouteComponentProps } from 'react-router';

import { AuthContext } from '../../components/Auth';
import { PackageStatus } from '../../dsl/enum/demo.PackageStatus';
import { SearchPackagesPresenter } from '../../dsl/presenters/demo.SearchPackages';
import { IPackageVM } from '../../dsl/interface/demo.PackageVM';

interface IListPackages extends RouteComponentProps<{}> {}

const canNavigateTo = (formType: FormType, item: IPackageVM) => formType !== FormType.Edit || item?.status === PackageStatus.Pending;

export const ListPackages: React.FC<IListPackages> = ({ history }) => {
  const { user, onForbidden } = React.useContext(AuthContext)!;
  const getPackageID = React.useCallback((p: IPackageVM) => p.ID, []);
  const navigateTo = React.useCallback((formType: FormType, id?: UUID) => {
    switch (formType) {
      case FormType.Create: return history.push(`/package/create`);
      case FormType.Edit: return history.push(`/package/${encodeURIComponent(id!)}/edit`);
      case FormType.View: return history.push(`/package/${encodeURIComponent(id!)}/dashboard`);
    }
  }, [history]);

  return (
    <SearchPackagesPresenter
      userRoles={user?.roles ?? new Set()}
      onForbidden={onForbidden}
      canNavigateTo={canNavigateTo}
      navigateTo={navigateTo}
      getIdentifier={getPackageID}
    />
  )
};
