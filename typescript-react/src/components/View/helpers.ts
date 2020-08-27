import { ColumnType, TypescriptResultSet } from '../../ResultSet/ResultSet';
import { isObject } from '../../util/FunctionalUtils/FunctionalUtils';
import {
  CellType,
  FooterType,
  IRowsConfig,
  IColumnConfig,
} from '../Table/Table';

export const anyFieldMatchesQuery = <T>(item: T, query: string): boolean =>
  Object.values(item).some((value) =>
    isObject(value) || Array.isArray(value)
      ? anyFieldMatchesQuery(value, query)
      : String(value).toLocaleLowerCase().includes(query),
  );

export const fastFilter = <T>(
  items: T[] | undefined,
  displayItems: IObjectAny[],
  query?: string,
): T[] | undefined => {
  if (
    items == null ||
    query == null ||
    query.trim() === '' ||
    !Array.isArray(items)
  ) {
    return items;
  }

  const lowerCaseQuery = query.toLocaleLowerCase();

  return items.filter(
    (item, index) =>
      anyFieldMatchesQuery(displayItems[index], lowerCaseQuery) ||
      anyFieldMatchesQuery(item, lowerCaseQuery),
  );
};

export const mergeDefinition = <T extends any[]>(
  conf: IRowsConfig<T>,
  footerLabel?: string,
): IRowsConfig<T> => {
  if (footerLabel == null || conf.length < 1) {
    return conf;
  }

  return [
    {
      ...conf[0],
      footerLabel,
      footerType: FooterType.Label,
    },
    ...conf.slice(1),
  ];
};

export const localizeDefinition = <T extends any[]>(
  conf: IRowsConfig<T>,
  formatMessage: (key: string) => string,
) => {
  if (!formatMessage) {
    return conf;
  }

  return conf.map((row: IColumnConfig<any>) => {
    // TODO change to "has localization path" once that field is added to dsl and add default message
    if (row.title.split('.')[0] === 'general') {
      row.title = formatMessage(row.title);
    }
    return row;
  });
};

export const getResultSetColumnDefinitions = (
  rs: TypescriptResultSet,
): IRowsConfig<any> => {
  return rs.columns.map((column, index) => {
    const getType = () => {
      const colType = rs.types[index];
      switch (colType) {
        case ColumnType.Boolean:
          return CellType.Boolean;
        case ColumnType.Date:
          return CellType.Date;
        case ColumnType.Decimal:
          return CellType.Decimal;
        case ColumnType.Float:
        case ColumnType.Int:
        case ColumnType.Long:
        case ColumnType.Short:
          return CellType.Number;
        case ColumnType.Timestamp:
          return CellType.DateTime;
        case ColumnType.Url:
          return CellType.Link;
        case ColumnType.String:
        case ColumnType.Unknown:
        case ColumnType.XML:
        case ColumnType.Blob:
        case ColumnType.Array:
        case ColumnType.Binary:
        case ColumnType.Time:
        case ColumnType.SQL:
        case ColumnType.Null:
        default:
          return CellType.Text;
      }
    };

    const getFormatter = () => {
      const colType = rs.types[index];
      switch (colType) {
        case ColumnType.Array:
          return (it: any) =>
            Array.isArray(it) ? it.map(String).join(', ') : String(it);
        case ColumnType.Null:
          return () => '—';
        default:
          return;
      }
    };

    const getUrlConstructor = () => {
      const colType = rs.types[index];
      switch (colType) {
        case ColumnType.Url:
          return (it: string) => it;
        default:
          return;
      }
    };

    return {
      cellType: getType(),
      constructUrl: getUrlConstructor(),
      field: [column],
      formatter: getFormatter(),
      title: column,
    };
  });
};

export const unpackRow = (row: IObjectAny | Map<string, any>): IObjectAny => {
  if (row instanceof Map) {
    return Array.from(row.entries()).reduce((acc, [key, value]) => {
      acc[key] = value;
      return acc;
    }, {});
  } else {
    return row;
  }
};
