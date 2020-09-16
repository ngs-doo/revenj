import * as React from 'react';

import { formatNumber } from '../../../util/Formatters/NumberFormatter';
import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

export const DECIMAL_FORMAT: string = '#,##0.00';

interface IDecimalCellProps<T> extends ICellProps<T, DecimalStr> {}

export class DecimalCell<T> extends React.PureComponent<IDecimalCellProps<T> & React.HTMLProps<HTMLDivElement>> {
  render() {
    const {
      className,
      formatter,
      row,
      style,
      value,
    } = this.props;

    const fmtFunction = formatter != null ? formatter : (val: string) => formatNumber(val, DECIMAL_FORMAT);

    return (
      <div style={style} className={className} >
        { value != null
          ? fmtFunction(value, row?._original)
          : <PlaceholderValue /> }
      </div>
    );
  }
}
