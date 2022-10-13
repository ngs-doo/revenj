import * as React from 'react';

import { formatNumber } from '../../../util/Formatters/NumberFormatter';
import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

export const INTEGER_FORMAT: string = '#,##0';

interface IIntegerCellProps<T> extends ICellProps<T, IntegerStr> {}

export class IntegerCell<T> extends React.PureComponent<IIntegerCellProps<T> & React.HTMLProps<HTMLDivElement>> {
  render() {
    const {
      className,
      formatter,
      row,
      style,
      value,
    } = this.props;

    const fmtFunction = formatter != null ? formatter : (val: string) => formatNumber(val, INTEGER_FORMAT);

    return (
      <div style={style} className={className} >
        { value != null
          ? fmtFunction(value, row?._original)
          : <PlaceholderValue /> }
      </div>
    );
  }
}
