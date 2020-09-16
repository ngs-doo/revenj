import { exactLengthCreator, isInCreator, isNotInCreator, maxLengthCreator, minLengthCreator } from '../collection';

describe('validation', () => {
  describe('maxLength', () => {
    it('should return undefined if input value is not longer than maxLength', () => {
      const maxLength20 = maxLengthCreator(20)();
      expect(maxLength20('under 20 chars', {})).toBeUndefined();
      expect(maxLength20('exactly twenty chars', {})).toBeUndefined();
    });

    it('should return an error message if input value is longer than maxLength', () => {
      const maxLength20 = maxLengthCreator(20)();
      expect(maxLength20('slightly over twenty characters', {})).toBeDefined();
      expect(maxLength20('slightly over twenty characters', {})).toMatchSnapshot();
    });

    it('should use the digit count as length for numbers (regression test for IN-8549)', () => {
      const maxLength3 = maxLengthCreator(3)();
      expect(maxLength3(1, {})).not.toBeDefined();
      expect(maxLength3(1234, {})).toBeDefined();
      expect(maxLength3(1234, {})).toMatchSnapshot();
    });
  });

  describe('exactLength', () => {
    it('should return undefined if the value is null, empty string, or has the exact length', () => {
      const exactLength5 = exactLengthCreator(5)();
      expect(exactLength5(null as any, {})).toBeUndefined();
      expect(exactLength5(undefined as any, {})).toBeUndefined();
      expect(exactLength5('', {})).toBeUndefined();
      expect(exactLength5('12345', {})).toBeUndefined();
      expect(exactLength5([1, 2, 3, 4, 5], {})).toBeUndefined();
    });

    it('should return an error message if the input has a different length', () => {
      const exactLength5 = exactLengthCreator(5)();
      expect(exactLength5('2', {})).toBeDefined();
      expect(exactLength5('2', {})).toMatchSnapshot();
      expect(exactLength5('333333333', {})).toBeDefined();
      expect(exactLength5('333333333', {})).toMatchSnapshot();
      expect(exactLength5([], {})).toBeDefined();
      expect(exactLength5([], {})).toMatchSnapshot();
      expect(exactLength5([1, 1, 1, 1, 1, 1, 1, 1, 1, 1], {})).toBeDefined();
      expect(exactLength5([1, 1, 1, 1, 1, 1, 1, 1, 1, 1], {})).toMatchSnapshot();
    });
  });

  describe('minLength', () => {
    it('should return undefined if input value is longer than minLength', () => {
      const minLength10 = minLengthCreator(10)();
      expect(minLength10('more than 10 chars', {})).toBeUndefined();
      expect(minLength10('five chars', {})).toBeUndefined();
    });

    it('should return an error message if input value is shorter than minLength', () => {
      const minLength7 = minLengthCreator(7)();
      expect(minLength7('4-char', {})).toBeDefined();
      expect(minLength7('4-char', {})).toMatchSnapshot();
    });

    it('should use the digit count as length for numbers (regression test for IN-8549)', () => {
      const minLength3 = minLengthCreator(3)();
      expect(minLength3(134543, {})).not.toBeDefined();
      expect(minLength3(1, {})).toBeDefined();
      expect(minLength3(12, {})).toMatchSnapshot();
    });
  });

  describe('isIn', () => {
    it('should return undefined if input value is found in the set of values', () => {
      const isIn = isInCreator([3, 5, 6])();
      expect(isIn(3, {})).toBeUndefined();
      expect(isIn(6, {})).toBeUndefined();
    });

    it('should return an error message if input value is not found in the set of values', () => {
      const isIn = isInCreator([3, 5, 6])();
      expect(isIn(1, {})).toBeDefined();
      expect(isIn(9, {})).toBeDefined();
    });
  });

  describe('isNotIn', () => {
    it('should return undefined if input value is not found in the set of values', () => {
      const isIn = isNotInCreator([3, 5, 6])();
      expect(isIn(1, {})).toBeUndefined();
      expect(isIn(9, {})).toBeUndefined();
    });

    it('should return an error message if input value is found in the set of values', () => {
      const isIn = isNotInCreator([3, 5, 6])();
      expect(isIn(3, {})).toBeDefined();
      expect(isIn(6, {})).toBeDefined();
    });
  });
});
