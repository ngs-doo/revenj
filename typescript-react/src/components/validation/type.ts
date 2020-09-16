import { Numeric, parseBigNum } from '../../util/NumberUtils/NumberUtils';
import { ValidateTrigger } from './interfaces';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Type validators                                                            //
////////////////////////////////////////////////////////////////////////////////
export const isIntegerCreator =
  validatorCreatorFactory<void, Numeric, any, any>({
    getValidatorErrorMessage: () => `Value must be an integer.`,
    operator: () => (input) => {
      try {
        const aNumber = parseBigNum(input);
        if (typeof input === 'string') {
          // string can be of form 0.00 which is an integer, but the string value is written in a non-integer notation
          return !aNumber.isNaN() && aNumber.isInteger() && /^(?:\+|-)?\d+$/.test(input);
        } else {
          return !aNumber.isNaN() && aNumber.isInteger();
        }
      } catch {
        return false;
      }
    },
    validate: ValidateTrigger.IfInputIsSet,
  });

export const isNumberCreator =
  validatorCreatorFactory<void, Numeric, any, any>({
    getValidatorErrorMessage: () => `Value must be a number.`,
    operator: () => (input) => {
      try {
        const aNumber = parseBigNum(input);
        return !aNumber.isNaN();
      } catch {
        return false;
      }
    },
    validate: ValidateTrigger.IfInputIsSet,
  });
