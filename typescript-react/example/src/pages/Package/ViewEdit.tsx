import React from 'react';
import { RouteComponentProps } from 'react-router';
import {
  FormType,
  IActionButton,
} from 'revenj';

import { AuthContext } from '../../components/Auth';
import { LookupPackage } from '../../dsl/class/demo.LookupPackage';
import { MarkPackageDelivered } from '../../dsl/class/demo.MarkPackageDelivered';
import { MarkPackageInDelivery } from '../../dsl/class/demo.MarkPackageInDelivery';
import { MarkPackageReturned } from '../../dsl/class/demo.MarkPackageReturned';
import { PackageStatus } from '../../dsl/enum/demo.PackageStatus';
import { IEditPackage as ICommand } from '../../dsl/interface/demo.EditPackage';
import { EditPackagePresenter } from '../../dsl/presenters/demo.EditPackage';

interface IRouteProps {
  id: UUID;
}

interface IViewEditPackage extends RouteComponentProps<IRouteProps> {
  isEdit: boolean;
}

interface IConcreteViewEditPresenter extends RouteComponentProps<IRouteProps> {}

const canNavigateTo = (formType: FormType, item?: ICommand) =>
  formType !== FormType.Edit || item?.status === PackageStatus.Pending;

export const ViewEditPackage: React.FC<IViewEditPackage> = ({ isEdit, history, match }) => {
  const { user, onForbidden } = React.useContext(AuthContext)!;
  const [ activeItem, setActiveItem ] = React.useState<ICommand>();
  const onReceiveItem = React.useCallback((item: ICommand) => setActiveItem(item), [setActiveItem]);

  const navigateTo = React.useCallback((formType: FormType, id?: UUID) => {
    switch (formType) {
      case FormType.Create: return history.push(`/package/create`);
      case FormType.Edit: return history.push(`/package/${encodeURIComponent(id!)}/edit`);
      case FormType.View: return history.push(`/package/${encodeURIComponent(id!)}/dashboard`);
    }
  }, [history]);

  const onRequestItem = React.useCallback(
    (id: string) => LookupPackage.submit({ id: id as UUID }).then((response) => response.package!),
    [],
  );

  const actions: IActionButton[] = React.useMemo(() => [
    {
      label: 'Start Delivery',
      isVisible: () => !isEdit && activeItem?.status === PackageStatus.Pending,
      onClick: async () => {
        await MarkPackageInDelivery.submit({ packageID: activeItem!.ID });
        history.go(0);
      },
    },
    {
      label: 'Delivered',
      isVisible: () => !isEdit && activeItem?.status === PackageStatus.InDeliver,
      onClick: async () => {
        await MarkPackageDelivered.submit({ packageID: activeItem!.ID });
        history.go(0);
      },
    },
    {
      label: 'Returned',
      isVisible: () => !isEdit && activeItem?.status === PackageStatus.InDeliver,
      onClick: async () => {
        await MarkPackageReturned.submit({ packageID: activeItem!.ID });
        history.go(0);
      },
    },
  ], [isEdit, activeItem, history]);

  return (
    <EditPackagePresenter
      actions={actions}
      isEdit={isEdit}
      userRoles={user?.roles ?? new Set()}
      onForbidden={onForbidden}
      canNavigateTo={canNavigateTo}
      navigateTo={navigateTo}
      onCancel={history.goBack}
      onRequestItem={onRequestItem}
      onReceiveItem={onReceiveItem}
      activeKey={match.params.id}
      onSubmitSuccess={(command: ICommand) => navigateTo(FormType.View, command.ID!)}
    />
  )
};

export const ViewPackage: React.FC<IConcreteViewEditPresenter> = (props) =>
  <ViewEditPackage {...props} isEdit={false} />;

export const EditPackage: React.FC<IConcreteViewEditPresenter> = (props) =>
  <ViewEditPackage {...props} isEdit />;
