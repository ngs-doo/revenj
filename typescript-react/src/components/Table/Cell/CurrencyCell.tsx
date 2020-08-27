import * as React from 'react';

import { CurrencyFormatter } from '../../../util/Formatters/CurrencyFormatter';
import { Numeric } from '../../../util/NumberUtils/NumberUtils';

import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

interface ICurrencyCellProps<T> extends ICellProps<T, Numeric> {}

export class CurrencyCell<T> extends React.PureComponent<ICurrencyCellProps<T> & React.HTMLProps<HTMLDivElement>> {
  render() {
    const {
      className,
      formatter,
      row,
      style,
      value,
    } = this.props;

    const fmtFunction = formatter ?? ((value: Numeric) => CurrencyFormatter.formatNumber(value));

    return (
      <div style={style} className={className} >
        { value != null
          ? fmtFunction(value as Numeric, row?._original)
          : <PlaceholderValue /> }
      </div>
    );
  }
}
