import * as React from 'react';

import { DateFormatter } from '../../../util/Formatters/DateFormatter';
import { ICellProps } from '../interfaces';
import { PlaceholderValue } from './PlaceholderValue';

interface IDateTimeCellProps<T> extends ICellProps<T, DateStr | TimestampStr> {
}

export class DateTimeCell<T> extends React.PureComponent<IDateTimeCellProps<T>> {

  render() {
    const {
      className,
      dateTimeFormat,
      formatter,
      row,
      style,
      value,
    } = this.props;

    const fmtFunction = formatter != null ? formatter : ((val: DateStr) => DateFormatter.formatPresentationalDate(val, dateTimeFormat));

    return (
      <div style={style} className={className}>
        { value != null
          ? fmtFunction(value as DateStr, row?._original)
          : <PlaceholderValue /> }
      </div>
    );
  }
}

export class DateCell<T> extends React.PureComponent<IDateTimeCellProps<T>> {

  render() {
    return <DateTimeCell { ...this.props } />;
  }
}
