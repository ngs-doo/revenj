import { BigNumber } from 'bignumber.js';

import {
  isIntegerCreator,
  isNumberCreator,
} from '../type';

describe('validation', () => {
  describe('isNumberCreator', () => {
    const isNumber = isNumberCreator();

    it('should return undefined on valid numeric or the value is not present', () => {
      expect(isNumber(1, {})).toBeUndefined();
      expect(isNumber('2.2', {})).toBeUndefined();
      expect(isNumber(new BigNumber(22), {})).toBeUndefined();
      expect(isNumber(undefined as any, {})).toBeUndefined();
      expect(isNumber(null as any, {})).toBeUndefined();
    });

    it('should return an error when value exists but is not treatable as a numeric value', () => {
      expect(isNumber(NaN, {})).toBeDefined();
      expect(isNumber(NaN, {})).toMatchSnapshot();
      expect(isNumber('asdf2.2', {})).toBeDefined();
      expect(isNumber('asdf2.2', {})).toMatchSnapshot();
      expect(isNumber('2.2asdf', {})).toBeDefined();
      expect(isNumber('2.2asdf', {})).toMatchSnapshot();
    });
  });

  describe('isIntegerCreator', () => {
    const isInteger = isIntegerCreator();

    it('should return undefined on valid numeric values', () => {
      expect(isInteger(1, {})).toBeUndefined();
      expect(isInteger(new BigNumber(22), {})).toBeUndefined();
      expect(isInteger(undefined as any, {})).toBeUndefined();
      expect(isInteger(null as any, {})).toBeUndefined();
      expect(isInteger('0', {})).toBeUndefined();
      expect(isInteger('123', {})).toBeUndefined();
    });

    it('should return an error when value exists but is not treatable as an integer', () => {
      expect(isInteger('123.00', {})).toBeDefined();
      expect(isInteger('123.00', {})).toMatchSnapshot();
      expect(isInteger(NaN, {})).toBeDefined();
      expect(isInteger(NaN, {})).toMatchSnapshot();
      expect(isInteger('asdf2.2', {})).toBeDefined();
      expect(isInteger('asdf2.2', {})).toMatchSnapshot();
      expect(isInteger('2.2asdf', {})).toBeDefined();
      expect(isInteger('2.2asdf', {})).toMatchSnapshot();
      expect(isInteger(2.2, {})).toBeDefined();
      expect(isInteger(2.2, {})).toMatchSnapshot();
      expect(isInteger('2.2', {})).toBeDefined();
      expect(isInteger('2.2', {})).toMatchSnapshot();
      expect(isInteger(new BigNumber(2.2), {})).toBeDefined();
      expect(isInteger(new BigNumber(2.2), {})).toMatchSnapshot();
    });
  });
});
