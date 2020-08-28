import classNames from 'classnames';
import * as React from 'react';
import { isObject } from 'util';

import {
  CellType,
  IColumnConfig,
  IRowsConfig,
  IRowConfig,
  Table,
} from '../Table/Table';
import { Loading } from '../Loader/Loader';
import { TypescriptResultSet } from '../../ResultSet/ResultSet';
import { get, set } from '../../util/FunctionalUtils/FunctionalUtils';
import { Internationalised, IWithI18n } from '../I18n/I18n';
import { ListPresenterComponent } from '../Form/Context';
import { FastFilter } from './FastFilter';
import {
  fastFilter,
  getResultSetColumnDefinitions,
  localizeDefinition,
  mergeDefinition,
  unpackRow,
} from './helpers';

import styles from './Grid.module.css';

export interface IGridPublicProps<Row> {
  className?: string;
  dynamic?: boolean;
  pagination?: boolean;
  fastSearch?: boolean;
  definition: IRowConfig<Row>;
  footerLabel?: string;
  maxResults?: number;
}

interface IGrid<Row> extends IGridPublicProps<Row>, IWithI18n {}

interface IGridComponentProps<Row> {
  items: Row[];
  page?: number;
  perPage?: number;
  pages?: number;
  totalCount?: number;
  onPaginationChange?: (page: number, perPage: number) => Promise<any>;
}

interface IGridComponent<Row> extends IGridPublicProps<Row>, IGridComponentProps<Row> {}

interface IGridComponentState<R> {
  items: R[];
  displayItems?: IObjectAny[]; // TODO: Maybe type this? Not that important, I guess
  query?: string;
}

export class GridComponent<Row> extends React.PureComponent<IGridComponent<Row>, IGridComponentState<Row>> {
  public state: IGridComponentState<Row> = {
    displayItems: undefined,
    items: [] as Row[],
    query: undefined,
  };

  public componentDidMount() {
    const items = this.props.items instanceof TypescriptResultSet ? this.props.items.toObjects() as Row[] : this.props.items;
    this.setState({
      displayItems: this.getDisplayItems(items),
      items,
    });
  }

  public componentDidUpdate(prevProps: IGridComponent<Row>) {
    if (prevProps.items !== this.props.items) {
      const items = this.props.items instanceof TypescriptResultSet ? this.props.items.toObjects() as Row[] : this.props.items;
      this.setState({
        displayItems: this.getDisplayItems(items),
        items,
      });
    }
  }

  public render() {
    const {
      className,
      fastSearch,
      maxResults,
      pagination,
      totalCount,
      page,
      perPage,
      pages,
    } = this.props;
    const { query } = this.state;

    return (
      <div className={classNames(styles.Grid, className)}>
        {
          fastSearch || maxResults ? (
            <div className={styles.Header}>
              <span className={styles.Max}>
                {
                  maxResults ? `Maximum number of results: ${maxResults}` : <span />
                }
              </span>
              {
                fastSearch ? (
                  <FastFilter
                    query={query}
                    onQueryChange={this.setQuery}
                  />
                ) : null
              }
            </div>
          ) : null
        }
        <Table
          columns={this.getDefinition()}
          data={this.getItems() ?? []}
          foldableColumns
          pagination={pagination}
          automaticPagination={this.automaticallyPaginate()}
          page={page}
          perPage={perPage}
          pages={pages}
          totalCount={totalCount}
          onPageChange={!this.automaticallyPaginate() ? this.onPageChange : undefined}
          onPageSizeChange={!this.automaticallyPaginate() ? this.onPerPageChange : undefined}
        />
      </div>
    );
  }

  private automaticallyPaginate = () => this.props.pagination && this.props.page == null && this.props.perPage == null;

  private onPageChange = (page: number) => this.props.onPaginationChange ? this.props.onPaginationChange(page, this.props.perPage!) : undefined;

  private onPerPageChange = (perPage: number) => {
    if (this.props.onPaginationChange) {
      const currentStartingPoint = this.props.perPage! * this.props.page!;
      const newPage = Math.floor(currentStartingPoint / perPage) as Int;
      this.props.onPaginationChange(newPage, perPage);
    }
  }

  private getItems = () => {
    return this.props.fastSearch && this.state.query
      ? fastFilter(this.state.items || [], this.state.displayItems || [], this.state.query)
      : this.state.items || [];
  }

  private getDisplayItems = (items: Row[]) => {
    const definition = this.getDefinition();

    return (items || []).map((item) => {
      const displayRow = {};

      definition.forEach((cell: IColumnConfig<Row>) => {
        if (cell.cellType === CellType.Actions) {
          return; // No real display value
        }

        const rawValue = get(item, cell.field! as DeepKeyOf<Row>);
        const value = cell.formatter != null ? cell.formatter!(rawValue, item) : rawValue;
        if (!isObject(value)) {
          set(displayRow, cell.field! as any, value);
        }
      });

      return displayRow;
    });
  }

  private setQuery = (query?: string) => {
    this.setState({ query });
  }

  private getDefinition = () => {
    const { definition, dynamic } = this.props;

    return dynamic ? this.getDynamicColumns(this.props.items) : definition;
  }

  private getDynamicColumns = (rows: any[] | TypescriptResultSet): IRowsConfig<any> => {
    if (rows instanceof TypescriptResultSet) {
      return getResultSetColumnDefinitions(rows);
    }

    if (rows == null || rows.length === 0) {
      return [];
    }

    const columns: Array<[string, CellType]> = [];
    const firstRow = unpackRow(rows[0]);
    Object.keys(firstRow).forEach((cell) => {
      columns.push([cell, this.inferColumnType(rows[0][cell])]);
    });

    return Array.from(columns).map(([column, cellType]) => ({
      cellType,
      field: [column],
      title: column,
    }));
  }

  /**
   * A very primitive inference mechanism for cells that come from a Map. In general, this should not be used (use a typed grid or a ResultSet instead),
   * but it can stay for now.
   */
  private inferColumnType = (value: any): CellType => {
    if (typeof value === 'boolean') {
      return CellType.Boolean;
    }
    if (typeof value === 'number') {
      return CellType.Number;
    }
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
      return CellType.Date;
    }
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T[\d:]+$/.test(value)) {
      return CellType.DateTime;
    }
    return CellType.Text;
  }
}

class GridBare<Row> extends React.PureComponent<IGrid<Row[]>> {
  public render() {
    const {
      maxResults,
      fastSearch,
      pagination,
      className,
      dynamic,
      footerLabel,
    } = this.props;
    return (
      <ListPresenterComponent>
        {
          (ctx) => (
            ctx!.isLoaded || !ctx!.isLoading ? (
              <GridComponent
                definition={this.getDefinition()}
                maxResults={maxResults}
                fastSearch={fastSearch}
                className={className}
                dynamic={dynamic}
                footerLabel={footerLabel}
                pagination={Boolean(pagination) || (ctx!.page != null && ctx!.perPage != null)}
                page={ctx!.page}
                perPage={ctx!.perPage}
                pages={ctx!.totalCount != null ? Math.ceil(ctx!.totalCount / ctx!.perPage!) : undefined}
                totalCount={ctx!.totalCount}
                onPaginationChange={ctx!.onChangePagination}
                items={this.transformItems(ctx!.items)}
              />
            ) : <Loading />
          )
        }
      </ListPresenterComponent>
    );
  }

  private getDefinition = () => {
    const { definition: originalDefinition, footerLabel, localize } = this.props;
    const definition = mergeDefinition(originalDefinition, footerLabel);

    return definition != null ? localizeDefinition(definition, localize) : definition;
  }

  private transformItems = (items: Row[]) => {
    const { dynamic } = this.props;
    if (dynamic && items != null) {
      if (items instanceof TypescriptResultSet) {
        return items;
      } else {
        return items.map(unpackRow);
      }
    } else {
      return items;
    }
  }
}

export class Grid<Row> extends React.PureComponent<IGridPublicProps<Row>> {
  public render() {
    return (
      <Internationalised>
        {
          ({ localize }) => (
            <GridBare {...this.props as IGridPublicProps<any>} localize={localize} />
          )
        }
      </Internationalised>
    );
  }
}
