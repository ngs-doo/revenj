import { isEqual } from '../../util/FunctionalUtils/FunctionalUtils';
import {
  equals,
  isNumber,
  parseBigNum,
  Numeric,
} from '../../util/NumberUtils/NumberUtils';
import {
  IEqOperator,
  INumOperator,
  IOrdAlphaOperator,
  IOrdOperator,
  IRequireableOperator,
  Num,
  RegexOperator,
} from './interfaces';

// Basic Operators
export const required: IRequireableOperator = () => (inputValue: any) =>
  (inputValue != null && inputValue !== '' && (Array.isArray(inputValue) ? inputValue.length > 0 : true));

export const eq: IEqOperator = <T extends Numeric>(baseValue: T) => (inputValue: T) => {
  if (isNumber(inputValue)) {
    return equals(inputValue, baseValue);
  } else {
    return isEqual(inputValue, baseValue);
  }
};

export type CompareResult = -1 | 0 | 1;

export interface IAlphaComparator {
  (a: string, b: string): CompareResult;
}

export interface IComparator {
  (a: Numeric, b: Numeric): CompareResult;
}
// #TODO @bigd -> remove this abomination when we normalize select fields
// to yield a single value instad an object because validator operators
// shouldn't think about all the values they might receive
export const compare: IComparator = (a, b) => {
  const src = parseBigNum(String(a));
  const dst = parseBigNum(String(b));

  return src.comparedTo(dst) as CompareResult;
};

export const compareAlpha: IAlphaComparator = (src, dst) => {
  if (src > dst) {
    return 1;
  } else if (src < dst) {
    return -1;
  } else {
    return 0;
  }
};

export const gt: IOrdOperator = (baseValue: Numeric) => (inputValue: Numeric) => compare(inputValue, baseValue) === 1;
export const gte: IOrdOperator = (baseValue: Numeric) => (inputValue: Numeric) => compare(inputValue, baseValue) >= 0;
export const lt: IOrdOperator = (baseValue: Numeric) => (inputValue: Numeric) => compare(inputValue, baseValue) === -1;
export const lte: IOrdOperator = (baseValue: Numeric) => (inputValue: Numeric) => compare(inputValue, baseValue) <= 0;

export const gtAlpha: IOrdAlphaOperator = (baseValue) => (inputValue) => compareAlpha(inputValue, baseValue) === 1;
export const gteAlpha: IOrdAlphaOperator = (baseValue) => (inputValue) => compareAlpha(inputValue, baseValue) >= 0;
export const ltAlpha: IOrdAlphaOperator = (baseValue) => (inputValue) => compareAlpha(inputValue, baseValue) === -1;
export const lteAlpha: IOrdAlphaOperator = (baseValue) => (inputValue) => compareAlpha(inputValue, baseValue) <= 0;

export const multipleOfFactor: INumOperator = (baseValue: Num | undefined) => (inputValue: Num | undefined) => {
  const input = parseBigNum(inputValue!)
  const base = parseBigNum(baseValue!);

  return input.modulo(base).isZero();
};

export const matches: RegexOperator = (regex) => (inputValue) => regex.test(inputValue);
