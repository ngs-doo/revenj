// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { GroupdemoSearchPackagesFilterPrice } from '../groups/demo.SearchPackages.SearchPackagesFilter.Price';
import { GroupdemoSearchPackagesFilterWeight } from '../groups/demo.SearchPackages.SearchPackagesFilter.Weight';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';

export interface ISearchPackagesFilter {
  
  form?: string;
  nested?: boolean;
  vertical?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  optional?: boolean;
}


export class SearchPackagesFilter extends React.PureComponent<ISearchPackagesFilter> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  private statusesFieldName: any = 'statuses';
  private statusesValidations: any = [];
  public render() {
	return (
	  <Group {...this.props}>
        <GroupdemoSearchPackagesFilterPrice />
        <GroupdemoSearchPackagesFilterWeight />
		<this.context.Fields.EnumSelect
		  name={this.statusesFieldName}
          className='dslField'
		  label={`In Statuses`}
		  required={false}
		  validate={this.statusesValidations}
		  {...{enum:demoPackageStatus, multi: true}}
		/>
	  </Group>
	);
  }
}
