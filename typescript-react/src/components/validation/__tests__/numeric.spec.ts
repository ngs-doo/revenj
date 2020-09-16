import { BigNumber } from 'bignumber.js';

import { multipleOfFactorCreator } from '../numeric';

describe('validation', () => {
  describe('multipleOfFactorCreator', () => {
    const multipleOf7 = multipleOfFactorCreator(7)();
    const multipleOfBn7 = multipleOfFactorCreator(new BigNumber(7))();

    it('should return undefined if input value is a multiple of base value', () => {
      expect(multipleOf7(14, {})).toBeUndefined();
      expect(multipleOfBn7(new BigNumber(14), {})).toBeUndefined();
    });

    it('should return an error message if input value is not a multiple of base value', () => {
      expect(multipleOf7(31, {})).toBeDefined();
      expect(multipleOf7(31, {})).toMatchSnapshot();
      expect(multipleOfBn7(new BigNumber(-3), {})).toBeDefined();
      expect(multipleOfBn7(new BigNumber(-3), {})).toMatchSnapshot();
    });
  });
});
