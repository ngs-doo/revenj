import classNames from 'classnames';
import * as React from 'react';

import styles from './Pagination.module.css';

// Type as documentation :)
export type ZeroIndexedNumber = number;

export interface IButtonOption<T> {
  disabled?: boolean;
  label: string;
  value?: T;
  onClick?(): void;
}

export interface IButtonOptions<T> {
  options: Array<IButtonOption<T>>;
  value: T;
  className?: string;
}

export class ButtonOptions<T> extends React.PureComponent<IButtonOptions<T>> {
  render() {
    const { className, options, value } = this.props;

    return (
      <div className={classNames(styles.ButtonOptions, className)}>
        {
          options.map((option, index) => (
            <button
              key={index}
              type='button'
              disabled={option.disabled}
              className={classNames(
                styles.ButtonOption,
                {
                  [styles.ButtonOptionActive]: option.value === value,
                  [styles.ButtonOptionDisabled]: option.disabled,
                },
              )}
              onClick={option.onClick}
            >
              {option.label}
            </button>
          ))
        }
      </div>
    );
  }
}

// Shape shamelessly ripped off of the default implementation of react-table
export interface IPagination {
  data?: any[];
  manual?: boolean; // Is pagination manual
  page: ZeroIndexedNumber;
  pages: number;
  showPageSizeOptions?: boolean;
  pageSizeOptions: number[];
  pageSize: number;
  totalCount?: number;
  showPageJump?: boolean;
  canPrevious?: boolean;
  canNext?: boolean;
  nextText?: string;
  ofText?: string;
  rowsText?: string;
  className?: string;
  onPageChange(page: ZeroIndexedNumber): void;
  onPageSizeChange(page: number): void;
}

export interface IPaginationState {
  page: ZeroIndexedNumber;
}

export class Pagination extends React.Component<IPagination, IPaginationState> {
  static defaultProps: Partial<IPagination> = {
    ofText: 'of',
    showPageJump: true,
    showPageSizeOptions: true,
  };

  public state: IPaginationState = {
    page: 0,
  };

  public componentDidUpdate(prevProps: IPagination) {
    if (prevProps.page !== this.props.page) {
      this.setState({
        page: this.props.page,
      })
    }
  }

  public render() {
    const {
      data,
      manual,
      page,
      showPageSizeOptions,
      showPageJump,
      pageSize,
      className,
    } = this.props;

    if (!data || data.length === 0) {
      return null;
    }

    return (
      <div className={classNames(className, styles.Pagination)}>
        <div className={styles.PagesContainer}>
          {
            showPageJump ? (
              <ButtonOptions
                className={styles.Pages}
                options={this.getPages()}
                value={page}
              />
            ) : <div />
          }
          <div className={styles.Label}>
            {
              !manual ? (
                this.renderEntryStats() // Since we know exactly how many entries there are - it's FE pagination
              ) : (
                this.renderPageStats()
              )
            }
          </div>
        </div>

        {
          showPageSizeOptions ? (
            <ButtonOptions
              className={styles.PageSizes}
              options={this.getPageSizeOptions()}
              value={pageSize}
            />
          ) : <div />
        }
      </div>
    );
  }

  private renderPageStats = () => {
    const { data, page, pages, pageSize, ofText, totalCount } = this.props;
    const entriesCount = data?.length ?? 0;
    const firstEntry = page * pageSize + 1;
    const lastEntry = firstEntry + entriesCount - 1;
    const entries = `Entries ${firstEntry} to ${lastEntry} ${ofText} ${totalCount}`;

    return (
      <React.Fragment>
        <span>Showing page {page + 1} {ofText} {Math.max(1, pages)} pages</span>
        {
          totalCount ? (
            <div>{entries}</div>
          ) : null
        }
      </React.Fragment>
    );
  }

  private renderEntryStats = () => {
    const { page, pageSize, ofText, data } = this.props;
    const entriesCount = data?.length ?? 0;
    const howManyEntriesAreShown = Math.min(pageSize, entriesCount - page * pageSize);
    const firstEntry = page * pageSize + 1;
    const lastEntry = firstEntry + howManyEntriesAreShown - 1;

    return (
      <span>Showing {firstEntry} to {lastEntry} {ofText} {entriesCount} entries</span>
    );
  }

  private limitPage = (page: number) => {
    const actualPage = Number.isNaN(page) ? this.props.page : page;
    return Math.min(Math.max(actualPage, 0), this.props.pages - 1);
  }

  private setPage = (page: number) => {
    const limitedPage = this.limitPage(page);
    this.setState({ page: limitedPage });

    if (limitedPage !== this.props.page) {
      this.props.onPageChange(page);
    }
  }

  private onFirstClick = () => {
    if (this.props.page > 0) {
      this.setPage(0);
    }
  }

  private onLastClick = () => {
    if (this.props.page < this.props.pages - 1 && this.props.page >= 0) {
      this.setPage(this.props.pages - 1);
    }
  }

  private onPreviousClick = () => {
    if (this.props.canPrevious) {
      this.setPage(this.props.page - 1);
    }
  }

  private onNextClick = () => {
    if (this.props.canNext) {
      this.setPage(this.props.page + 1);
    }
  }

  private getPageButtonIndices = (): ZeroIndexedNumber[] => {
    const { page, pages } = this.props;
    const min = Math.max(page - 2, 0);
    const max = Math.min(page + 2, pages - 1);
    const numbers = [];

    for (let i = min; i <= max; i++) {
      numbers.push(i);
    }

    return numbers;
  }

  private getPages = (): Array<IButtonOption<ZeroIndexedNumber>> => {
    const { canNext, canPrevious } = this.props;
    const pageIndices = this.getPageButtonIndices();

    return [
      { label: '«', onClick: this.onFirstClick, disabled: !canPrevious },
      { label: '‹', onClick: this.onPreviousClick, disabled: !canPrevious },
      ...pageIndices.map((index) => ({
        label: String(index + 1),
        onClick: () => this.setPage(index),
        value: index,
      })),
      { label: '›', onClick: this.onNextClick, disabled: !canNext },
      { label: '»', onClick: this.onLastClick, disabled: !canNext },
    ];
  }

  private getPageSizeOptions = (): Array<IButtonOption<number>> => {
    const { pageSizeOptions, onPageSizeChange } = this.props;

    return pageSizeOptions.map((option) => ({
      label: String(option),
      onClick: () => onPageSizeChange(option),
      value: option,
    }));
  }
}
