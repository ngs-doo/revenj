import classNames from 'classnames';
import * as React from 'react';

import { CellStatus, ICellProps } from '../interfaces';

import styles from './Cells.module.css';

const getStatusStyle = (status: CellStatus) => {
  switch (status) {
  case CellStatus.Danger: return 'label-important';
  case CellStatus.Info: return 'label-info';
  case CellStatus.Success: return 'label-success';
  case CellStatus.Warning:
  default: return 'label-warning';
  }
};

export class StatusCell<T> extends React.PureComponent<ICellProps<T, string>> {
  render() {
    const {
      className,
      formatter,
      onClick,
      original,
      row,
      value,
      style,
      statusDefinition,
    } = this.props;

    const cellClass = classNames(className, { [styles.clickable]: Boolean(onClick) }, styles.StatusCell);
    const status = statusDefinition![value]!;

    return (
      <div
        style={style}
        className={cellClass}
        onClick={(event) => onClick && onClick(event, original, this.props)}
      >
        { status != null
          ? (
            <span
              className={classNames(styles.Label, status.type != null ? getStatusStyle(status.type) : undefined)}
              style={status.color ? { color: status.color } : undefined}
            >
              { formatter
                ? formatter(status.label, row ? row._original : row)
                : status.label }
            </span>
          )
          : null }
      </div>
    );
  }
}
