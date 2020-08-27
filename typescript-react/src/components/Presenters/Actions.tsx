import classNames from 'classnames';
import * as React from 'react';

import { ApiConsumer } from '../Api/ApiContext';
import { Buttons } from '../Buttons/Buttons';

import styles from './Presenter.module.css';

interface ISimpleButtonAction {
  className?: string;
  disabled?: boolean;
  label: string;
  href?: string;
  isVisible?: (actionsProps: IActions) => boolean;
  onClick?: (actionProps?: IActions) => void;
}

interface IComponentActionButton {
  Component: React.ComponentType<{}>;
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
  private get buttons(): IActionButton[] {
    const { actions } = this.props;

    const visibleActions = actions.filter((action) => action.isVisible == null || action.isVisible(this.props));

    return visibleActions.map((action) => {
      if (isComponentButton(action)) {
        return action;
      } else {
        return {
          ...action,
          onClick: action.onClick != null ? () => action.onClick!(this.props) : undefined,
        };
      }
    });
  }

  public render() {
    const { reportEntryCommandName, templateType, filterField } = this.props;
    return (
      <ApiConsumer>
        {
          (ctx) => {
            const ExportButton = ctx!.ExportButton;
            return (
              <div className={classNames(styles.Actions, 'dslActions')}>
                <Buttons
                  buttons={this.buttons}
                  maxVisibleButtons={3}
                />
                {
                  templateType != null ? (
                    <ExportButton
                      className={styles.ExportButton}
                      template={templateType}
                      filterField={filterField}
                      conceptOverride={reportEntryCommandName}
                    />
                  ) : null
                }
              </div>
            )
          }
        }
      </ApiConsumer>
    );
  }
}
