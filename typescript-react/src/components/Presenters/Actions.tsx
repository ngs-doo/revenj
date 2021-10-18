import classNames from 'classnames';
import * as React from 'react';
import { ButtonVariant } from 'react-bootstrap/esm/types';

import { ApiContext } from '../Api/ApiContext';
import { Buttons } from '../Buttons/Buttons';
import { IComponentButtonProps } from '../Buttons/interfaces';
import { ListPresenterComponent, UpdatePresenterContext } from '../Form/Context';
import styles from './Presenter.module.css';

export interface ISimpleButtonAction {
  className?: string;
  disabled?: boolean;
  label: string;
  url?: string;
  variant?: ButtonVariant;
  isVisible?: (actionsProps: IActions) => boolean;
  onClick?: (actionProps?: IActions) => void;
}

interface IComponentActionButton {
  Component: React.ComponentType<IComponentButtonProps>;
  values?: any;
  isVisible?: (actionsProps: IActions) => boolean;
}

export type IActionButton = ISimpleButtonAction | IComponentActionButton;

const isComponentButton = (btn: IActionButton): btn is IComponentActionButton =>
  btn && (btn as IComponentActionButton).Component != null;

export interface IActions {
  presenterName?: string;
  conceptName?: string;
  reportEntryCommandName?: string;
  templateType?: string;
  filterField?: string;
  actions: IActionButton[];
  // HACK: This is used to cache-bust on DSL generated pages, think about this... Rather generate actions?
  userRoles?: Set<any>;
}

export class Actions extends React.PureComponent<IActions> {
  public static contextType = ApiContext;
  public context!: React.ContextType<typeof ApiContext>;

  private get buttons(): IActionButton[] {
    const { actions } = this.props;

    const visibleActions = actions.filter(
      (action) => action.isVisible == null || action.isVisible(this.props),
    );

    return visibleActions.map((action) => {
      if (isComponentButton(action)) {
        return action;
      } else {
        return {
          ...action,
          onClick: () => action.onClick?.(this.props),
        };
      }
    });
  }

  public render() {
    return (
      <div className={classNames(styles.Actions, 'dslActions')}>
        <Buttons buttons={this.buttons} maxVisibleButtons={3} />
        {this.renderExportButton()}
      </div>
    );
  }

  private renderExportButton = () => {
    const { templateType, reportEntryCommandName, filterField } = this.props;
    const { ExportButton } = this.context!;

    if (templateType == null) {
      return null;
    }

    // Only one of these will be rendered, as a presenter cannot be a list and a dashboard at the same time.
    // Create context obviously makes no sense, as there is nothing to export yet
    return (
      <React.Fragment>
        <ListPresenterComponent>
          {
            (context) => context != null ? (
              <ExportButton
                className={styles.ExportButton}
                templateType={templateType}
                onDownload={(customTemplate?: string) => context!.onExport(templateType!, customTemplate, reportEntryCommandName, filterField)}
                disabled={context.isExporting || context.isLoading}
              />
            ) : null
          }
        </ListPresenterComponent>
        <UpdatePresenterContext.Consumer>
          {
            (context) => context != null ? (
              <ExportButton
                className={styles.ExportButton}
                templateType={templateType}
                onDownload={(customTemplate?: string) => context!.onExport(templateType!, customTemplate, reportEntryCommandName)}
                disabled={context.isExporting || context.isLoading}
              />
            ) : null
          }
        </UpdatePresenterContext.Consumer>
      </React.Fragment>
    );
  }
}
