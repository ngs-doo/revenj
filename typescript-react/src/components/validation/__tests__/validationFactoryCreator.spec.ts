import { ValidateTrigger } from '../interfaces';
import { validatorCreatorFactory, valueIsAbsent } from '../validatorCreatorFactory';

describe('validation', () => {
  describe('valueIsAbsent', () => {
    it('should return true for undefined, null and empty string, true otherwise', () => {
      expect(valueIsAbsent(undefined)).toBe(true);
      expect(valueIsAbsent(null)).toBe(true);
      expect(valueIsAbsent('')).toBe(true);

      expect(valueIsAbsent(0)).toBe(false);
      expect(valueIsAbsent(1)).toBe(false);
      expect(valueIsAbsent(false)).toBe(false);
      expect(valueIsAbsent(true)).toBe(false);
      expect(valueIsAbsent('a string')).toBe(false);
      expect(valueIsAbsent([])).toBe(false);
      expect(valueIsAbsent({})).toBe(false);
    });
  });

  describe('validatorCreatorFactory', () => {
    it('should create a valid validator creator with a validation error message', () => {
      interface IInterval {
        min: number;
        max: number;
      }

      const intervalidatorCreator = validatorCreatorFactory<IInterval, number, void, void>({
        getValidatorBaseValue: () => ({ min: 10, max: 20 }),
        getValidatorErrorMessage: () => `Input value is outside of the interval`,
        operator: (interval) => (input) =>
          input > interval.min && input < interval.max,
      });

      const dayInWeek = intervalidatorCreator({ getValidatorBaseValueOverride: { min: 1, max: 7 } });

      expect(dayInWeek(2)).toBeUndefined();
      expect(dayInWeek(0)).toBe('Input value is outside of the interval');
    });

    it('should create a valid validator creator with a validation error message function', () => {
      interface IInterval {
        min: number;
        max: number;
      }

      const intervalidatorCreator = validatorCreatorFactory<IInterval, number, void, void>({
        getValidatorBaseValue: () => ({ min: 10, max: 20 }),
        getValidatorErrorMessage: ({ min, max }, input) => `Input value {${input}} is not from interval <${min}, ${max}>`,
        operator: (interval) => (input) =>
          input > interval.min && input < interval.max,
      });

      const dayInWeek = intervalidatorCreator({ getValidatorBaseValueOverride: { min: 1, max: 7 } });

      expect(dayInWeek(2)).toBeUndefined();
      expect(dayInWeek(0)).toBe('Input value {0} is not from interval <1, 7>');
    });

    it('should not catch the error from getValidatorErrorMessage if it throws', () => {
      const failidatorCreator = validatorCreatorFactory<void, any, void, void>({
        getValidatorErrorMessage: () => { throw new Error('It indeed failed!'); },
        operator: () => () => true,
        validate: ValidateTrigger.Always,
      });

      const failidator = failidatorCreator();
      const fail = () => failidator('dummy');
      expect(fail).toThrowErrorMatchingSnapshot();
    });

    it('should not catch the error from getValueFromInput if it throws', () => {
      const failidatorCreator = validatorCreatorFactory<void, any, void, void>({
        getValidatorErrorMessage: 'It failed!',
        getValueFromInput: () => { throw new Error('It indeed failed!'); },
        operator: () => () => true,
        validate: ValidateTrigger.Always,
      });

      const failidator = failidatorCreator();
      const fail = () => failidator('dummy');
      expect(fail).toThrowErrorMatchingSnapshot();
    });

    it('should not catch the error from getValidatorBaseValue if it throws', () => {
      const failidatorCreator = validatorCreatorFactory<void, any, void, void>({
        getValidatorBaseValue: () => { throw new Error('It indeed faild because reasons!'); },
        getValidatorErrorMessage: 'It failed!',
        operator: () => () => true,
        validate: ValidateTrigger.Always,
      });

      const failidator = failidatorCreator();
      const fail = () => failidator('dummy');
      expect(fail).toThrowErrorMatchingSnapshot();
    });

    it('should not catch the error from getValueFromBase if it throws', () => {
      const failidatorCreator = validatorCreatorFactory<void, any, void, void>({
        getValidatorErrorMessage: 'It failed!',
        getValueFromBase: () => { throw new Error('It indeed failed!'); },
        operator: () => () => true,
        validate: ValidateTrigger.Always,
      });

      const failidator = failidatorCreator();
      const fail = () => failidator('dummy');
      expect(fail).toThrowErrorMatchingSnapshot();
    });
  });
});
