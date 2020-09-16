// TODO there are instances where we are using BigNumber constructors directly, refactor to use this util
import BigNumber from 'bignumber.js';

import * as FunctionalUtils from '../FunctionalUtils/FunctionalUtils';

export type Numeric = BigNumber | number | string;

export const parseBigNum = (bigNum: Numeric | BigNumber): BigNumber => {
  if (bigNum === '' || isNumber(bigNum)) {
    return new BigNumber(bigNum === '' ? 0 : bigNum);
  }
  throw new Error(bigNum + ' could not be parsed!');
};

export const isNumber = (n: any): boolean => {
  return !Number.isNaN(parseFloat(n)) && isFinite(n) && n !== '';
};

export const sum = (numbers: Numeric[], initial: Numeric = 0): BigNumber => numbers.map(
    (num) => tryParseBigNumOrZero(num),
  ).reduce(
    (acc: BigNumber, num: BigNumber): BigNumber => acc.plus(num),
    new BigNumber(initial),
  );

export const sumToDecimal = (numbers: Numeric[]): string => sum(numbers).toString();

export const average = (numbers: Numeric[]): BigNumber => numbers.length === 0
  ? new BigNumber(0)
  : sum(numbers).dividedBy(numbers.length);

export const difference = (numbers: Numeric[], initial: Numeric = 0): BigNumber => numbers.map(
  (num) => new BigNumber(num),
).reduce(
  (acc: BigNumber, num: BigNumber): BigNumber => acc.minus(num),
  new BigNumber(initial),
);

export const add = (a: Numeric, b: Numeric): string => {
  return parseBigNum(a).plus(parseBigNum(b)).toString();
};

export const subtract = (a: Numeric, b: Numeric): string => {
  return parseBigNum(a).minus(parseBigNum(b)).toString();
};

export const times = (a: Numeric, b: Numeric): string => {
  return parseBigNum(a).times(b).toString();
};

export const divide = (a: Numeric, b: Numeric): string => {
  return parseBigNum(a).dividedBy(parseBigNum(b)).toString();
};

export const percentageValue = (percent: Numeric, amount: Numeric): string => {
  return times(divide(percent, 100), amount);
};

export const floor = (a: Numeric): string => {
  return parseBigNum(a).floor().toString();
};

export const round = (a: Numeric, decimals: number): string => {
  return parseBigNum(a).toFixed(decimals).toString();
};

export const gt = (a: Numeric, b: Numeric): boolean => {
  return parseBigNum(a).gt(parseBigNum(b));
};

export const gte = (a: Numeric, b: Numeric): boolean => {
  return parseBigNum(a).gte(parseBigNum(b));
};

export const lt = (a: Numeric, b: Numeric): boolean => {
  return parseBigNum(a).lt(parseBigNum(b));
};

export const lte = (a: Numeric, b: Numeric): boolean => {
  return parseBigNum(a).lte(parseBigNum(b));
};

export const nonZero = (a?: Numeric): boolean => a != null && !parseBigNum(a).isZero();

export const isZero = (a?: Numeric): boolean => a != null && parseBigNum(a).isZero();

export const eq = (a?: Numeric, b?: Numeric): boolean =>
  a != null && b != null && parseBigNum(a).equals(b);

export const isNumeric = (a?: any): a is Numeric => {
  if (typeof a !== 'string' && typeof a !== 'number' && !(a instanceof BigNumber)) {
    return false;
  }

  return FunctionalUtils.tryOrDefault(() => {
    parseBigNum(a);
    return true;
  }, false);
};

// TODO change to use more generic type later
export const compare = (a: BigNumber, b: BigNumber): number => {
  if (a.greaterThan(b)) {
    return 1;
  }

  if (a.lessThan(b)) {
    return -1;
  }

  return 0;
};

export const naturalToOrdinal = (num: number): string => {
  if (Number.isNaN(num)) {
    return String(num);
  }

  if (num % 10 === 1 && num % 100 !== 11) {
    return `${num}st`;
  } else if (num % 10 === 2 && num % 100 !== 12) {
    return `${num}nd`;
  } else if (num % 10 === 3 && num % 100 !== 13) {
    return `${num}rd`;
  }

  return `${num}th`;
};

export const equals = (a: Numeric, b: Numeric): boolean => {
  const bna = parseBigNum(a);
  const bnb = parseBigNum(b);

  return bna.equals(bnb);
};

export const notEquals = (a: Numeric, b: Numeric): boolean =>
  !equals(a, b);

export const tryParseBigNumOrZero = (bigNum: Numeric) =>
  FunctionalUtils.tryOrDefault(() => parseBigNum(bigNum), new BigNumber('0'));

export const ZERO = parseBigNum(0);
export const NAN = new BigNumber(NaN);

export interface IRange {
  from: Numeric;
  to: Numeric;
}

/**
 * Checks if two numeric ranges contain overlaps
 * @param a from - to numeric range, to value is not inclusive
 * @param b from - to numeric range, to value is not inclusive
 */
export const rangesOverlap = (a: IRange, b: IRange): boolean =>
  (lte(a.from, b.from) && gt(a.to, b.from) && lte(a.to, b.to)) ||
  (gte(a.from, b.from) && lt(a.from, b.to) && gte(a.to, b.to)) ||
  (gte(a.from, b.from) && lt(a.from, b.to) && lte(a.to, b.to)) ||
  (lte(a.from, b.from) && lt(a.from, b.to) && gte(a.to, b.to));

export const max = (numOne: Numeric, numTwo: Numeric) => gt(numOne, numTwo) ? numOne : numTwo;
export const min = (numOne: Numeric, numTwo: Numeric) => lt(numOne, numTwo) ? numOne : numTwo;

export const clampNumeric = (num: Numeric, lowerBound: Numeric, upperBound: Numeric) => min(upperBound, max(lowerBound, num));

export const clamp = (num: number, min: number, max: number) => Math.min(max, Math.max(min, num));
