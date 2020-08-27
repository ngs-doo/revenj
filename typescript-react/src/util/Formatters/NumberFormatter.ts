import BigNumber, { Format } from 'bignumber.js';

import * as NumberUtils from '../NumberUtils/NumberUtils';

export const isNumber = NumberUtils.isNumber;


export interface INumberFormat extends Format {
  precision: number;
}

interface IFormats {
  [key: string]: INumberFormat;
}

// Regex to match the grouping and decimal strategies and separators
const REGEX = /^(?:(?:#*)([., ]))*?(?:(#*)([., ]))?(#*0)(?:([.,])(0+))?$/;
// Format used for machine number representation
const MACHINE_FORMAT: Format = {
  decimalSeparator: '.',
  groupSeparator: '',
};

export const MachineBigNumber = BigNumber.another({ FORMAT: MACHINE_FORMAT });

const formats: IFormats = {};

/**
 * Format given number to a string. If formatting fails, empty string.
 *
 * @param   {any}
 * @returns {String} Empty string if formatting fails.
 */
export const formatNumber = (number: any, pattern?: string): string => {
  let format: any;
  if (pattern) {
    // validate pattern throws and stops the function execution
    validatePattern(pattern);
    format = constructFormat(pattern);

    if (format && !isNumber(number)) {
      return '';
    }
  } else {
    format = MACHINE_FORMAT;
  }

  BigNumber.config({
    FORMAT: format,
  });

  if (format) {
    const precision = format && format.precision ? format.precision : undefined;
    return new BigNumber(number).toFormat(precision);
  } else {
    return '';
  }
};

// HACK: On instances where smallest denomination is 1, everything goes bad. In this case, we're trying to guess the decimal separator
export const formatNumberToDecimals = (number: NumberUtils.Numeric, decimals: number): string => {
  const existingFormat = BigNumber.config();
  const format = {
    ...existingFormat,
    FORMAT: {
      ...existingFormat.FORMAT,
      decimalSeparator: existingFormat.FORMAT!.decimalSeparator !== ''
        ? existingFormat.FORMAT!.decimalSeparator
        : existingFormat.FORMAT!.groupSeparator === '.'
          ? ','
          : '.',
    },
  };
  const FormatBigNum = BigNumber.another(format);
  return new FormatBigNum(number).toFormat(decimals);
};

/**
 * Convert given value to a number type. If conversion fails, NaN
 *
 * @param   {any}
 * @returns {Number} NaN if conversion fails.
 */
export const parseNumber = (numberString: number | string, pattern: string): number | string => {
  validatePattern(pattern);
  const format = constructFormat(pattern);

  numberString = (numberString + '').trim();

  // Check if negative number
  let prefix = '';
  if (numberString[0] === '-') {
    prefix = '-';
    numberString = numberString.substring(1, numberString.length);
  }

  let whole;
  let decimal;

  if (format && format.decimalSeparator) {
    const numberParts = numberString.split(format.decimalSeparator);
    if (numberParts.length > 2) {
      return NaN;
    }
    [whole, decimal] = numberParts;
  } else {
    whole = numberString;
  }

  // remove the grouping separators
  if (format && format.groupSeparator) {
    whole = whole.split(format.groupSeparator).join('');
  }

  if ((whole && !isOnlyNumbers(whole)) || (decimal && !isOnlyNumbers(decimal))) {
    return NaN;
  }

  if (decimal && decimal.length > (format?.precision ?? 0)) {
    // too many decimals -> dont parse
    return NaN;
  }

  if (!decimal) {
    decimal = '';
    if (!whole) {
      // Both parts of the number are empty
      return '';
    }
  }

  BigNumber.config({ FORMAT: MACHINE_FORMAT });

  return new BigNumber(prefix + whole + '.' + decimal).toFormat();
};

export const fromMachineFormat = (numberString: number | string, pattern: string): string => {
  validatePattern(pattern);
  const format = constructFormat(pattern)!;
  const hasDecimalSeparator = format.decimalSeparator != null && format.decimalSeparator !== '';

  if (format.precision === 0 && !hasDecimalSeparator && numberString != null) {
    return String(numberString).replace(/\.0+$/g, '');
  }

  return String(numberString);
};

/**
 * Convert given value to a number type. If conversion fails, NaN
 * Will parse machine formatted numbers even if they are sent with decimals included
 */
export const parseNumberIncludingMachineFormat = (numberString: number | string, pattern: string): number | string => {
  return parseNumber(
    fromMachineFormat(numberString, pattern),
    pattern,
  );
};

export const validatePattern = (pattern: string) => {
  const format = constructFormat(pattern);
  if (!format) {
    throw new Error('formatNumber(): Format pattern cannot be parsed!');
  }
};

export const constructFormat = (pattern: string): INumberFormat | null => {
  const format: INumberFormat = {
    decimalSeparator: '.',
    groupSeparator: ',',
    groupSize: 0,
    precision: 0,
    secondaryGroupSize: 0,
  };
  if (typeof pattern !== 'string') {
    return null;
  }

  if (pattern in formats) {
    return formats[pattern];
  }

  const rGroups = REGEX.exec(pattern);
  try {
    if (rGroups && rGroups[0] === pattern) {
      // the configuration part of the pattern is valid

      // decimal separator
      const decimalSeparator = rGroups[5] ? rGroups[5] : '';
      // grouping separator
      let groupSeparator;
      if (rGroups[1] && rGroups[1] !== rGroups[3]) {
        return null;
      } else {
        groupSeparator = rGroups[3] ? rGroups[3] : '';
      }
      format.decimalSeparator = decimalSeparator;
      format.groupSeparator = groupSeparator;
      // check if grouping and decimal separators are the same
      if (format.groupSeparator &&
        format.decimalSeparator &&
        format.groupSeparator === format.decimalSeparator) {
        return null;
      }
      // check if the rest of the pattern is valid
      const validPart = rGroups.slice(1).join();
      const rest = pattern.substring(0, pattern.lastIndexOf(validPart));
      if (rest.match(new RegExp(`[^#${format.groupSeparator}]`))) {
        // the rest contains a character that is not a # or a grouping separator
        return null;
      }
      // grouping strategy
      if (format.groupSeparator) {
        const groups = [rGroups[1] && rGroups[2] ? rGroups[2].length : null, rGroups[4] ? rGroups[4].length : null];
        const groupingStrategy = groups.filter(
          (element, index, array) => {
            // filters out duplicates and nulls
            return element ? array.indexOf(element) === index : false;
          },
        ).reverse();
        format.groupSize = groupingStrategy[0] as number;
        format.secondaryGroupSize = groupingStrategy[1] as number;
      }
      // precision
      format.precision = rGroups[6] ? rGroups[6].length : 0;
    }

    formats[pattern] = format;

    return format;
  } catch (e) {
    console.error(e);
  }
  return null;
};

export const isOnlyNumbers = (str: string): boolean => {
  if (str.match(/[^0-9]/) === null) {
    return true;
  }
  return false;
};
