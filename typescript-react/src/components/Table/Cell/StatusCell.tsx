import classNames from 'classnames';
import * as React from 'react';

import { Status } from '../../Status/Status';
import { ICellProps } from '../interfaces';

import styles from './Cells.module.css';

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
            <Status
              color={status.color}
              label={formatter ? formatter(status.label, row ? row._original : row) : status.label}
              type={status.type}
            />
          )
          : null }
      </div>
    );
  }
}
