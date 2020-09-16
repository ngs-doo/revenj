import { mergeConfigs } from '../config';

import { CellType } from '../Cell';
import { IRowConfig } from '../interfaces';

interface IDummyRow {
  order: Int;
  name: string;
  age: Int;
}

const baseConfig: IRowConfig<IDummyRow> = [
  {
    field: 'order',
    title: 'Order',
  },
  {
    field: 'name',
    title: 'Name',
  },
];

describe('Table', () => {
  describe('config', () => {
    describe('Merge Column config', () => {
      it('should merge the given row configurations for all fields by field name', () => {
        const formatFn = jest.fn();
        const extraConfig = [
          {
            cellType: CellType.Text,
            field: 'order',
          },
          {
            field: 'name',
            formatter: formatFn,
          },
        ];

        const mergedConfig = mergeConfigs([...baseConfig], [...extraConfig]);
        expect(mergedConfig).toEqual([
          {
            cellType: CellType.Text,
            field: 'order',
            title: 'Order',
          },
          {
            field: 'name',
            formatter: formatFn,
            title: 'Name',
          },
        ]);
      });

      it('should preserve the cell order of the first config', () => {
        const extraConfig = [{ field: 'name'}, { field: 'order' }];
        const mergedConfig = mergeConfigs([...baseConfig], [...extraConfig]);
        const mergedKeysInOrder = mergedConfig.map((conf) => conf.field);
        expect(mergedKeysInOrder).toEqual(['order', 'name']);
      });

      it('should overwrite keys using the second config values, if specified in both', () => {
        const extraConfig = [
          {
            field: 'order',
            title: 'Better Order',
          },
        ];

        const mergedConfig = mergeConfigs([...baseConfig], [...extraConfig]);

        expect(mergedConfig).toEqual([
          {
            field: 'order',
            title: 'Better Order',
          },
          {
            field: 'name',
            title: 'Name',
          },
        ]);
      });

      it('should append additional fields to the end of the configuration', () => {
        const extraConfig = [
          {
            field: 'age',
            title: 'Age',
          },
        ];

        const mergedConfig = mergeConfigs([...baseConfig], [...extraConfig]);

        expect(mergedConfig).toEqual([
          {
            field: 'order',
            title: 'Order',
          },
          {
            field: 'name',
            title: 'Name',
          },
          {
            field: 'age',
            title: 'Age',
          },
        ]);
      });

      it('should consider each non-addressed (unbound) column a new column and not merge it', () => {
        const expandedBaseConfig = [...baseConfig, { title: 'Actions' }];
        const extraConfig = [{ title: 'More Actions' }];
        const mergedConfig = mergeConfigs([...expandedBaseConfig], [...(extraConfig as any[])]);

        expect(mergedConfig).toEqual([
          {
            field: 'order',
            title: 'Order',
          },
          {
            field: 'name',
            title: 'Name',
          },
          {
            title: 'Actions',
          },
          {
            title: 'More Actions',
          },
        ]);
      });
    });
  });
});
