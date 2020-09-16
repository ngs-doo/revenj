import { unitOfTime } from 'moment';

import { DateFormatter } from '../../util/Formatters/DateFormatter';
import { get } from '../../util/FunctionalUtils/FunctionalUtils';
import {
  UNIT_DAY,
  UNIT_MONTH,
} from '../../util/time/constants';
import {
  time,
  MachineDateFormat,
} from '../../util/time/time';
import { IFormFieldSpecification } from './interfaces';
import * as operators from './operators';
import { validatorCreatorFactory } from './validatorCreatorFactory';

export const notInTheFutureCreator = <F = void, P = void>(precision: unitOfTime.DurationConstructor = UNIT_DAY) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time().format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => 'Date can\'t be set in the future.',
    operator: (today) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isAfter(time(today, MachineDateFormat), precision)
    ),
  });

export const notInThePastCreator = <F = void, P = void>(precision: unitOfTime.DurationConstructor = UNIT_DAY) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time().format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => 'Date can\'t be set in the past.',
    operator: (today) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isBefore(time(today, MachineDateFormat), precision)
    ),
  });

export interface IDateOrdValidatorProps {
  date: DateStr;
  precision?: unitOfTime.DurationConstructor;
}

export const afterTheDateCreator = ({
  date,
  precision = UNIT_DAY,
}: IDateOrdValidatorProps) =>
  validatorCreatorFactory<DateStr, DateStr, void, void>({
    getValidatorBaseValue: () => time(date).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => `Date must be after ${DateFormatter.formatPresentationalDate(date)}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      time(input, MachineDateFormat).isAfter(time(target, MachineDateFormat), precision)
    ),
  });

export const sameAsOrBeforeTheDateCreator = <F = void, P = void>({
  date,
  precision = UNIT_DAY,
}: IDateOrdValidatorProps) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time(date).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => `Date must be before or same as ${DateFormatter.formatPresentationalDate(date)}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isAfter(time(target, MachineDateFormat), precision)
    ),
  });

export const sameAsOrAfterTheDateCreator = <F = void, P = void>({
  date,
  precision = UNIT_DAY,
}: IDateOrdValidatorProps) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time(date).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => `Date must be after or same as ${DateFormatter.formatPresentationalDate(date)}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isBefore(time(target, MachineDateFormat), precision)
    ),
  });

export const sameAsOrBeforeFieldCreator = <F>(
  left: IFormFieldSpecification,
  right: IFormFieldSpecification,
  precision = UNIT_DAY,
) =>
  validatorCreatorFactory<DateStr, DateStr, F, void>({
    getValidatorBaseValue: (formData) =>
      time(get(formData, right.path as unknown as DeepKeyOf<F>) as unknown as DateStr).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => `${left.label} must be same as or before ${right.label}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isAfter(time(target, MachineDateFormat), precision)
    ),
  });

export const sameAsOrAfterFieldCreator = <F>(
  left: IFormFieldSpecification,
  right: IFormFieldSpecification,
  precision = UNIT_DAY,
) =>
  validatorCreatorFactory<DateStr, DateStr, F, void>({
    getValidatorBaseValue: (formData) =>
      time(get(formData, right.path as keyof F) as unknown as DateStr).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => `${left.label} must be same as or after ${right.label}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      !time(input, MachineDateFormat).isBefore(time(target, MachineDateFormat), precision)
    ),
  });

export const overMonthFieldCreator = <F>(
  _left: IFormFieldSpecification,
  right: IFormFieldSpecification,
  precision = UNIT_DAY,
) =>
  validatorCreatorFactory<DateStr, DateStr, F, void>({
    getValidatorBaseValue: (formData) =>
      time(get(formData, right.path as keyof F) as unknown as string).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: (date) =>
      `Can't be after ${DateFormatter.formatPresentationalDate(time(date).add(1, UNIT_MONTH).format(MachineDateFormat) as DateStr)}`,
    operator: (target) => (input) => (
      !operators.required()(input) ||
      time(input, MachineDateFormat).isBefore(time(target, MachineDateFormat).add(1, UNIT_MONTH), precision)
    ),
  });

export const maxInTheFutureCreator = <F, P>(
  amount: number,
  precision = UNIT_DAY,
) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time().add(amount, precision).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => amount > 0 ? `Cannot be more than ${amount} ${precision}s in the future` : 'Cannot be in the future',
    operator: (target) => (input) => (
      !operators.required()(input) ||
      time(input, MachineDateFormat).isSameOrBefore(time(target, MachineDateFormat), precision)
    ),
  });

export const maxInThePastCreator = <F, P>(
  amount: number,
  precision = UNIT_DAY,
) =>
  validatorCreatorFactory<DateStr, DateStr, F, P>({
    getValidatorBaseValue: () => time().subtract(amount, precision).format(MachineDateFormat) as DateStr,
    getValidatorErrorMessage: () => amount > 0 ? `Cannot be more than ${amount} ${precision}s in the past` : 'Cannot be in the past',
    operator: (target) => (input) => (
      !operators.required()(input) ||
      time(input, MachineDateFormat).isSameOrAfter(time(target, MachineDateFormat), precision)
    ),
  });
