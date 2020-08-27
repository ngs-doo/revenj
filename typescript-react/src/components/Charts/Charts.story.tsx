import * as React from 'react';

import { FunctionalUtils } from '@oradian/core';
import { storiesOf } from '@storybook/react';

import {
  PieChart,
  RichChart,
} from './Charts';
import { DimensionType } from './types';

interface IRow {
  bracket: string;
  name: string;
  sum: string;
  counter: number;
}

const names = [
  'Bear',
  'Bunny',
  'Cat',
  'Turtle',
];

export const mockData: IRow[] = FunctionalUtils.range(1, 10).map((it) => ({
  bracket: `Category ${it % 5}`,
  counter: 5 * (it % 4),
  name: names[it % 4],
  sum: `${10 * (it % 5)}.5`,
}));

const Container = ({ children }) => (
  <main style={{ padding: '40px', width: '100%', display: 'flex', 'justify-content': 'center' }}>
    {children}
  </main>
)

storiesOf('Charts', module)
  .add('Pie Chart', () => (
    <Container>
      <PieChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        y='counter'
        title='Pie Chart'
      />
    </Container>
  ))
  .add('Doughnut Chart', () => (
    <Container>
      <PieChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        y='counter'
        title='Doughnut Chart'
        isDoughnut
      />
    </Container>
  ))
  .add('Bar Chart', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter' }]}
        title='Bar Chart'
      />
    </Container>
  ))
  .add('Multiple Bar Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count' }, { y: 'sum', label: 'Sum' }]}
        title='Bar Chart'
      />
    </Container>
  ))
  .add('Stacked Bar Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count' }, { y: 'sum', label: 'Sum' }]}
        title='Bar Chart'
        stacked
      />
    </Container>
  ))
  .add('Stacked Area Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count', type: DimensionType.Area }, { y: 'sum', label: 'Sum', type: DimensionType.Area }]}
        title='Bar Chart'
        stacked
      />
    </Container>
  ))
  .add('Bar + line Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count' }, { y: 'sum', label: 'Sum', type: DimensionType.Line }]}
        title='Bar Chart'
      />
    </Container>
  ))
  .add('Multi-line Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count', type: DimensionType.Line }, { y: 'sum', label: 'Sum', type: DimensionType.Line }]}
        title='Bar Chart'
      />
    </Container>
  ))
  .add('Horizontal Bar Charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count', type: DimensionType.Bar }]}
        title='Bar Chart'
        horizontal
      />
    </Container>
  ))
  .add('Area, bar, and line charts', () => (
    <Container>
      <RichChart<IRow>
        items={mockData}
        xs={['bracket', 'name']}
        ys={[{ y: 'counter', label: 'Count' }, { y: 'counter', label: 'Count', type: DimensionType.Area }, { y: 'sum', label: 'Sum', type: DimensionType.Line }]}
        title='Bar Chart'
      />
    </Container>
  ));
