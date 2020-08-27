import { requiredCreator } from '../required';

describe('validation', () => {
  describe('requiredCreator', () => {
    const REQUIRED_ERROR_MESSAGE = 'Required';
    const REQUIRED_CUSTOM_ERROR_MESSAGE = 'Custom Required';

    const required = requiredCreator();
    const requiredCustomMessage = requiredCreator({ getValidatorErrorMessageOverride: REQUIRED_CUSTOM_ERROR_MESSAGE });

    it('should return an error message on undefined', () => {
      expect(required(undefined, {})).toBe(REQUIRED_ERROR_MESSAGE);
    });

    it('should return an error message on null', () => {
      expect(required(null, {})).toBe(REQUIRED_ERROR_MESSAGE);
    });

    it('should return an error message on an empty string', () => {
      expect(required('', {})).toBe(REQUIRED_ERROR_MESSAGE);
    });

    it('should return an error message on an empty array', () => {
      expect(required([], {})).toBe(REQUIRED_ERROR_MESSAGE);
    });

    it('should return undefined if array is non-empty', () => {
      expect(required([2], {})).toBeUndefined();
    });

    it('should return undefined if string is non-empty', () => {
      expect(required('a non-empty string', {})).toBeUndefined();
    });

    it('should return undefined if number is passed to required', () => {
      expect(required(0, {})).toBeUndefined();
      expect(required(10, {})).toBeUndefined();
    });

    it('should return undefined if a boolean is passed to required', () => {
      expect(required(true, {})).toBeUndefined();
      expect(required(false, {})).toBeUndefined();
    });

    it('should return a custom error message on undefined', () => {
      expect(requiredCustomMessage(undefined, {})).toBe(REQUIRED_CUSTOM_ERROR_MESSAGE);
    });
  });
});
