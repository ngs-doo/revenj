import {
  INT_MAX_VALUE,
  INT_MIN_VALUE,
} from '../../constants';
import { formatNumberToDecimals } from '../../util/Formatters/NumberFormatter';
import { Numeric, parseBigNum } from '../../util/NumberUtils/NumberUtils';
import { ValidateTrigger } from './interfaces';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Type validators                                                            //
////////////////////////////////////////////////////////////////////////////////

const INT_MIN_VALUE_STRING = formatNumberToDecimals(INT_MAX_VALUE, 0);
const INT_MAX_VALUE_STRING = formatNumberToDecimals(INT_MIN_VALUE, 0);

const NUMBER_NOT_IN_RANGE_ERROR = `Must be a whole number between ${INT_MIN_VALUE_STRING} and ${INT_MAX_VALUE_STRING}`;
const DEFAULT_NUMBER_ERROR = 'Must be a whole number';

export const isIntegerCreator =
  validatorCreatorFactory<void, Numeric, any, any>({
    getValidatorErrorMessage: (_, input) => !(Number(input) <= INT_MAX_VALUE && Number(input) >= INT_MIN_VALUE) ?
      NUMBER_NOT_IN_RANGE_ERROR : DEFAULT_NUMBER_ERROR,
    operator: () => (input) => {
      try {
        const aNumber = parseBigNum(input);
        // string can be of form 0.00 which is an integer, but the string value is written in a non-integer notation
        const isInteger = !aNumber.isNaN() && aNumber.isInteger() &&  Number(input) <= INT_MAX_VALUE && Number(input) >= INT_MIN_VALUE;
        if (typeof input === 'string') {
          return isInteger && /^(?:\+|-)?\d+$/.test(input);
        }

        return isInteger;
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
