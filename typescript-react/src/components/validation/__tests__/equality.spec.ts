import { BigNumber } from 'bignumber.js';

import { isEqualCreator } from '../equality';

describe('validation', () => {
  describe('isEqualCreator', () => {
    type AThing = [number, number, string];
    interface ISomethingElse { x: number; y: { z: string }; }

    const isEqual = <T>(val: T) => isEqualCreator(val)();

    it('should return undefined if input value is a multiple of base value', () => {
      expect(isEqual(44)(44)).toBeUndefined();
      expect(isEqual('asdf')('asdf')).toBeUndefined();
      expect(isEqual(false)(false)).toBeUndefined();
      expect(isEqual(undefined)(undefined)).toBeUndefined();
      expect(isEqual(null)(null)).toBeUndefined();
      expect(isEqual(new BigNumber(-7))(new BigNumber(-7))).toBeUndefined();
      expect(isEqual<AThing>([1, 2, '3'])([1, 2, '3'])).toBeUndefined();
      expect(isEqual<ISomethingElse>({ x: 2, y: { z: 'wat' } })({ x: 2, y: { z: 'wat' } })).toBeUndefined();
    });

    it('should return an error message if input value is not a multiple of base value', () => {
      expect(isEqual(44)(13)).toBeDefined();
      expect(isEqual(44)(13)).toMatchSnapshot();
      expect(isEqual('asdf')('aasdf')).toBeDefined();
      expect(isEqual('asdf')('aasdf')).toMatchSnapshot();
      expect(isEqual(false)(true)).toBeDefined();
      expect(isEqual(false)(true)).toMatchSnapshot();
      expect(isEqual(new BigNumber(-7))(new BigNumber(7))).toBeDefined();
      expect(isEqual(new BigNumber(-7))(new BigNumber(7))).toMatchSnapshot();
      expect(isEqual<AThing>([1, 2, '3'])([3, 4, 'asd'])).toBeDefined();
      expect(isEqual<AThing>([1, 2, '3'])([3, 4, 'asd'])).toMatchSnapshot();
      expect(isEqual<ISomethingElse>({ x: 0, y: { z: 'hmmm' } })({ x: 2, y: { z: 'wat' } })).toBeDefined();
      expect(isEqual<ISomethingElse>({ x: 0, y: { z: 'hmmm' } })({ x: 2, y: { z: 'wat' } })).toMatchSnapshot();
    });
  });
});
