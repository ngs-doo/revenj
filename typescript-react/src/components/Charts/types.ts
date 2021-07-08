import Chart from 'chart.js';

export interface IChartConfiguration extends Chart.ChartConfiguration {};

export interface IChartOptions extends Chart.ChartOptions {};

export interface IChartDataSets extends Chart.ChartDataSets {};

export enum ChartType {
  Pie = 'Pie',
  Doughnut = 'Doughnut',
  Rich = 'Rich',
}

export enum DimensionType {
  Bar = 'bar',
  Line = 'line',
  Area = 'area',
}

interface IChartDefinitionCommonProps<T> {
  chartOnly?: boolean;
  title?: string;
  xs: Array<keyof T & string>;
}

export interface IPieLikeChartDefinition<T> extends IChartDefinitionCommonProps<T> {
  type: ChartType.Pie | ChartType.Doughnut;
  y: keyof T & string;
}

export interface IChartDimension<T> {
  type: DimensionType;
  y: keyof T & string;
  secondaryYAxis: boolean;
}

export interface IRichChartDefinition<T> extends IChartDefinitionCommonProps<T> {
  horizontal: boolean;
  stacked: boolean;
  type: ChartType.Rich;
  ys: Array<IChartDimension<T>>;
}

export type ChartDefinition<T> = IPieLikeChartDefinition<T> | IRichChartDefinition<T>;

export const serializeChartDefinition = <T>(chartDefinition: ChartDefinition<T>): Map<string, string> => {
  const result = new Map<string, string>();

  if (chartDefinition.title) {
    result.set('title', chartDefinition.title);
  }

  if (chartDefinition.chartOnly != null) {
    result.set('chartOnly', JSON.stringify(chartDefinition.chartOnly));
  }

  result.set('xs', JSON.stringify(chartDefinition.xs));
  result.set('type', chartDefinition.type);

  if (chartDefinition.type === ChartType.Rich) {
    result.set('ys', JSON.stringify(chartDefinition.ys));
    result.set('horizontal', JSON.stringify(Boolean(chartDefinition.horizontal)));
    result.set('stacked', JSON.stringify(Boolean(chartDefinition.stacked)));
  } else {
    result.set('y', chartDefinition.y);
  }

  return result;
};

export const deserializeChartDefinition = <T>(map: Map<string, string>): ChartDefinition<T> => {
  const type = map.get('type') as unknown as ChartType;
  const chartOnly = map.get('chartOnly') ? JSON.parse(map.get('chartOnly')!) : undefined;
  if (type === ChartType.Rich) {
    return {
      chartOnly,
      horizontal: JSON.parse(map.get('horizontal')!),
      stacked: JSON.parse(map.get('stacked')!),
      title: map.get('title'),
      type,
      xs: JSON.parse(map.get('xs')!),
      ys: JSON.parse(map.get('ys')!),
    };
  } else {
    return {
      chartOnly,
      title: map.get('title'),
      type,
      xs: JSON.parse(map.get('xs')!),
      y: map.get('y') as keyof T & string,
    };
  }
};
