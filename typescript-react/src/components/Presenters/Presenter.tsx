import classNames from 'classnames';
import * as React from 'react';
import { connect } from 'react-redux';
import { getFormValues } from 'redux-form';

import { IGeneratedConcept } from '../Form/Context';
import { Header } from '../Header/Header';
import { Internationalised } from '../I18n/I18n';
import { localizeTextIfMarked } from '../I18n/service';
import { Actions, IActionButton } from './Actions';
import styles from './Presenter.module.css';

interface IPresenterPublicProps<T> {
  title: string;
  presenterName: string;
  domainObject: IGeneratedConcept<T>;
  filterField?: string;
  exportFile?: string;
  reportEntryCommandName?: string;
  actions?: IActionButton[];
  userRoles: Set<string>;
  onForbidden: () => void;
  roles?: string[];
}

interface IPresenterStateProps<T> {
  values?: T;
}

interface IPresenter<T> extends React.PropsWithChildren<IPresenterPublicProps<T>>, IPresenterStateProps<T> {}

const mapStateToProps = (state: any, ownProps: IPresenterPublicProps<any>): IPresenterStateProps<any> => ({
  values: getFormValues(ownProps.presenterName ?? ownProps.domainObject.domainObjectName)(state),
});

export class PresenterBare<T> extends React.PureComponent<IPresenter<T>> {

  public componentDidMount() {
    this.checkRoles();
  }

  public render() {
    const { actions, children, title, exportFile, filterField, reportEntryCommandName, userRoles, values } = this.props;
    const actionsWithValues = actions?.map(a => ({ ...a, values }));

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
                      actions={actionsWithValues ?? []}
                      templateType={exportFile}
                      reportEntryCommandName={reportEntryCommandName}
                      userRoles={userRoles}
                      filterField={filterField}
                    />
                  </Header>
                ) : (
                  <div className={styles.ActionsContainer}>
                    <Actions
                      actions={actionsWithValues ?? []}
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
      const { userRoles, roles, onForbidden } = this.props;
      const hasRole = roles == null || roles.length === 0 || userRoles.size === 0 || roles.some((role) => userRoles.has(role));

      if (!hasRole) {
        onForbidden();
      }
    } catch (error) {
      console.error(error);
      throw error;
    }
  }
}

export const Presenter = connect(mapStateToProps)(PresenterBare) as React.ComponentType<IPresenterPublicProps<any>>;
