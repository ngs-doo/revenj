// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';

export interface IAddress {
  
  form?: string;
  nested?: boolean;
  vertical?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  optional?: boolean;
}


export class Address extends React.PureComponent<IAddress> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  private streetFieldName: any = 'street';
  private streetValidations: any = [];
  private zipCodeFieldName: any = 'zipCode';
  private zipCodeValidations: any = [];
  private cityFieldName: any = 'city';
  private cityValidations: any = [];
  private regionFieldName: any = 'region';
  private regionValidations: any = [];
  private countryFieldName: any = 'country';
  private countryValidations: any = [];
  public render() {
	return (
	  <Group {...this.props}>
		<this.context.Fields.ShortText
		  name={this.streetFieldName}
          className='dslField'
		  label={`Street and Number`}
		  required={true}
		  validate={this.streetValidations}
		/>
		<this.context.Fields.ShortText
		  name={this.zipCodeFieldName}
          className='dslField'
		  label={`Postal/Zip Code`}
		  required={true}
		  validate={this.zipCodeValidations}
		/>
		<this.context.Fields.ShortText
		  name={this.cityFieldName}
          className='dslField'
		  label={`City`}
		  required={true}
		  validate={this.cityValidations}
		/>
		<this.context.Fields.ShortText
		  name={this.regionFieldName}
          className='dslField'
		  label={`Region/State`}
		  required={true}
		  validate={this.regionValidations}
		/>
		<this.context.Fields.ShortText
		  name={this.countryFieldName}
          className='dslField'
		  label={`Country`}
		  required={true}
		  validate={this.countryValidations}
		/>
	  </Group>
	);
  }
}
