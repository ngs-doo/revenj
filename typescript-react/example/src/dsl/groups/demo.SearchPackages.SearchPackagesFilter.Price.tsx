// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';

export interface IGroupdemoSearchPackagesFilterPrice {
}


export class GroupdemoSearchPackagesFilterPrice extends React.PureComponent<IGroupdemoSearchPackagesFilterPrice> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  private minPriceFieldName: any = 'minPrice';
  private minPriceValidations: any = [];
  private maxPriceFieldName: any = 'maxPrice';
  private maxPriceValidations: any = [];
  public render() {
	return (
	  <Group title='Price' {...this.props}>
		<this.context.Fields.Currency
		  name={this.minPriceFieldName}
          className='dslField'
		  label={`From ($)`}
		  required={false}
		  validate={this.minPriceValidations}
		/>
		<this.context.Fields.Currency
		  name={this.maxPriceFieldName}
          className='dslField'
		  label={`To ($)`}
		  required={false}
		  validate={this.maxPriceValidations}
		/>
	  </Group>
	);
  }
}
