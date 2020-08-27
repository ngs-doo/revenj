import { CurrencyFormatter } from '../../../util/Formatters/CurrencyFormatter';
import { DateFormatter } from '../../../util/Formatters/DateFormatter';
import { DateTimeFormatter } from '../../../util/Formatters/DateTimeFormatter';
import {
  ColumnAlignment,
  IColumnConfig,
} from '../interfaces';
import { ActionsCell } from './ActionsCell';
import { BooleanCell } from './BooleanCell';
import { CurrencyCell } from './CurrencyCell';
import {
  DateCell,
  DateTimeCell,
} from './DateTimeCell';
import { DecimalCell } from './DecimalCell';
import { FieldCell } from './FieldCell';
import { LinkCell } from './LinkCell';
import { S3FileCell } from './S3FileCell';
import { StatusCell } from './StatusCell';
import { TextCell } from './TextCell';
import { TooltipCell } from './TooltipCell';

export enum CellType {
  Actions = 'Actions',
  Boolean = 'Boolean',
  Currency = 'Currency',
  Date = 'Date',
  DateTime = 'DateTime',
  Decimal = 'Decimal',
  Field = 'Field',
  Link = 'Link',
  Number = 'Number',
  Status = 'Status',
  Text = 'Text',
  Object = 'Object',
  Tooltip = 'Tooltip',
  S3FileCell = 'S3FileCell',
}

export const getCellComponent = <T>(column: IColumnConfig<T>) => {
  switch (column.cellType) {
  case CellType.Actions: return ActionsCell;
  case CellType.Currency: return CurrencyCell;
  case CellType.Date: return DateCell;
  case CellType.DateTime: return DateTimeCell;
  case CellType.Decimal: return DecimalCell;
  case CellType.Link: return LinkCell;
  case CellType.Status: return StatusCell;
  case CellType.Boolean: return BooleanCell;
  case CellType.Tooltip: return TooltipCell;
  case CellType.S3FileCell: return S3FileCell;
  case CellType.Field: return FieldCell;
  case CellType.Text:
  case CellType.Number:
  case CellType.Object:
  default: return TextCell;
  }
};

// tslint:disable:line ban-types
export const getDefaultFormatter = <T>(column: IColumnConfig<T>): Function | undefined => {
  switch (column.cellType) {
  case CellType.Object: return (v: any) => String(v);
  case CellType.Currency: return CurrencyFormatter.formatCurrency;
  case CellType.Date: return (value: DateStr) => DateFormatter.formatPresentationalDate(value, column.dateTimeFormat);
  case CellType.DateTime: return (value: TimestampStr) => DateTimeFormatter.formatDateTime(value, column.dateTimeFormat);
  default: return;
  }
};

export const getDefaultAlignment = <T>(column: IColumnConfig<T>): ColumnAlignment => {
  switch (column.cellType) {
      case CellType.Currency: return ColumnAlignment.Right;
      default: return ColumnAlignment.Left;
  }
};

export {
  CurrencyCell,
  TextCell,
};
