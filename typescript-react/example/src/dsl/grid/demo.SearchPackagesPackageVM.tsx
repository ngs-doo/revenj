// tslint:disable ordered-imports object-literal-sort-keys
import * as React from 'react';
import { FieldRegistryContext } from 'revenj';

import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { FormType } from 'revenj';
import { CellType, IRowConfig } from 'revenj';
import { Grid, IGridPublicProps } from 'revenj';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';

export interface IGridPackageVM {
  canNavigateTo?: (formType: FormType, to?: any) => boolean;
  navigateTo?: (formType: FormType, id ?: any) => void;
  gridProps?: Partial<IGridPublicProps<demoIPackageVM>>;
  definition?: IRowConfig<demoIPackageVM>;
  getDefinition?: (props: any) => IRowConfig<demoIPackageVM>;
  footerLabel?: string;
  pagination?: boolean;
}

export const getDefinition = (_Fields: any): IRowConfig<demoIPackageVM> => [
  {
    cellType: CellType.Text,
    field: ['ID'],
    title: 'Tracking Identifer',
  },
  {
    cellType: CellType.Currency,
    field: ['price'],
    title: 'Price',
  },
  {
    cellType: CellType.Number,
    field: ['weight'],
    title: 'Weight (kg)',
  },
  {
    cellType: CellType.Text,
    field: ['status'],
    title: 'Package Status',
    formatter: (v: any) => v != null ? (demoPackageStatus.getMeta(v) != null ? demoPackageStatus.getMeta(v)!.description : v) : undefined,
  },
  {
    cellType: CellType.Text,
    field: ['description'],
    title: 'Notes',
  },
];

export default class GridIGridPackageVM extends React.PureComponent<IGridPackageVM> {
  public static conceptName: string = 'demo.PackageVM';
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;

  public render() {
    const { gridProps, getDefinition, ...rest } = this.props;
    const definition = this.getDefinition();

    return (
      <Grid<demoIPackageVM>
        {...rest}
        {...gridProps}
        definition={definition}
        fastSearch
      />
    );
  }

  private getDefinition = (): IRowConfig<demoIPackageVM> => {
    return this.props.getDefinition?.(this.props) ?? this.props.definition ?? getDefinition(this.context.Fields);
  }
}
