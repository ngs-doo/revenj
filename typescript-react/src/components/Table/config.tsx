import * as React from 'react';

import styles from './Table.css';

import { get } from '../../util/FunctionalUtils/FunctionalUtils';
import { DEFAULT_MIN_COLUMN_WIDTH } from './constants';
import { getCellComponent, getDefaultAlignment, getDefaultFormatter } from './Cell';
import { getFooterComponent } from './Footer';

import {
  ColumnAlignment,
  IColumnConfig,
  IFilterFunction,
  IRowConfig,
} from './interfaces';

const defaultFilterMethod: IFilterFunction = (query: any, row: IObjectAny, _column) => {
  const id = query.pivotId || query.id;
  return row[id] !== undefined ? String(row[id]).toLocaleLowerCase().includes(query.value.toLocaleLowerCase()) : true;
};

type ReactTableColumnConfig = any[];

export const getCssClassFromAlignment = (alignment?: ColumnAlignment): string => {
  if (alignment === ColumnAlignment.Right) {
    return styles.PullRight;
  } else if (alignment === ColumnAlignment.Left) {
    return styles.PullLeft;
  } else {
    return '';
  }
};

export function getColumnId<T>(column: IColumnConfig<T>): string {
  const field = column.field || column.title;
  if (Array.isArray(field)) {
    return field.join('.');
  } else {
    return `${field}`;
  }
}

export function getCellProps<T>(column: IColumnConfig<T>) {
  const maybeAccessor = column.accessor || column.field;
  let accessor;

  if (maybeAccessor != null) {
    if (Array.isArray(maybeAccessor)) {
      accessor = (data: any) => get(data, maybeAccessor);
    } else if (typeof maybeAccessor === 'string' || typeof maybeAccessor === 'function') {
      accessor = maybeAccessor;
    } else {
      throw new Error(
        'if either column.accessor or column.field are present, ' +
        'column.accessor needs to be either a function, string or an array of strings ' +
        'or if accessor is not specified, column.field needs to be as string or an array of strings!');
    }
  }

  return {
    accessor,
    actions: column.actions,
    aggregate: column.aggregate,
    className: column.className,
    constructUrl: column.constructUrl,
    dateTimeFormat: column.dateTimeFormat,
    disabled: column.disabled,
    download: column.download,
    field: getColumnId(column),
    fieldProps: column.fieldProps,
    formatter: column.formatter != null ? column.formatter : getDefaultFormatter(column),
    isExternal: column.isExternal,
    label: column.footerLabel,
    onBlur: column.onBlur,
    onChange: column.onChange,
    onClick: column.onClick,
    openInNewTab: column.openInNewTab,
    sectionName: column.sectionName,
    statusDefinition: column.statusDefinition,
    style: column.style,
    url: column.url,
  };
}

export function transformConfig<T>(columns: IRowConfig<T>, items?: T[]): ReactTableColumnConfig {
  return columns.filter((c) => {
    if (typeof c.isVisible === 'function') {
      return c.isVisible() !== false;
    } else {
      return c.isVisible !== false;
    }
  }).map((column) => {
    const alignment = column.alignment != null ? column.alignment : getDefaultAlignment(column);
    const cssClass = getCssClassFromAlignment(alignment);

    const Header: any = column.Header ? column.Header : column.title;
    const Cell: any = column.Cell || getCellComponent(column); // assume default cell type is text
    const Footer: any = column.Footer || getFooterComponent(column);

    const cellProps = getCellProps(column);

    return {
      Cell: Cell ? (props: any) => <Cell { ...props } { ...cellProps } /> : null,
      Footer: Footer && items && items.length > 0 ? (props: any) => <Footer { ...props } { ...cellProps } Cell={Cell}/> : null,
      Header,
      accessor: cellProps.accessor,
      aggregate: column.aggregate,
      className: cssClass,
      filterMethod: column.filterMethod || defaultFilterMethod,
      filterable: column.filterable,
      headerClassName: cssClass,
      id: cellProps.field,
      maxWidth: column.maxWidth,
      minWidth: column.minWidth || DEFAULT_MIN_COLUMN_WIDTH,
      sortable: column.sortable,
    };
  });
}

/**
 * Merges two table configurations by field name.
 *
 * The first configuration needs to be a complete column configuration, while the second one may omit the
 * normally mandatory `title` field. Note that it is the developer's responsibility to ensure that any added
 * field have the `title` field set.
 *
 * The second config overwrites any fields in the first if they have the same field name and the same key.
 * If there are cells in the second config that are not present in the first, they will be appended.
 *
 * Every non-addressed (unbound) column is considered a new column and will not be merged.
 *
 * Note: Not very efficient, can be rewritten to use _.groupBy and lookups, but that's probably a micro-optimisation
 * that's not needed.
 * @param configA Table configuration for row of a given type
 * @param configB Partial table configuration for row of a given type
 */
export function mergeConfigs<T>(configA: IRowConfig<T>, configB: Array<Omit<IColumnConfig<T>, 'title'>>): IRowConfig<T> {
  const merged: IRowConfig<T> = [];
  const fieldsForA = configA.map((cellConfig) => cellConfig.field);
  const fieldsForB = configB.map((cellConfig) => cellConfig.field);
  const addedFields = fieldsForB.filter((field) => field == null || !fieldsForA.includes(field));

  for (const field of fieldsForA) {
    const cellConfigA: IColumnConfig<T> | undefined = configA.find((cellConfig) => cellConfig.field === field);
    const cellConfigB: Omit<IColumnConfig<T>, 'title'> | undefined = configB.find((cellConfig) => cellConfig.field === field);
    const mergedCellConfig = field != null && cellConfigB != null ? {...cellConfigA, ...cellConfigB} as IColumnConfig<T> : cellConfigA;
    merged.push(mergedCellConfig!);
  }

  for (const field of addedFields) {
    merged.push(configB.find((cellConfig) => cellConfig.field === field) as IColumnConfig<T>);
  }

  return merged;
}
