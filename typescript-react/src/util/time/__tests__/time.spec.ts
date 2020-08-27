import {
  asStartOfMonth,
  asStartOfYear,
} from '../time';

describe('time utils', () => {
  describe('asStartOfMonth', () => {
    it('should resolve the first day of the month', () => {
      expect(asStartOfMonth('2020-03-25')).toBe('2020-03-01');
      expect(asStartOfMonth('2020-03-01')).toBe('2020-03-01');
      expect(asStartOfMonth('2020-02-29')).toBe('2020-02-01');
    });
  });

  describe('asStartOfYear', () => {
    it('should resolve the first month of the year without touching the day', () => {
      expect(asStartOfYear('2020-03-25')).toBe('2020-01-25');
      expect(asStartOfYear('2020-03-01')).toBe('2020-01-01');
      expect(asStartOfYear('2020-02-29')).toBe('2020-01-29');
    });
  });
});
