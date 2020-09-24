import React from 'react';
import { FormType } from 'revenj';
import { RouteComponentProps } from 'react-router';

import { AuthContext } from '../../components/Auth';
import { CreatePackagePresenter } from '../../dsl/presenters/demo.CreatePackage';
import { ICreatePackage as ICommand } from '../../dsl/interface/demo.CreatePackage';

interface ICreatePackage extends RouteComponentProps<{}> {}

export const CreatePackage: React.FC<ICreatePackage> = ({ history }) => {
  const { user, onForbidden } = React.useContext(AuthContext)!;
  const constTrue = React.useCallback(() => true, []);
  const navigateTo = React.useCallback((formType: FormType, id?: UUID) => {
    switch (formType) {
      case FormType.Create: return history.push(`/package/create`);
      case FormType.Edit: return history.push(`/package/${encodeURIComponent(id!)}/edit`);
      case FormType.View: return history.push(`/package/${encodeURIComponent(id!)}/dashboard`);
    }
  }, [history]);
  return (
    <CreatePackagePresenter
      userRoles={user?.roles ?? new Set()}
      onForbidden={onForbidden}
      canNavigateTo={constTrue}
      navigateTo={navigateTo}
      onCancel={history.goBack}
      onSubmitSuccess={(command: ICommand) => navigateTo(FormType.View, command.ID!)}
    />
  )
};
