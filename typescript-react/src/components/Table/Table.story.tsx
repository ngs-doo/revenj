import { storiesOf } from '@storybook/react';
import BigNumber from 'bignumber.js';
import * as React from 'react';

import { CellType, ColumnAlignment, FooterType, IRowConfig, Table } from './Table';
import { RowState } from './interfaces';

import data from './__fixture__/combinedSheet.fixture.json';

interface IClientDetails {
  clientId: number;
  clientExternalId: string;
  clientType: string;
  clientDisplayName: string;
}

interface ILoanAccountItem {
  productId: number;
  productExternalId: string;
  accountId: number;
  accountExternalId: string;
  total: string;
  fee: string;
  overdue: string;
  penalty: string;
  olbPrincipal: string;
  canBackdate: boolean;
  hasPaymentOnDate: boolean;
}

interface IDepositAccount {
  productId: number;
  productExternalId: string;
  accountId: number;
  accountExternalId: string;
  subscribed: string;
  overdue: string;
  total: string;
  balance: string;
  canBackdate: boolean;
  hasPaymentOnDate: boolean;
}

interface ICombinedSheetItem {
  bla: number;
  clientDetails: IClientDetails;
  loanAccounts: ILoanAccountItem[];
  depositAccounts: IDepositAccount[];
}

const items: ICombinedSheetItem[] = data.items as unknown as ICombinedSheetItem[];

const columns: IRowConfig<ICombinedSheetItem> = [
  {
    alignment: ColumnAlignment.Left,
    field: ['clientDetails', 'clientDisplayName'],
    footerLabel: 'Totals',
    footerType: FooterType.Label,
    minWidth: 165,
    sortable: true,
    title: 'Client name',
    weight: 3,
  },
  {
    accessor: (d) => d.clientDetails.clientType,
    field: 'clientType',
    formatter: (a) => `bla ${a}`,
    title: 'Client Type',
  },
  {
    accessor: (d) => d.clientDetails.clientExternalId,
    actions: [
      {
        onClick: (event, original, row) => console.log(event, original, row),
        title: 'Bingo',
      },
      {
        onClick: (event, original, row) => console.log(event, original, row),
        title: 'Bongo',
      },
    ],
    cellType: CellType.Actions,
    minWidth: 200,
    title: 'Client ID',
  },
  {
    accessor: () => '2018-11-06T13:32:23',
    cellType: CellType.DateTime,
    field: 'ups2',
    title: 'Bla2',
  },
  {
    accessor: (d) => d.depositAccounts.reduce(
      (total, acc) => total.add(new BigNumber(acc.overdue)),
      new BigNumber(0),
    ),
    alignment: ColumnAlignment.Right,
    cellType: CellType.Currency,
    field: 'totalOverdue',
    minWidth: 100,
    title: 'Total Overdue($)',
    weight: 3,
  },
  {
    accessor: (d) => d.depositAccounts.reduce(
      (total, acc) => total.add(new BigNumber(acc.subscribed)),
      new BigNumber(0),
    ),
    aggregate: (values) => values.reduce((total, val) => total.add(val), new BigNumber(0)),
    alignment: ColumnAlignment.Right,
    cellType: CellType.Currency,
    field: 'totalExpected',
    footerType: FooterType.Aggregate,
    title: 'Total Expected($)',
  },
  {
    accessor: (d) => d.depositAccounts.reduce(
      (total, acc) => total.add(new BigNumber(acc.total)),
      new BigNumber(0),
    ).toNumber(),
    alignment: ColumnAlignment.Right,
    cellType: CellType.Currency,
    field: 'totalPayment',
    title: 'Total Payment($)',
  },
];

class TypedTable extends Table<ICombinedSheetItem> {}

storiesOf('Table', module)
  .add('Basic', () => (
    <div style={{ padding: 40, }}>
      <TypedTable
        minWidth={320}
        getRowState={(row) => row.index % 8
          ? RowState.Default
          : RowState.Error
        }
        columns={columns}
        data={items}
        foldableColumns
      />
    </div>
  ));
