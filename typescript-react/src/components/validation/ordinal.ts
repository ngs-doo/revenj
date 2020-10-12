import { Ord, ValidatorCreator} from './interfaces';
import * as operators from './operators';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Order validators                                                           //
////////////////////////////////////////////////////////////////////////////////
export interface IOrdValidatorCreatorFactory {
  (value: Ord): ValidatorCreator<Ord, Ord, any, any>;
}

export interface IOrdAlphaValidatorCreatorFactory {
  (value: string): ValidatorCreator<string, string, any, any>;
}

export const eqOrdCreator: IOrdValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<Ord, Ord, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base, _input) => `Value must be equal to ${base}.`,
    operator: operators.eq,
  });

export const gtOrdCreator: IOrdValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<Ord, Ord, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base, _input) => `Value must be greater than ${base}.`,
    operator: operators.gt,
  });

export const gteOrdCreator: IOrdValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<Ord, Ord, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be greater than or equal to ${base}`,
    operator: operators.gte,
  });

export const ltOrdCreator: IOrdValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<Ord, Ord, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be less than ${base}`,
    operator: operators.lt,
  });

export const lteOrdCreator: IOrdValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<Ord, Ord, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be less than or equal to ${base}`,
    operator: operators.lte,
  });

export const gtOrdAlphaCreator: IOrdAlphaValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<string, string, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be greater than ${base}.`,
    operator: operators.gtAlpha,
  });

export const gteOrdAlphaCreator: IOrdAlphaValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<string, string, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be greater than or equal to ${base}`,
    operator: operators.gteAlpha,
  });

export const ltOrdAlphaCreator: IOrdAlphaValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<string, string, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be less than ${base}`,
    operator: operators.ltAlpha,
  });

export const lteOrdAlphaCreator: IOrdAlphaValidatorCreatorFactory = (value) =>
  validatorCreatorFactory<string, string, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be less than or equal to ${base}`,
    operator: operators.lteAlpha,
  });

////////////////////////////////////////////////////////////////////////////////
// Range validators                                                           //
////////////////////////////////////////////////////////////////////////////////
interface IRange<T extends Ord> {
  min?: T;
  max?: T;
}

export interface IRangeValidatorCreatorFactory {
  (range: IRange<Ord>): ValidatorCreator<IRange<Ord>, Ord, any, any>;
}
export const inIntervalCreator: IRangeValidatorCreatorFactory = (range: IRange<Ord>) =>
  validatorCreatorFactory<IRange<Ord>, Ord, void, void>({
    getValidatorBaseValue: () => range,
    getValidatorErrorMessage: ({ min, max }) => `Value is not in valid range <${min}, ${max}>`,
    operator: (interval) => (input) =>
      interval.min == null || interval.max == null
        ? true
        : operators.gt(interval.min)(input) && operators.lt(interval.max)(input),
  });

export const inSegmentCreator: IRangeValidatorCreatorFactory = (range: IRange<Ord>) =>
  validatorCreatorFactory<IRange<Ord>, Ord, void, void>({
    getValidatorBaseValue: () => range,
    getValidatorErrorMessage: ({ min, max }) => `Value is not in valid range [${min}, ${max}]`,
    operator: (interval) => (input) => {
      return interval.min == null || interval.max == null
          ? true
          : operators.gte(interval.min)(input) && operators.lte(interval.max)(input);
    },
  });
