import { Numeric } from '../../util/NumberUtils/NumberUtils';
import { sortBy } from '../../util/SortUtils/SortUtils';
import * as Validator from '../validation';

const isInteger = (num: string): boolean => Validator.isInteger(num, {}) == null;

export const parseNumber = (it: Numeric): Int | Double => {
  const numberStr = String(it);
  if (isInteger(numberStr)) {
    return Number.parseInt(numberStr, 10) as Int;
  } else {
    return Number.parseFloat(numberStr) as Double;
  }
};

export const getLabel = <T>(item: T, labelKeys: Array<keyof T>): string =>
  labelKeys.map((labelKey) => item[labelKey] != null ? String(item[labelKey]) : '—').join(', ');

export const getTopNLabels = <T>(items: T[], labelKeys: Array<keyof T>, valueKey: keyof T, N: number): string[] => {
  const groupings = {};

  for (const item of items) {
    const key = getLabel(item, labelKeys);
    const value = item[valueKey] != null ? parseNumber(String(item[valueKey])) : 0;
    groupings[key] = value;
  }

  return sortBy(
    (key) => groupings[key],
    Object.keys(groupings),
  ).reverse().slice(0, N);
};
