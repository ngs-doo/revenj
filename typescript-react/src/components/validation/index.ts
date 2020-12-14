import {
  parseBigNum,
  Numeric,
  ZERO,
} from '../../util/NumberUtils/NumberUtils';
import * as StringUtils from '../../util/StringUtils/StringUtils';
import * as collection from './collection';
import * as conditional from './conditional';
import * as date from './date';
import { Validator as ValidatorType } from './interfaces';
import * as numeric from './numeric';
import * as allOperators from './operators';
import * as ordinal from './ordinal';
import * as requireValidators from './required';
import * as text from './text';
import * as type from './type';
import {
  validatorCreatorFactory as factory,
  withPreprocessor,
} from './validatorCreatorFactory';

export const addPreprocessor = withPreprocessor;
export const validatorCreatorFactory = factory;

export type Validator<I, F, P> = ValidatorType<I, F, P>;

type IAllValues = any;
export interface IPredicate {
  (value: any, values: IAllValues, props?: any): boolean;
}

////////////////////////////////////////////////////////////////////////////////
// Composed validator creators                                                //
////////////////////////////////////////////////////////////////////////////////

export const isPositiveCreator = (message: string = 'Value must be positive') =>
  ordinal.gtOrdCreator(ZERO)({
    getValidatorErrorMessageOverride: message,
    getValueFromInputOverride: (input) => {
      try {
        const num = parseBigNum(input);
        return num;
      } catch (_) {
        return parseBigNum(-1);
      }
    },
  });

export const operators = allOperators;

export const nonEmptyCreator = (message: string = 'Required') =>
  requireValidators.requiredCreator({
    getValidatorErrorMessageOverride: message,
    getValueFromInputOverride: (value: string) => value && typeof value === 'string' ? StringUtils.trim(String(value)) : value,
  });

export const isNumberCreator = (message: string = 'Please enter a number') =>
  type.isNumberCreator({
    getValidatorErrorMessageOverride: message,
  });

export const isIntegerCreator = (message: string = `Must be a whole number with up to 10 digits`) =>
  type.isIntegerCreator({
    getValidatorErrorMessageOverride: message,
  });

export const maxLengthCreator = (length: number) =>
  collection.maxLengthCreator(length)();

export const exactLengthCreator = (length: number) =>
  collection.exactLengthCreator(length)();

export const minLengthCreator = (length: number) =>
  collection.minLengthCreator(length)();

export const maxCountCreator = (count: number) =>
  collection.maxLengthCreator(count)({
    getValidatorErrorMessageOverride: (value: number) => `No more than ${value} items can be selected`,
  });

export const multipleOfFactorCreator = (factor: Numeric, message?: string) =>
  numeric.multipleOfFactorCreator(parseBigNum(factor))({
    getValidatorErrorMessageOverride: message || `Value must be a multiple of ${factor}`,
  });

export const requiredCreator = requireValidators.requiredCreator;

export const eqCreator = ordinal.eqOrdCreator;
export const ltCreator = ordinal.ltOrdCreator;
export const lteCreator = ordinal.lteOrdCreator;
export const gteCreator = ordinal.gteOrdCreator;
export const inSegmentCreator = ordinal.inSegmentCreator;
export const inIntervalCreator = ordinal.inIntervalCreator;

export const requiredAtLeastOneOfCreator = conditional.requireAtLeastOneOfCreator;
export const uniqueAmongstCreator = conditional.uniqueAmongstCreator;
export const checkedOneOfCreator = conditional.checkedOneOfCreator;
export const ltFieldCreator = conditional.ltFieldCreator;
export const lteFieldCreator = conditional.lteFieldCreator;
export const cantBeNegativeIfCreator = conditional.cantBeNegativeIfCreator;
export const gteFieldCreator = conditional.gteFieldCreator;
export const gtFieldCreator = conditional.gtFieldCreator;
export const requiredIfCheckedCreator = conditional.requiredIfCheckedCreator;
export const requiredIfSetCreator = conditional.requiredIfSetCreator;

export const notInTheFutureCreator = date.notInTheFutureCreator;
export const notInThePastCreator = date.notInThePastCreator;
export const afterTheDateCreator = date.afterTheDateCreator;
export const sameAsOrBeforeTheDateCreator = date.sameAsOrBeforeTheDateCreator;
export const sameAsOrAfterTheDateCreator = date.sameAsOrAfterTheDateCreator;
export const sameAsOrBeforeFieldCreator = date.sameAsOrBeforeFieldCreator;
export const sameAsOrAfterFieldCreator = date.sameAsOrAfterFieldCreator;
export const maxInTheFutureCreator = date.maxInTheFutureCreator;
export const maxInThePastCreator = date.maxInThePastCreator;
export const overMonthFieldCreator = date.overMonthFieldCreator;

export const minQueryLengthCreator = collection.minQueryLengthCreator();
export const isNotInCreator = collection.isNotInCreator;
export const maxLengthCreatorWithIgnoreByRegex = collection.maxLengthCreatorWithIgnoreByRegex;

export const matchesRegexCreator = (regexp: string, message?: string) => text.matchesRegexCreator(new RegExp(`^${regexp}$`), message);
export const matchesRegexFieldCreator = text.matchesRegexFieldCreator;

////////////////////////////////////////////////////////////////////////////////
// Actual validators                                                          //
////////////////////////////////////////////////////////////////////////////////

export const required = requiredCreator();
export const nonEmpty = nonEmptyCreator();
export const isNumber = isNumberCreator();
export const isInteger = isIntegerCreator();
export const minQueryLength = minQueryLengthCreator();

export const isNonNegative = ordinal.gteOrdCreator(0)({
  getValidatorErrorMessageOverride: 'Value must be non-negative',
});

export const isPositive = isPositiveCreator('Is not a positive number');

export const isValidURL = text.isValidURLCreator();
export const isValidEmail = text.isValidEmailCreator();

////////////////////////////////////////////////////////////////////////////////
// Custom validators & utils                                                  //
////////////////////////////////////////////////////////////////////////////////

export const requiredPhoneNumberCreator = (message: string = 'Phone number is incomplete') =>
  (...requiredParts: string[]): Validator<any, any, any> =>
    (value: any) => {
      const regionCodeMissing = requiredParts.includes('regionCode') && !value.regionCode;
      const numberMissing = requiredParts.includes('number') && !value.number;
      return regionCodeMissing || numberMissing
        ? message
        : undefined;
    };

export const requiredPhoneNumber = requiredPhoneNumberCreator();

export const validateIf = <I, F, P>(predicate: IPredicate | boolean, validator: Validator<I, F, P> | Array<Validator<I, F, P>>): Validator<I, F, P> =>
  (value: any, allValues: IAllValues, props: any) => {
    const validate = typeof predicate === 'function' ? predicate(value, allValues, props) : predicate;

    if (validate) {
      if (validator instanceof Array) {
        return validator.map((fn) => fn(value, allValues, props)).find((validation) => validation !== undefined && validation !== '');
      } else {
        return validator(value, allValues, props);
      }
    }

    return;
  };

export const externalIDLength = maxLengthCreator(64);

export const zeroToHundredInterval = inIntervalCreator({ min: 0, max: 100 })
  ({ getValidatorErrorMessageOverride: 'Value has to be larger than 0 and smaller than 100' });
