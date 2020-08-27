import classNames from 'classnames';
import * as React from 'react';

import styles from './DetailRow.module.css';
import { FoldedColumns } from './FoldedColumns';

import { IGetRowState, IRowConfig, IRowInfo } from '../interfaces';

export interface IDetailRowProps<T> {
  data: IRowInfo<T>;
  foldedColumns: IRowConfig<T>;
  ExtraComponent?: React.ComponentType<T>;
  FoldedColumnsComponent?: React.ComponentType<T>;
  hasFoldedColumns?: boolean;
  getRowState?: IGetRowState<T>;
}

export class DetailRow<T> extends React.PureComponent<IDetailRowProps<T>> {
  static defaultProps: Partial<IDetailRowProps<any>> = {
    FoldedColumnsComponent: FoldedColumns,
    hasFoldedColumns: false,
  };

  render() {
    const {
      ExtraComponent,
      FoldedColumnsComponent,
      hasFoldedColumns,
    } = this.props;

    const Folded = FoldedColumnsComponent!;

    return (
      <div className={classNames(styles.DetailRowWrapper, 'table-detail-row')}>
        { hasFoldedColumns
          ? (
            <div className={styles.FoldedColumnsWrapper}>
              <Folded { ...(this.props as any) } />
            </div>
          )
          : null }
        { ExtraComponent
          ? (
            <div className={styles.ExtraWrapper}>
              <ExtraComponent { ...(this.props as any) } />
            </div>
          )
          : null }
      </div>
    );
  }
}
