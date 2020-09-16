import {
  UNIT_DAY,
  UNIT_MONTH,
} from '../../../util/time/constants';
import { time } from '../../../util/time/time';
import {
  afterTheDateCreator,
  maxInTheFutureCreator,
  maxInThePastCreator,
  notInTheFutureCreator,
  notInThePastCreator,
  sameAsOrAfterFieldCreator,
  sameAsOrAfterTheDateCreator,
  sameAsOrBeforeFieldCreator,
  sameAsOrBeforeTheDateCreator,
} from '../date';

interface IDateForm {
  before: DateStr;
  after: DateStr;
}

const mockDateForm: IDateForm = {
  after: '2018-03-14' as DateStr,
  before: '2018-03-14' as DateStr,
};

describe('validation', () => {
  describe('notInTheFuture', () => {
    const notInTheFuture = notInTheFutureCreator(UNIT_DAY)();

    it('should return undefined if the input date is not in the future', () => {
      expect(notInTheFuture(time().format() as DateStr)).toBeUndefined();
      expect(notInTheFuture(time().subtract(1, UNIT_DAY).format() as DateStr)).toBeUndefined();
      expect(notInTheFuture(undefined as any)).toBeUndefined();
    });

    it('should return an error message if the input date is in the future', () => {
      expect(notInTheFuture(time().add(2, UNIT_DAY).format() as DateStr)).toBeDefined();
      expect(notInTheFuture(time().add(2, UNIT_DAY).format() as DateStr)).toMatchSnapshot();
    });
  });

  describe('notInThePast', () => {
    const notInThePast = notInThePastCreator(UNIT_DAY)();

    it('should return undefined if the input date is not in the past', () => {
      expect(notInThePast(time().format() as DateStr)).toBeUndefined();
      expect(notInThePast(time().add(1, UNIT_DAY).format() as DateStr)).toBeUndefined();
      expect(notInThePast(undefined as any)).toBeUndefined();
    });

    it('should return an error message if the input date is in the past', () => {
      expect(notInThePast(time().subtract(2, UNIT_DAY).format() as DateStr)).toBeDefined();
      expect(notInThePast(time().subtract(2, UNIT_DAY).format() as DateStr)).toMatchSnapshot();
    });
  });

  describe('afterTheDateCreator', () => {
    const afterTheDate = afterTheDateCreator({
      date: '2018-03-14' as DateStr,
    })();

    it('should return undefined if the input date is after some date', () => {
      expect(afterTheDate('2018-03-15' as DateStr)).toBeUndefined();
      expect(afterTheDate(undefined as any)).toBeUndefined();
    });

    it('should return an error message if the input date is before or same as some date', () => {
      expect(afterTheDate('2018-03-13' as DateStr)).toBeDefined();
      expect(afterTheDate('2018-03-13' as DateStr)).toMatchSnapshot();
      expect(afterTheDate('2018-03-14' as DateStr)).toBeDefined();
      expect(afterTheDate('2018-03-14' as DateStr)).toMatchSnapshot();
      expect(afterTheDate('2018-03-14T00:00:01' as DateStr)).toBeDefined();
      expect(afterTheDate('2018-03-14T00:00:01' as DateStr)).toMatchSnapshot();
      expect(afterTheDate('2018-03-13T23:59:59' as DateStr)).toBeDefined();
      expect(afterTheDate('2018-03-13T23:59:59' as DateStr)).toMatchSnapshot();
    });
  });

  describe('sameAsOrAfterTheDateCreator', () => {
    const sameAsOrAfterTheDate = sameAsOrAfterTheDateCreator({
      date: '2018-03-14' as DateStr,
    })();

    it('should return undefined if the input date is on or after some date', () => {
      expect(sameAsOrAfterTheDate('2018-03-14' as DateStr)).toBeUndefined();
      expect(sameAsOrAfterTheDate('2018-03-14T00:00:01' as DateStr)).toBeUndefined();
      expect(sameAsOrAfterTheDate('2018-03-15' as DateStr)).toBeUndefined();
      expect(sameAsOrAfterTheDate(undefined as any)).toBeUndefined();
    });

    it('should return an error message if the input date is before some date', () => {
      expect(sameAsOrAfterTheDate('2018-03-13' as DateStr)).toBeDefined();
      expect(sameAsOrAfterTheDate('2018-03-13' as DateStr)).toMatchSnapshot();
      expect(sameAsOrAfterTheDate('2018-03-13T23:59:59' as DateStr)).toBeDefined();
      expect(sameAsOrAfterTheDate('2018-03-13T23:59:59' as DateStr)).toMatchSnapshot();
    });
  });

  describe('sameAsOrBeforeTheDateCreator', () => {
    const sameAsOrBeforeTheDate = sameAsOrBeforeTheDateCreator({
      date: '2018-03-14' as DateStr,
    })();

    it('should return undefined if the input date is on or before some date', () => {
      expect(sameAsOrBeforeTheDate('2018-03-14' as DateStr)).toBeUndefined();
      expect(sameAsOrBeforeTheDate('2018-03-14T23:59:59' as DateStr)).toBeUndefined();
      expect(sameAsOrBeforeTheDate('2018-03-13' as DateStr)).toBeUndefined();
      expect(sameAsOrBeforeTheDate(undefined as any)).toBeUndefined();
    });

    it('should return an error message if the input date is after some date', () => {
      expect(sameAsOrBeforeTheDate('2018-03-15' as DateStr)).toBeDefined();
      expect(sameAsOrBeforeTheDate('2018-03-15' as DateStr)).toMatchSnapshot();
      expect(sameAsOrBeforeTheDate('2018-03-15T23:59:59' as DateStr)).toBeDefined();
      expect(sameAsOrBeforeTheDate('2018-03-15T23:59:59' as DateStr)).toMatchSnapshot();
    });
  });

  describe('sameAsOrAfterFieldCreator', () => {
    const sameAsOrAfter = sameAsOrAfterFieldCreator<IDateForm>(
      { label: 'Date', path: 'date' },
      { label: 'After', path: 'after' },
    )();

    it('should return undefined if input date is after or same as field date', () => {
      expect(sameAsOrAfter('2018-03-14' as DateStr, mockDateForm)).toBeUndefined();
      expect(sameAsOrAfter('2018-03-15' as DateStr, mockDateForm)).toBeUndefined();
      expect(sameAsOrAfter(undefined as any, mockDateForm)).toBeUndefined();
    });

    it('should return an error message if input date is before the field date', () => {
      expect(sameAsOrAfter('2018-03-13' as DateStr, mockDateForm)).toBeDefined();
      expect(sameAsOrAfter('2018-03-13' as DateStr, mockDateForm)).toMatchSnapshot();
    });
  });

  describe('sameAsOrBeforeFieldCreator', () => {
    const sameAsOrBefore = sameAsOrBeforeFieldCreator<IDateForm>(
      { label: 'Date', path: 'date' },
      { label: 'Before', path: 'before' },
    )();

    it('should return undefined if input date is after or same as field date', () => {
      expect(sameAsOrBefore('2018-03-14' as DateStr, mockDateForm)).toBeUndefined();
      expect(sameAsOrBefore('2018-03-13' as DateStr, mockDateForm)).toBeUndefined();
      expect(sameAsOrBefore(undefined as any, mockDateForm)).toBeUndefined();
    });

    it('should return an error message if input date is before the field date', () => {
      expect(sameAsOrBefore('2018-03-15' as DateStr, mockDateForm)).toBeDefined();
      expect(sameAsOrBefore('2018-03-15' as DateStr, mockDateForm)).toMatchSnapshot();
    });
  });

  describe('maxInTheFutureCreator', () => {
    const max10DaysInFuture = maxInTheFutureCreator(10)();

    it('should return undefined if the date is within boundaries from today (default unit day)', () => {
      expect(max10DaysInFuture(time().format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().subtract(1, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().add(1, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().add(10, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
    });

    it('should return an error message if the input date is outside boundaries from today (default unit day)', () => {
      expect(max10DaysInFuture(time().add(11, UNIT_DAY).format() as DateStr, {})).toBe('Cannot be more than 10 days in the future');
    });

    it('should allow for customisation of precision/step', () => {
      const max1MonthInTheFuture = maxInTheFutureCreator(1, UNIT_MONTH)();
      expect(max1MonthInTheFuture(time().add(11, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max1MonthInTheFuture(time().add(101, UNIT_DAY).format() as DateStr, {})).toBe('Cannot be more than 1 months in the future');
    });
  });

  describe('maxInThePastCreator', () => {
    const max10DaysInFuture = maxInThePastCreator(10)();

    it('should return undefined if the date is within boundaries from today (default unit day)', () => {
      expect(max10DaysInFuture(time().format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().subtract(1, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().add(1, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max10DaysInFuture(time().subtract(10, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
    });

    it('should return an error message if the input date is outside boundaries from today (default unit day)', () => {
      expect(max10DaysInFuture(time().subtract(11, UNIT_DAY).format() as DateStr, {})).toBe('Cannot be more than 10 days in the past');
    });

    it('should allow for customisation of precision/step', () => {
      const max1MonthInTheFuture = maxInThePastCreator(1, UNIT_MONTH)();
      expect(max1MonthInTheFuture(time().subtract(11, UNIT_DAY).format() as DateStr, {})).toBeUndefined();
      expect(max1MonthInTheFuture(time().subtract(101, UNIT_DAY).format() as DateStr, {})).toBe('Cannot be more than 1 months in the past');
    });
  });
});
