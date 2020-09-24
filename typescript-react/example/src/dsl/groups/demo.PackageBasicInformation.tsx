// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';

export interface IGroupdemoPackageBasicInformation {
}


export class GroupdemoPackageBasicInformation extends React.PureComponent<IGroupdemoPackageBasicInformation> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  private priceFieldName: any = 'price';
  private priceValidations: any = [this.context.validators.isPositive, ];
  private weightFieldName: any = 'weight';
  private weightValidations: any = [this.context.validators.isPositive, this.context.validators.lessThan(500), ];
  private descriptionFieldName: any = 'description';
  private descriptionValidations: any = [];
  public render() {
	return (
	  <Group title='Basic Information' {...this.props}>
		<this.context.Fields.Currency
		  name={this.priceFieldName}
          className='dslField'
		  label={`Delivery price`}
		  required={true}
		  validate={this.priceValidations}
		/>
		<this.context.Fields.Decimal
		  name={this.weightFieldName}
          className='dslField'
		  label={`Weight (kg)`}
		  required={true}
		  validate={this.weightValidations}
		/>
		<this.context.Fields.LongText
		  name={this.descriptionFieldName}
          className='dslField'
		  label={`Description and Delivery Notes`}
		  required={false}
		  validate={this.descriptionValidations}
		/>
	  </Group>
	);
  }
}
