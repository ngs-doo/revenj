import classNames from 'classnames';
import * as React from 'react';

import styles from './FoldedColumns.module.css';

import { get } from '../../../util/FunctionalUtils/FunctionalUtils';
import { getCellComponent } from '../Cell';
import { getCellProps, getColumnId } from '../config';
import { IRowConfig } from '../interfaces';

import { IDetailRowProps } from './DetailRow';

export class FoldedColumns<T> extends React.PureComponent<IDetailRowProps<T>> {
  render() {
    const {
      data,
      foldedColumns,
      getRowState,
    } = this.props;

    const {
      row,
      original,
    } = data;

    const columns = (Object.keys(row) as Array<keyof T>).reduce((folded: IRowConfig<T>, columnName: keyof T) => {
      const column = foldedColumns.find((foldedColumn) => {
        const field = getColumnId(foldedColumn);
        return field === columnName;
      });
      if (column != null) {
        return [...folded, column];
      } else {
        return folded;
      }
    }, []);

    return (
      <div className={classNames(styles.FoldedColumnsWrapper, getRowState && getRowState(data))}>
        { columns
          .map((column, index) => {
            // #TODO @bigd -> murderify this :any
            const Cell: any = getCellComponent(column);
            const cellProps = getCellProps(column);

            const accessor = cellProps.accessor;
            const value = typeof accessor === 'function'
              ? accessor(original)
              : get(original, accessor as unknown as DeepKeyOf<T>);

            return (
              <div
                key={index}
                className={styles.FoldedColumnContainer}
              >
                <div className={styles.FoldedColumnName}>
                  { column.Header ? <column.Header /> : column.title }
                </div>
                <div className={styles.FoldedColumnValue}>
                  <Cell { ...column } { ...cellProps } { ...data } value={value} />
                </div>
              </div>
            );
          }) }
      </div>
    );
  }
}
