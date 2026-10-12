import * as React from 'react';
import { render } from '@testing-library/react';

import { CellType, IRowConfig } from '../../Table/Table';
import { IRowInfo, RowState } from '../../Table/interfaces';
import { GridComponent } from '../Grid';

// Node removed `util.isObject`; Grid relies on the bundler's polyfill for it
jest.mock('util', () => ({
  ...jest.requireActual('util'),
  isObject: (value: any) => value !== null && typeof value === 'object',
}));

interface ITestRow {
  name: string;
  cancelled: boolean;
}

const definition: IRowConfig<ITestRow> = [
  { cellType: CellType.Text, field: 'name', title: 'Name' },
] as IRowConfig<ITestRow>;

const items: ITestRow[] = [
  { name: 'first', cancelled: false },
  { name: 'second', cancelled: true },
];

const getRowGroups = (container: HTMLElement) =>
  Array.from(container.querySelectorAll('.rt-tbody .rt-tr-group'));

describe('GridComponent', () => {
  it('applies getRowState to each row', () => {
    const getRowState = jest.fn((row: IRowInfo<ITestRow>) =>
      row.original.cancelled ? RowState.CrossedOut : RowState.Default);

    const { container } = render(
      <GridComponent definition={definition} items={items} getRowState={getRowState} />,
    );

    const rows = getRowGroups(container);
    expect(rows[0].classList).toContain(RowState.Default);
    expect(rows[0].classList).not.toContain(RowState.CrossedOut);
    expect(rows[1].classList).toContain(RowState.CrossedOut);
    expect(getRowState).toHaveBeenCalled();
  });

  it('renders rows without state classes when getRowState is not provided', () => {
    const { container } = render(
      <GridComponent definition={definition} items={items} />,
    );

    getRowGroups(container).forEach((row) => {
      Object.values(RowState).forEach((state) => {
        expect(row.classList).not.toContain(state);
      });
    });
  });
});
