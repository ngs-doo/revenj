import * as React from 'react';

import { formatNumber } from '../../../util/Formatters/NumberFormatter';
import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

export const SHORT_FORMAT: string = '###0';

interface IShortCellProps<T> extends ICellProps<T, ShortStr> {}

export class ShortCell<T> extends React.PureComponent<IShortCellProps<T> & React.HTMLProps<HTMLDivElement>> {
  render() {
    const {
      className,
      formatter,
      row,
      style,
      value,
    } = this.props;

    const fmtFunction = formatter != null ? formatter : (val: string) => formatNumber(val, SHORT_FORMAT);

    return (
      <div style={style} className={className} >
        { value != null
          ? fmtFunction(value, row?._original)
          : <PlaceholderValue /> }
      </div>
    );
  }
}
