import { Numeric } from '../../util/NumberUtils/NumberUtils';

// Operator argument types
export type Requireable = any;
export type Ord = Numeric;
export type Num = Numeric;
export type Eq = any;

// Operator interfaces
export interface IOperator<B, I> {
  (baseValue: B): (inputValue: I) => boolean;
}
export interface IRequireableOperator extends IOperator<undefined, Requireable> {
  (): (inputValue: Requireable) => boolean;
}

export interface IOrdOperator extends IOperator<Ord, Ord> {
  (baseValue: Numeric): (inputValue: Numeric) => boolean;
}

export interface IOrdAlphaOperator extends IOperator<string, string> {}

export interface INumOperator extends IOperator<Ord, Ord> {
  (baseValue: Numeric): (inputValue: Numeric) => boolean;
}

export interface IEqOperator extends IOperator<any, any> {
  <T>(baseValue: T): (inputValue: T) => boolean;
}

export interface IGetValidatorBaseValue<B, F, P> {
  (allValues: F, componentProps: P, fieldName: string): B;
}

export interface IGetValidatorErrorMessage<B, I, F, P> {
  (validationBaseValue: B, inputValue: I, allValues: F, componentProps: P, fieldName: string): string;
}

export interface IParseValue<R, V> {
  (input: R): V;
}

export type ValidationError = string | undefined;
export type Validator<I, F, P> = (inputValue: I, allValues?: F, props?: P, name?: keyof F) => ValidationError;

export enum ValidateTrigger {
  Always = 'Always',
  IfInputIsSet = 'IfInputIsSet',
  IfBaseIsSet = 'IfBaseIsSet',
  IfInputAndBaseAreSet = 'IfInputAndBaseAreSet',
}

interface IValidatorCreatorProps<B, I, F, P> {
  validateOverride?: ValidateTrigger;
  getValidatorBaseValueOverride?: IGetValidatorBaseValue<B, F, P> | B;
  getValidatorErrorMessageOverride?: IGetValidatorErrorMessage<B, I, F, P> | string;
  getValueFromInputOverride?: IParseValue<any, I>;
  getValueFromBaseOverride?: IParseValue<any, B>;
}

export type ValidatorCreator<B, I, F, P> = (props?: IValidatorCreatorProps<B, I, F, P>) => Validator<I, F, P>;

export type Operator<B, I> = (baseValue: B) => (inputValue: I) => boolean;

export interface IFormFieldSpecification {
  label: string;
  path: string;
}

// In many cases, the path is not actually required
export interface IFormFieldSpecificationDescriptor extends Omit<IFormFieldSpecification, 'path'> {
  path?: IFormFieldSpecification['path'];
}

export interface IFormFieldValue<T> extends IFormFieldSpecification {
  value: T;
}

export interface ITransformer<V, R> {
  (value: V): R;
}

export type RegexOperator = (regex: RegExp) => (inputValue: string) => boolean;

export type Preprocessor<T> = (value: T) => T;
