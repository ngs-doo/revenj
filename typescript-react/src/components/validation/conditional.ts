import {
  count,
  get,
  identity,
} from '../../util/FunctionalUtils/FunctionalUtils';
import {
  IFormFieldSpecification,
  IFormFieldSpecificationDescriptor,
  IFormFieldValue,
  ITransformer,
  Ord,
  Requireable,
  ValidateTrigger,
} from './interfaces';
import * as operators from './operators';
import { resolveRelativePath } from './utils';
import {
  validatorCreatorFactory,
  valueIsAbsent,
} from './validatorCreatorFactory';

export const gtAlphaFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<string, string, F, void>({
    getValidatorBaseValue: (formValues, _, fieldName) => get(formValues, resolveRelativePath<F>(right.path, fieldName)) as unknown as string,
    getValidatorErrorMessage: () => `${left.label} value must be greater than the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (base == null || input == null) {
        return true;
      } else {
        return operators.gtAlpha(base)(input);
      }
    },
  });

export const gteAlphaFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<string, string, F, void>({
    getValidatorBaseValue: (formValues, _, fieldName) => get(formValues, resolveRelativePath<F>(right.path, fieldName)) as unknown as string,
    getValidatorErrorMessage: () => `${left.label} value must greater than or equal to the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (base == null || input == null) {
        return true;
      } else {
        return operators.gteAlpha(base)(input);
      }
    },
  });

export const ltAlphaFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<string, string, F, void>({
    getValidatorBaseValue: (formValues, _, fieldName) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as unknown as string,
    getValidatorErrorMessage: () => `${left.label} value must be less than the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (base == null || input == null) {
        return true;
      } else {
        return operators.ltAlpha(base)(input);
      }
    },
  });

export const lteAlphaFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<string, string, F, void>({
    getValidatorBaseValue: (formValues, _, fieldName) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as unknown as string,
    getValidatorErrorMessage: () => `${left.label} value must be less than or equal to the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (base == null || input == null) {
        return true;
      } else {
        return operators.lteAlpha(base)(input);
      }
    },
  });

export const gtFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<Ord, Ord, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as Ord,
    getValidatorErrorMessage: () => `${left.label} value must be greater than the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (valueIsAbsent(base) || valueIsAbsent(input)) {
        return true;
      } else {
        return operators.gt(base)(input);
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

export const gteFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<Ord, Ord, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as Ord,
    getValidatorErrorMessage: () => `${left.label} value must be greater than or equal to the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (valueIsAbsent(base) || valueIsAbsent(input)) {
        return true;
      } else {
        return operators.gte(base)(input);
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

export const ltFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<Ord, Ord, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as Ord,
    getValidatorErrorMessage: () => `${left.label} value must be less than the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (valueIsAbsent(base) || valueIsAbsent(input)) {
        return true;
      } else {
        return operators.lt(base)(input);
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

export const lteFieldCreator = <F>(left: IFormFieldSpecificationDescriptor, right: IFormFieldSpecification) =>
  validatorCreatorFactory<Ord, Ord, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) as Ord,
    getValidatorErrorMessage: () => `${left.label} value must be less than or equal to the value of field ${right.label}`,
    operator: (base) => (input) => {
      if (valueIsAbsent(base) || valueIsAbsent(input)) {
        return true;
      } else {
        return operators.lte(base)(input);
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

export const cantBeNegativeIfCreator = <F>(field: IFormFieldSpecificationDescriptor, condition: IFormFieldSpecification) =>
  validatorCreatorFactory<Requireable, Ord, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath<F>(condition.path, fieldName)) as Requireable,
    getValidatorErrorMessage: () => `${field.label} value can't be negative when ${condition.label} is used`,
    operator: (base) => (input) => {
      if (valueIsAbsent(input) || !base) {
        return true;
      } else {
        return operators.lte(input)('0');
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

export const requiredIfSetCreator = <F>(field: IFormFieldSpecification) =>
  validatorCreatorFactory<Requireable, Requireable, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath<F>(field.path, fieldName)),
    getValidatorErrorMessage: () => `Required`,
    operator: (baseValue) => (inputValue) => {
      const isRequired = operators.required()(baseValue);
      if (isRequired) {
        return operators.required()(inputValue);
      } else {
        return true;
      }
    },
    validate: ValidateTrigger.IfBaseIsSet,
  });

export const requiredIfCheckedCreator = <F>(field: IFormFieldSpecification) =>
  validatorCreatorFactory<Requireable, Requireable, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath<F>(field.path, fieldName)),
    getValidatorErrorMessage: () => `Required`,
    operator: (baseValue) => (inputValue) => {
      const isRequired = baseValue === true;
      if (isRequired) {
        return operators.required()(inputValue);
      } else {
        return true;
      }
    },
    validate: ValidateTrigger.IfBaseIsSet,
  });

export const requireAtLeastOneOfCreator = <F>(fields: IFormFieldSpecification[]) =>
  validatorCreatorFactory<Array<IFormFieldValue<Requireable>>, void, F, void>({
    getValidatorBaseValue: (formValues, _, fieldName) =>
      fields.map((field) => ({ ...field, value: get(formValues, resolveRelativePath<F>(field.path, fieldName)) })),
    getValidatorErrorMessage: () => `At least one of: ${fields.map((field) => field.label).join(', ')} is required`,
    operator: (baseValue) => () => !baseValue.length || baseValue.some((field) => field.value),
    validate: ValidateTrigger.IfBaseIsSet,
  });

export const uniqueAmongstCreator = <F, V>(selectFields: (formData: F) => V[], transform: ITransformer<any, V> = identity) =>
  validatorCreatorFactory<V[], V, F, void>({
    getValidatorBaseValue: (formData) => {
      const values = selectFields(formData)
        .filter((x) => x != null)
        .map(transform);

      return values;
    },
    getValidatorErrorMessage: (base, input) =>
      `Input value '${input}' is not unique amongst ${base.map((opt) => `'${opt}'`).join(', ')}`,
    getValueFromInput: transform,
    operator: (baseValue) => (inputValue) => {
      if (!operators.required()(inputValue)) {
        return true;
      } else {
        return count(baseValue, inputValue) <= 1;
      }
    },
  });

export const checkedOneOfCreator = <F>(selectFields: (formData: F) => boolean[]) =>
  validatorCreatorFactory<boolean, void, F, void>({
    getValidatorBaseValue: (formData) => selectFields(formData).some((value) => value === true),
    getValidatorErrorMessage: () => 'At least one checkboxbox has to be checked',
    operator: (baseValue) => () => baseValue,
    validate: ValidateTrigger.IfBaseIsSet,
  });
