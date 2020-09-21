import classNames from 'classnames';
import * as React from 'react';

import { IGeneratedConcept } from '../Form/Context';
import { Header } from '../Header/Header';
import { Internationalised } from '../I18n/I18n';
import { localizeTextIfMarked } from '../I18n/service';

import { Actions, IActionButton } from './Actions';

import styles from './Presenter.module.css';

interface IPresenter<T> {
  title: string;
  presenterName: string;
  domainObject: IGeneratedConcept<T>;
  filterField?: string;
  exportFile?: string;
  reportEntryCommandName?: string;
  actions?: IActionButton[];
  userRoles: Set<string>;
  onForbidden: () => void;
}

export class Presenter<T> extends React.PureComponent<IPresenter<T>> {

  public componentDidMount() {
    this.checkRoles();
  }

  public render() {
    const { actions, children, title, exportFile, filterField, reportEntryCommandName, userRoles } = this.props;

    return (
      <Internationalised>
        {
          ({ localize }) => (
            <div className={classNames('theme-modern', styles.Presenter)}>
              {/* Presenters without a title don't get to have a header at all */}
              {
                title ? (
                  <Header title={localizeTextIfMarked(localize, title)}>
                    <Actions
                      actions={actions ?? []}
                      templateType={exportFile}
                      reportEntryCommandName={reportEntryCommandName}
                      userRoles={userRoles}
                      filterField={filterField}
                    />
                  </Header>
                ) : (
                  <div className={styles.ActionsContainer}>
                    <Actions
                      actions={actions ?? []}
                      templateType={exportFile}
                      reportEntryCommandName={reportEntryCommandName}
                      userRoles={userRoles}
                      filterField={filterField}
                    />
                  </div>
                )
              }
              {children}
            </div>
          )
        }
      </Internationalised>
    );
  }

  private checkRoles = () => {
    try {
      const { userRoles, domainObject, onForbidden } = this.props;
      const hasRole = domainObject.roles.length === 0 || userRoles.size === 0 || domainObject.roles.some((role) => userRoles.has(role));

      if (!hasRole) {
        onForbidden();
      }
    } catch (error) {
      console.error(error);
      throw error;
    }
  }
}
