import classNames from 'classnames';
import * as React from 'react';
import ReactTable, { ComponentPropsGetterC, TableCellRenderer } from 'react-table';
import 'react-table/react-table.css';

import './Table.css';

import { throttle } from '../../util/AsyncUtils/AsyncUtils';
import { identity, isEqual } from '../../util/FunctionalUtils/FunctionalUtils';
import { DetailRow } from './DetailRow/DetailRow';
import { Expander } from './Expander';
import { Pagination, ZeroIndexedNumber } from './Pagination/Pagination';
import {
  getColumnId,
  transformConfig,
} from './config';
import {
  DEFAULT_COLUMN_WEIGHT,
  DEFAULT_EXPANDER_WIDTH,
  DEFAULT_MIN_COLUMN_WIDTH,
  DEFAULT_MIN_TABLE_WIDTH,
  DEFAULT_PAGE_SIZE,
  DEFAULT_RESIZE_HANDLE_THROTTLE,
} from './constants';
import {
  ColumnAlignment,
  IActionInfo,
  IAdditionalRowConfig,
  IColumnConfig,
  IGetRowState,
  IRowsConfig,
  IRowConfig,
  IRowInfo,
} from './interfaces';

import { CellType } from './Cell';
import { FooterType } from './Footer';
import { NoData } from './NoData/NoData';

export type { IActionInfo, IColumnConfig, IRowConfig, IRowsConfig, IAdditionalRowConfig };
export { CellType, FooterType };
export { ColumnAlignment };
export { mergeConfigs } from './config';

// tslint:disable:line interface-over-type-literal
type ExpandDescriptor = { [key: string]: string | ExpandDescriptor };

interface ITableState<T> {
  columns: IRowConfig<T>;
  foldedColumns: IRowConfig<T>;
  expanded: ExpandDescriptor;
  page?: number;
  perPage?: number;
}

interface ITableProps<T> {
  ExtraComponent?: React.ComponentType<T>;
  BeforePaginationComponent?: React.ComponentType<{}>;
  Expander?: TableCellRenderer;
  expanderWidth?: number;
  columns: IRowConfig<T>;
  data: T[];
  foldableColumns?: boolean;
  getTheadThProps?: ComponentPropsGetterC;
  minWidth?: number;
  pagination?: boolean;
  page?: number;
  pages?: number;
  perPage?: number;
  totalCount?: number;
  getRowState?: IGetRowState<T>;
  containerClass?: string;
  noDataContent?: string;
  automaticPagination?: boolean;
  onPageChange?(index: number): void;
  onPageSizeChange?(size: number): void;
}

export class Table<T> extends React.Component<ITableProps<T>, ITableState<T>> {
  public static defaultProps = {
    foldableColumns: false,
    minWidth: DEFAULT_MIN_TABLE_WIDTH,
    page: 1,
    pages: 1,
    pagination: false,
    perPage: 20,
  };

  state = {
    columns: this.props.columns,
    expanded: {},
    foldedColumns: [],
    page: this.props.page ? this.props.page - 1 : 0,
    perPage: this.props.perPage ? this.props.perPage : DEFAULT_PAGE_SIZE,
  };

  private divRef: HTMLDivElement | null = null;
  private onResize = throttle(() => this.calculateFoldedColumns(), DEFAULT_RESIZE_HANDLE_THROTTLE);

  private get rowCount(): number {
    return this.props.data ? this.props.data.length : 0;
  }

  private get pages(): number {
    return this.props.automaticPagination
      ? Math.max(1, Math.ceil(this.props.data.length / this.perPage))
      : this.props.pages!;
  }

  private get page(): number {
    if (this.props.automaticPagination) {
      return this.state.page;
    }

    if (this.props.pagination) {
      return this.props.page || 0; // react tables pages start from 0
    }

    return 0;
  }

  private get perPage(): number {
    if (this.props.automaticPagination) {
      return this.state.perPage;
    }

    if (this.props.pagination) {
      return this.props.perPage || DEFAULT_PAGE_SIZE;
    }

    return this.rowCount;
  }

  componentDidMount() {
    window.addEventListener('resize', this.onResize, false);
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.onResize);
  }

  componentWillReceiveProps(nextProps: ITableProps<T>) {
    let newState = {};
    if (
      (this.props.data !== nextProps.data) ||
      (this.state.columns !== nextProps.columns)
    ) {
      newState = {
        ...newState,
        columns: nextProps.columns,
        expanded: {},
      };
    }

    if (nextProps.automaticPagination) {
      if (this.props.page !== nextProps.page) {
        this.setState({ page: nextProps.page! - 1 || 0 });
      }

      if (this.props.perPage !== nextProps.perPage) {
        this.setState({ perPage: nextProps.perPage || DEFAULT_PAGE_SIZE });
      }

      if (this.props.data !== nextProps.data) {
        this.setState({ page: 0 });
      }
    }

    if (Object.keys(newState).length > 0) {
      const collapseExpanders = this.props.data !== nextProps.data;
      this.setState(newState, () => {
        this.calculateFoldedColumns(collapseExpanders);
      });
    }
  }

  public render() {
    const { expanded } = this.state;

    const data = this.props.data || [];
    const cssClasses = classNames(this.props.containerClass, 'TableContainer');

    return (
      <div ref={this.onTableDivRef} style={{ minWidth: this.props.minWidth, }} className={cssClasses}>
        <ReactTable
          NoDataComponent={this.constructNoDataComponent}
          LoadingComponent={() => null} /* circumvent the react-table always loading bug */
          data={data}
          showPagination={this.props.pagination}
          page={this.page}
          pageSize={this.perPage}
          pages={this.pages}
          onExpandedChange={this.onExpanded}
          onPageChange={this.onPageChange}
          onPageSizeChange={this.onPageSizeChange}
          onFilteredChange={this.onFilteredChange}
          expanded={expanded}
          resizable={false}
          expanderDefaults={{
            filterable: false,
            resizable: false,
            sortable: false,
            width: this.props.expanderWidth || DEFAULT_EXPANDER_WIDTH,
          }}
          columns={this.getColumnsConfig()}
          getTrGroupProps={this.getRowState as any}
          sortable={false}
          minRows={0}
          defaultPageSize={this.perPage}
          className='' // '-striped -highlight'
          SubComponent={this.getSubComponent() ?? undefined}
          manual={!this.props.automaticPagination}
          PaginationComponent={this.renderPagination}
          getTheadThProps={this.props.getTheadThProps}
        />
      </div>
    );
  }

  private renderPagination = (props: any) => {
    const { data, BeforePaginationComponent, totalCount } = this.props;
    return BeforePaginationComponent ? (
      <React.Fragment>
        { data && data.length > 0 ? <BeforePaginationComponent /> : null}
        <Pagination {...props} totalCount={totalCount} />
      </React.Fragment>
    ) : (
      <Pagination {...props} totalCount={totalCount} />
    );
  }

  // #TODO @bigd -> type me !!1!
  // Find a way to get internal table state typed and passed to first arg here
  private getRowState = (tableState: any, rowInfo: IRowInfo<T>) => {
    return {
      className: classNames(
        this.canExpand() && rowInfo && tableState.expanded[rowInfo.viewIndex] ? '-expanded' : '',
        this.props.getRowState && this.props.getRowState(rowInfo),
      ),
    };
  }

  private getSubComponent = () => {
    if (this.canExpand()) {
      return (d: IRowInfo<T>) => (
        <DetailRow
          data={d}
          foldedColumns={this.state.foldedColumns}
          ExtraComponent={this.props.ExtraComponent}
          hasFoldedColumns={this.props.foldableColumns}
          getRowState={this.props.getRowState}
        />
      );
    } else {
      return null;
    }
  }

  private canExpand = () => {
    return this.state.foldedColumns.length || this.props.ExtraComponent;
  }

  private onExpanded = (expanded: ExpandDescriptor) => {
    this.setState({ expanded });
  }

  private onPageChange = (index: ZeroIndexedNumber) => {
    const page = Math.max(0, index);
    if (this.props.onPageChange) {
      this.props.onPageChange(page);
    }

    if (this.props.automaticPagination) {
      this.setState({ page });
    }

    // clear the expanded flags index on page change
    this.setState({ expanded: {} });
  }

  private onPageSizeChange = (size: ZeroIndexedNumber) => {
    if (this.props.onPageSizeChange) {
      this.props.onPageSizeChange(size);
    }

    if (this.props.automaticPagination) {
      this.setState({ perPage: size, page: 0 });
    }
    // clear the expanded flags index on page size change
    this.setState({ expanded: {} });
  }

  private onFilteredChange = () => {
    // clear the expanded flags index on filtered change
    this.setState({ expanded: {} });
  }

  private calculateFoldedColumns = (collapseExpanders = false) => {
    if (this.divRef) {
      const { foldableColumns } = this.props;
      const width = this.divRef.offsetWidth;

      let breakpointWidth = DEFAULT_EXPANDER_WIDTH;
      const newFoldedColumns: IRowConfig<T> = foldableColumns
        ? this.state.columns
          .map(identity) // because js sort is stupid and inplace
          .sort((a: IColumnConfig<T>, b: IColumnConfig<T>) => (b.weight || DEFAULT_COLUMN_WEIGHT) - (a.weight || DEFAULT_COLUMN_WEIGHT))
          .reduce((foldedColumns: IRowConfig<T>, column: IColumnConfig<T>) => {
            breakpointWidth += (column.minWidth || DEFAULT_MIN_COLUMN_WIDTH);
            if (breakpointWidth > width && foldedColumns.length < (this.state.columns.length - 1)) {
              return [...foldedColumns, column];
            } else {
              return foldedColumns;
            }
          }, []) as IRowConfig<T>
        : [];

      if (!isEqual(this.state.foldedColumns.map(getColumnId), newFoldedColumns.map(getColumnId))) {
        this.setState({
          // collapse expanders if everything fits in the table and there is no extra else keep the state
          expanded: (this.canExpand() && !collapseExpanders) ? this.state.expanded : {},
          foldedColumns: newFoldedColumns,
        });
      }
    }
  }

  private onTableDivRef = (div: HTMLDivElement | null) => {
    this.divRef = div;
    if (div !== null) {
      this.calculateFoldedColumns();
    }
  }

  private getColumnsConfig() {
    const { columns, data } = this.props;
    const { foldedColumns } = this.state;

    const config = transformConfig(columns, data)
      .map((column) => ({
        ...column,
        show: !foldedColumns.map(getColumnId).includes(column.id),
      }));

    if (this.canExpand()) {
      return config.concat([{ Expander: this.props.Expander || Expander, expander: true }]);
    } else {
      return config;
    }
  }

  private constructNoDataComponent = () => {
    return <NoData />;
  }
}
