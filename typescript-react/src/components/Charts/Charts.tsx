import Chart from 'chart.js';
import classNames from 'classnames';
import * as React from 'react';

import { CurrencyFormatter } from '../../util/Formatters/CurrencyFormatter';
import * as FunctionalUtils from '../../util/FunctionalUtils/FunctionalUtils';
import * as SortUtils from '../../util/SortUtils/SortUtils';
import { ListPresenterContext } from '../Form/Context';
import { I18nContext } from '../I18n/I18n';
import styles from './Charts.module.css';
import { getLabel, getTopNLabels, parseNumber } from './helpers';
import { colors } from './Theme';
import {
  ChartDefinition,
  ChartType,
  DimensionType,
  IChartConfiguration,
  IChartDataSets
} from './types';

const MAX_PIE_GROUPS: number = 10;
const OTHER_LABEL: string = 'Other';
const PRIMARY_Y_AXIS_ID = 'primary';
const SECONDARY_Y_AXIS_ID = 'secondary';

export interface IChartJS {
  options: IChartConfiguration;
  width?: number;
  height?: number;
  className?: string;
}

export class ChartJS extends React.PureComponent<IChartJS> {
  private canvasRef: React.RefObject<HTMLCanvasElement> = React.createRef<HTMLCanvasElement>();
  private chart?: Chart;

  public componentDidMount() {
    this.prepareChart();
  }

  public componentDidUpdate(prevProps: IChartJS) {
    if (this.props.options !== prevProps.options) {
      this.prepareChart();
    }
  }

  public render() {
    const { className, width, height } = this.props;

    return (
      <div className={classNames(styles.Container, className)}>
        <canvas
          ref={this.canvasRef}
          width={width}
          height={height}
        />
      </div>
    );
  }

  private getOptions = (): IChartConfiguration => {
    return {
      ...this.props.options,
      options: {
        maintainAspectRatio: true,
        responsive: true,
        ...this.props.options.options,
      },
    };
  }

  private prepareChart = () => {
    if (this.canvasRef == null || this.canvasRef.current == null) {
      return;
    }

    if (this.chart != null) {
      this.chart.destroy();
    }

    const ctx = this.canvasRef.current.getContext('2d');
    this.chart = new Chart(ctx!, this.getOptions());
  }
}

interface IPieChart<T> {
  className?: string;
  width?: number;
  height?: number;
  title?: string;
  isDoughnut?: boolean;
  items: T[];
  xs: Array<keyof T>;
  y: keyof T;
  options?: IChartConfiguration;
  datasetOptions?: IChartDataSets;
}

export class PieChart<T> extends React.PureComponent<IPieChart<T>> {
  public render() {
    const { className, width, height } = this.props;

    return (
      <ChartJS
        className={className}
        width={width}
        height={height}
        options={this.getOptions()}
      />
    );
  }

  private getOptions = (): IChartConfiguration => {
    const { datasetOptions, isDoughnut, title, options } = this.props;
    const type = isDoughnut ? 'doughnut' : 'pie';
    const dataGroups = this.getDataGroups();
    const data = {
      datasets: [{
        backgroundColor: colors,
        data: dataGroups.map(FunctionalUtils.snd),
        ...datasetOptions,
      }],
      labels: dataGroups.map(FunctionalUtils.fst) as string[],
    };

    return {
      data,
      options: {
        title: {
          display: true,
          text: title || '',
        },
        tooltips: {
          callbacks: {
            label: (item, data) => {
              const label = data.labels![item.index!]!;
              const value = CurrencyFormatter.formatNumber(String(data.datasets![item.datasetIndex!]!.data![item.index!]!));
              return ` ${label}: ${value}`;
            },
          },
        },
        ...options,
      },
      type,
    };
  }

  private getDataGroups = (): Array<[string, number]> => {
    const { items, xs, y } = this.props;
    const groups: { [key: string]: number } = {};
    const topLabels = new Set(getTopNLabels(items, xs, y, MAX_PIE_GROUPS));

    for (const item of items) {
      const label = getLabel(item, xs);
      const key = topLabels.has(label) ? label : OTHER_LABEL;
      const value = parseNumber(String(item[y]));

      groups[key] = (groups[key] || 0) + value;
    }

    return SortUtils.sortBy(
        (it) => it === OTHER_LABEL ? 1 : 0, // We want other to always come last
        Object.keys(groups),
      ).map((key) => {
      return [key, groups[key]] as [string, number];
    });
  }
}

interface IDataGroup<T> {
  y: keyof T;
  label?: string;
  type?: DimensionType;
  stack?: string;
  secondaryYAxis?: boolean;
}

export interface IRichChart<T> {
  className?: string;
  width?: number;
  height?: number;
  title?: string;
  stacked?: boolean;
  horizontal?: boolean;
  items: T[];
  xs: Array<keyof T>;
  ys: Array<IDataGroup<T>>;
  options?: IChartConfiguration;
  datasetOptions?: IChartDataSets;
  secondaryYAxis?: boolean;
}

export class RichChart<T> extends React.PureComponent<IRichChart<T>> {
  public render() {
    const { className, width, height } = this.props;

    return (
      <ChartJS
        className={className}
        width={width}
        height={height}
        options={this.getOptions()}
      />
    );
  }

  private getOptions = (): IChartConfiguration => {
    const { horizontal, stacked, title, options, ys } = this.props;
    const type = horizontal ? 'horizontalBar' : 'bar';
    const data = {
      datasets: this.getDatasets(),
      labels: this.getLabels(),
    };

    const ticks = {
      beginFromZero: data.datasets.every((it: any) => it.data!.every((it: any) => it >= 0)),
      callback: (value: any) => CurrencyFormatter.formatNumber(value),
      suggestedMin: 0,
    };

    const primaryYAxis = {
      id: PRIMARY_Y_AXIS_ID,
      offset: ys[0]?.type === 'bar',
      stacked,
      ticks: horizontal ? {} : ticks,
      position: 'left',
    };

    const secondaryYAxis = {
      id: SECONDARY_Y_AXIS_ID,
      offset: ys[1]?.type === 'bar',
      stacked,
      ticks: horizontal ? {} : ticks,
      position: 'right',
    };

    const yAxes = ys.find((y) => y.secondaryYAxis) ? [primaryYAxis, secondaryYAxis] : [primaryYAxis];

    return {
      data,
      options: {
        scales: {
          // Depending on the main axis, the value axis will always be numeric
          xAxes: [{
            stacked,
            ticks: horizontal ? ticks : {},
          }],
          yAxes,
        },
        title: {
          display: true,
          text: title || '',
        },
        tooltips: {
          callbacks: {
            label: (item, data) => {
              const label = data.datasets![item.datasetIndex!].label || '';
              const value = CurrencyFormatter.formatNumber(item.value!);
              return label
                ? `${label}: ${value}`
                : value;
            },
          },
        },
        ...options,
      },
      type,
    };
  }

  private getDatasets = (): IChartDataSets[] => {
    const { items, ys, horizontal, stacked, datasetOptions } = this.props;
    return ys.map((yConf, index): IChartDataSets => {
      const label = yConf.label
        ? yConf.label
        : String(yConf.y);
      const type = yConf.type != null && !horizontal
        ? (yConf.type === 'bar' ? yConf.type : 'line')
        : undefined;
      const color = datasetOptions?.backgroundColor
        ? datasetOptions.backgroundColor[index]
        : colors[index];
      const yAxisID = yConf.secondaryYAxis ? SECONDARY_Y_AXIS_ID : PRIMARY_Y_AXIS_ID;

      return {
        backgroundColor: color,
        borderColor: yConf.type === 'line' ? color : undefined,
        data: items.map((item) => parseNumber(String(item[yConf.y]))),
        fill: yConf.type === 'area',
        label,
        order: yConf.type === 'line' ? 1 : 2,
        stack: stacked ? (yConf.stack || 'stack0') : undefined,
        type,
        yAxisID,
        ...FunctionalUtils.omit(datasetOptions || {}, 'backgroundColor'),
      };
    });
  }

  private getLabels = (): string[] => {
    const { items, xs } = this.props;

    return items.map((item) => {
      const parts = [];
      for (const x of xs) {
        if (item[x] == null) {
          parts.push('—');
        } else {
          parts.push(String(item[x]));
        }
      }

      return parts.join(', ');
    });
  }
}

interface IChartFromDefinition<T> {
  className?: string;
  items: T[];
  definition: ChartDefinition<T>;
  options?: IChartConfiguration;
  datasetOptions?: IChartDataSets;
}

export class ChartFromDefinition<T> extends React.PureComponent<IChartFromDefinition<T>> {
  public static contextType = I18nContext;
  public context!: React.ContextType<typeof I18nContext>;

  public render() {
    const { className, datasetOptions, items, definition, options } = this.props;

    if (definition.type === ChartType.Rich) {
      return (
        <RichChart<T>
          className={className}
          items={items}
          xs={definition.xs}
          title={definition?.title}
          ys={definition.ys}
          horizontal={definition.horizontal}
          stacked={definition.stacked}
          options={options}
          datasetOptions={datasetOptions}
        />
      );
    } else {
      return (
        <PieChart<T>
          className={className}
          items={items}
          xs={definition.xs}
          title={definition?.title}
          y={definition.y}
          isDoughnut={definition.type === ChartType.Doughnut}
          options={options}
          datasetOptions={datasetOptions}
        />
      );
    }
  }
}

// NOTE: Temporary for DSL consumption
interface IDslChartPublicProps<T> {
  reportKey: string;
  x: keyof T & string;
  y?: keyof T & string;
  conceptName?: string;
  title?: string;
}

interface IDslChart<T> extends IDslChartPublicProps<T> {}

export class DslChart<T> extends React.PureComponent<IDslChart<T>> {
  public static contextType = ListPresenterContext;
  public static context: React.ContextType<typeof ListPresenterContext>;

  public render() {
    const { x, y, title } = this.props;
    const { items } = this.context;

    if (!items || items.length === 0) {
      return null;
    }

    return (
      <section className={styles.DslChartContainer}>
        <PieChart<T>
          title={title}
          items={items || []}
          isDoughnut
          xs={[x]}
          y={y!}
        />
      </section>
    );
  }
}
