import { IExternalFormField } from '../Form/FormField';

import { CellType } from './Cell';
import { FooterType } from './Footer';

/******************************************************************************/
/**                                                                          **/
/******************************************************************************/

export enum ColumnAlignment {
  Center = 'Center',
  Left = 'Left',
  Right = 'Right',
}

export enum RowState {
  GreenText = 'GreenText',
  RedText = 'RedText',
  Grey = 'Grey',
  Error = 'Error',
  Warning = 'Warning',
  CrossedOut = 'CrossedOut',
  Default = 'Default',
}

export enum CellStatus {
  Warning = 'Warning',
  Danger = 'Danger',
  Success = 'Success',
  Info = 'Info',
}

export interface IStatusDefinition {
  [statusValue: string]: {
    label: string;
    type?: CellStatus;
    color?: string;
  };
}

export interface IGetRowState<T> {
  (row: IRowInfo<T>): RowState;
}

export interface IColumnAccessorFunction<T, R = string> {
  (data: T): R;
}

export interface IColumnAggregateFunction<T, V> {
  (columnValues: V[], tableValues: T[]): V;
}

export interface IFilterFunction {
  (query: { pivotId?: string, id?: string, value: string }, row: any, column: any): boolean;
}

export interface IColumnCellFormatFunction<T> {
  <V>(value: V, row: T): string | React.ReactElement<any>; // TODO consult @domagoj.cerjan about types here
}

export interface ICellEventHandler<T> {
  (event: React.SyntheticEvent<HTMLDivElement | HTMLButtonElement>, row: T, original: IRowInfo<T>): void;
}

type GenericFormField = Omit<IExternalFormField<any, any, any, any>, 'name' | 'onChange' | 'onBlur'>;

export type IFieldProps<P extends GenericFormField> = Partial<P> & {
  component: React.ComponentType<IExternalFormField<any, any, any, any>>;
  name?: (index: number) => string;
  onBlur?: (index: number) => IExternalFormField<any, any, any, any>['onBlur'];
  onChange?: (index: number) => IExternalFormField<any, any, any, any>['onChange'];
  getOptions?: <O = any>(index: number) => O[];
};

export interface IColumnConfig<T> {
  Cell?: any;
  Header?: React.ComponentType<any>;
  Footer?: React.ComponentType<any> | string;
  accessor?: DeepKeyOf<T> | IColumnAccessorFunction<T, any>;
  aggregate?: IColumnAggregateFunction<T, any>; // #TODO @bigd -> can this be typed correctly?
  alignment?: ColumnAlignment;
  cellType?: CellType;
  className?: string;
  dateTimeFormat?: string;
  field?: DeepKeyOf<T> | string;
  filterable?: boolean;
  filterMethod?: IFilterFunction;
  fixedWidth?: boolean; // #TODO @bigd -> implement it down the chain
  footerLabel?: string;
  footerType?: FooterType;
  formatter?: IColumnCellFormatFunction<T>;
  minWidth?: number;
  maxWidth?: number;
  sortable?: boolean;
  style?: any;
  title: string;
  weight?: number;
  isExternal?: boolean | ((row: T) => boolean);
  isVisible?: boolean | (() => boolean);
  actions?: Array<IActionInfo<T>>;
  onClick?: ICellEventHandler<T>;
  onChange?: ICellEventHandler<T>;
  onBlur?: ICellEventHandler<T>;
  url?: string;
  download?: boolean;
  openInNewTab?: boolean;
  constructUrl?: IConstructUrl<T>;
  statusDefinition?: IStatusDefinition;
  fieldProps?: IFieldProps<GenericFormField>;
  sectionName?: string;
  disabled?: boolean | ((row: T) => boolean);
}

export type IRowConfig<T> = Array<IColumnConfig<T>>;

export type IRowsConfig<Ts extends any[]> = Array<IColumnConfig<Ts[0]>>;

export type IAdditionalRowConfig<T> = Array<Omit<IColumnConfig<T>, 'title'>>;

export interface IRowInfo<T> {
  row: any; // #TODO @bigd - type me if possible
  rowValues: any; // #TODO @bigd - type me if possible

  original: T;
  index: number;
  viewIndex: number;
  pageSize: number;
  page: number;
  level: number;
  nestingPath: number[];
  aggregated: boolean;
  groupedByPivot: boolean;
  // subRows: any[]; // - not used
}

interface IInternalRowPluckedValues<A extends any> {
  [key: string]: A;
}
export interface IInternalRowInfo<T> extends IInternalRowPluckedValues<any> {
  _index: number;
  _nestingLevel: number[];
  _original: T;
  // _subRows:  // - not used
  _viewIndex: number;
}

/******************************************************************************/
/** Row and Cell props                                                       **/
/******************************************************************************/

export interface IInlineStyle {
  [key: string]: string | number;
}
export interface ITdProps {
}

export interface IResizedInfo {
}

export interface IActionInfo<T> {
  title?: string | ((original: T) => string);
  tooltip?: string | ((original: T) => string);
  icon?: string;
  className?: string;
  isVisible?: boolean | ((row: any) => boolean);
  disabled?: boolean | ((row: any) => boolean);
  onClick?: ICellEventHandler<T>;
  getUrl?: (conflict: T) => string;
  customComponent?: (row: T) => React.ReactElement<any>;
}

export interface IColumnProps<T> {
  actions?: Array<IActionInfo<T>>;
  className?: string;
  styles?: { [key: string]: string | number };
  onClick?: ICellEventHandler<T>;
  onChange?: ICellEventHandler<T>;
  onBlur?: ICellEventHandler<T>;
  url?: string;
  openInNewTab?: boolean;
  constructUrl?: IConstructUrl<T>;
  statusDefinition?: IStatusDefinition;
}

export interface ICellInfo<T, V> {
  isExpanded: boolean; // true if this row is expanded
  value: V; // the materialized value of this cell
  resized: IResizedInfo; // the resize information for this cell's column
  show: boolean; // true if the column is visible
  width: number; // the resolved width of this cell
  maxWidth: number; // the resolved maxWidth of this cell
  tdProps: ITdProps; // the resolved tdProps from `getTdProps` for this cell
  columnProps: IColumnProps<T>; // the resolved column props from 'getProps' for this cell's column
  className?: string; // the resolved array of classes for this cell
  style?: IInlineStyle; // the resolved styles for this cell
}

export interface IConstructUrl<T> {
  (original: T, row: IRowInfo<T>): string;
}

export interface ICellInternalProps<T, V> {
  actions?: Array<IActionInfo<T>>;
  accessor?: DeepKeyOf<T> | IColumnAccessorFunction<T, V>;
  aggregate?: IColumnAggregateFunction<T, V>;
  dateTimeFormat?: string;
  formatter?: IColumnCellFormatFunction<T>;
  field: DeepKeyOf<T> | string;
  label?: string;
  url?: string;
  download?: boolean;
  openInNewTab?: boolean;
  isExternal?: boolean | ((row: T) => boolean);
  constructUrl?: IConstructUrl<T>;
  onClick?: ICellEventHandler<T>;
  onChange?: ICellEventHandler<T>;
  onBlur?: ICellEventHandler<T>;
  statusDefinition?: IStatusDefinition;
  tooltipMessage?: string;
  fieldProps?: IFieldProps<GenericFormField>;
  sectionName?: string;
}

export interface ICellProps<T, V> extends IRowInfo<T>, ICellInfo<T, V>, ICellInternalProps<T, V> {}

export interface IOptionalFieldCellProps<T, V> extends Omit<ICellProps<T, V>, 'formatter'> {}
