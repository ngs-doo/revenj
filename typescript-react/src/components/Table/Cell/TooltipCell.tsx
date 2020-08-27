import classNames from 'classnames';
import * as React from 'react';

import { TooltipOnHover } from '../../Tooltip/Tooltip';
import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

import styles from './Cells.module.css';

export class TooltipCell<T> extends React.PureComponent<ICellProps<T, string>> {
  render() {
    const {
      className,
      formatter,
      onClick,
      original,
      row,
      value,
      tooltipMessage,
      style,
    } = this.props;

    return (
      <div
        style={style}
        className={classNames(className, styles.TooltipCell, { [styles.Clickable]: Boolean(onClick) })}
        onClick={(event) => onClick && onClick(event, original, this.props)}
      >
        <TooltipOnHover type='info' message={tooltipMessage!}>
          { value != null
            ? formatter ? formatter(value, row ? row._original : row) : value
            : <PlaceholderValue />
          }
        </TooltipOnHover>
      </div>
    );
  }
}
