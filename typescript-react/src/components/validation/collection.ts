import { removeSpaces } from '../../util/StringUtils/StringUtils';
import { validatorCreatorFactory } from './validatorCreatorFactory';

export const maxLengthCreator = (
  maxLength: number,
  operator?: (value: number) => (input: string | any[] | number) => boolean) =>
    // Number should _not_ be here, but it happens in real life and will have to stay until we have
    // typesafe forms across the board
    validatorCreatorFactory<number, string | any[] | number, any, any>({
      getValidatorBaseValue: () => maxLength,
      getValidatorErrorMessage: (value) => `Maximum length of field is ${value}`,
      operator: operator || ((value) => (input) =>
        value == null || (typeof input === 'number' ? String(input).length <= value : input.length <= value)),
    });

export const maxDecimalLengthCreator = (
  maxLength: number,
  operator?: (value: number) => (input: string | number) => boolean) =>
    validatorCreatorFactory<number, string | number, any, any>({
      getValidatorBaseValue: () => maxLength,
      getValidatorErrorMessage: (value) => `Maximum decimal length of field is ${value}`,
      operator: operator || ((value) => {
        const regex = new RegExp(`^-?[0-9]*\.[0-9]{0,${value}}$`);
        return (input) => regex.test(String(input));
    })});

export const exactLengthCreator = (
  expectedLength: number,
  operator?: (value: number) => (input: string | any[]) => boolean) =>
    validatorCreatorFactory<number, string | any[], any, any>({
      getValidatorBaseValue: () => expectedLength,
      getValidatorErrorMessage: (value) => `Required length of field is exactly ${value}`,
      operator: operator || ((value) => (input) => input == null || input.length === value),
    });

export const minLengthCreator = (minLength: number) =>
  validatorCreatorFactory<number, number | string, any, any>({
    getValidatorBaseValue: () => minLength,
    getValidatorErrorMessage: (value) => `Minimum length of field is ${value}`,
    operator: (minLength) => (input) =>
      input == null || (String(input).length >= minLength),
  });

export const maxLengthCreatorWithIgnoreByRegex = (maxLength: number, regex: RegExp) => maxLengthCreator(
    maxLength,
    (
      (value) => (input) =>
      value == null ||
      (typeof input === 'number'
        ? String(input).replace(regex, '').length <= value
        : Array.isArray(input)
          ? input.length <= value
          : input.replace(regex, '').length <= value
      )
    ),
  );

export const minQueryLengthCreator = (minLength: number = 3) =>
  validatorCreatorFactory<number, string, any, any>({
    getValidatorBaseValue: () => minLength,
    getValidatorErrorMessage: (value) => `Minimum query length is ${value}`,
    operator: (minLength) => (input) =>
      input == null || (removeSpaces(input).length >= minLength),
  });

export const isNotInCreator = (values: any[]) =>
  validatorCreatorFactory<any[], any, any, any>({
    getValidatorBaseValue: () => values,
    getValidatorErrorMessage: (value) => value.length > 1
      ? `Value must not be equal to any of: ${value.join(', ')}`
      : `Value must not be equal to ${value}`,
    operator: (values) => (input) => values.indexOf(input) === -1,
  });

export const isInCreator = (values: any[]) =>
  validatorCreatorFactory<any[], any, any, any>({
    getValidatorBaseValue: () => values,
    getValidatorErrorMessage: (value) => `Value must not be equal to ${value}`,
    operator: (values) => (input) => values.indexOf(input) !== -1,
  });
