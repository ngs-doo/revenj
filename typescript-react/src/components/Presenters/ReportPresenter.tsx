import classNames from 'classnames';
import * as React from 'react';

import { IActionButton } from './Actions';
import {
  IListPresenterPublicProps,
  ListPresenter,
} from './ListPresenter';
import { Presenter } from './Presenter';
import styles from './Presenter.module.css';

interface IReportPublicProps<T, R extends DeepKeyOf<T>> extends IListPresenterPublicProps<T, R> {
  className?: string;
  title: string;
  actions?: IActionButton[];
  reportEntryCommandName?: string;
  onForbidden?: () => void;
  exportFile?: string;
  userRoles?: Set<string>;
  roles?: string[];
}

interface IReport<T, R extends DeepKeyOf<T>> extends IReportPublicProps<T, R> { }

export class Report<T, R extends DeepKeyOf<T>> extends React.PureComponent<IReport<T, R>> {
  public render() {
    const { actions, children, className, domainObject, exportFile, presenterName, reportEntryCommandName, userRoles, title, roles, ...props } = this.props;
    return (
      <div className={classNames('theme-modern', styles.Presenter, className)}>
        <ListPresenter domainObject={domainObject} {...props}>
          <Presenter
            title={title}
            actions={actions}
            domainObject={domainObject}
            userRoles={userRoles || new Set()}
            reportEntryCommandName={reportEntryCommandName}
            exportFile={exportFile}
            presenterName={presenterName!}
            onForbidden={this.onForbidden}
            roles={roles ?? domainObject.roles}
          >
            {children}
          </Presenter>
        </ListPresenter>
      </div>
    );
  }

  private onForbidden = () => this.props.onForbidden?.();
}
