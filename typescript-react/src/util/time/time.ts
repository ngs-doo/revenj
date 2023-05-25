import moment from 'moment-timezone';

import { UNIT_DAY, UNIT_MILLISECOND, UNIT_MONTH, UNIT_YEAR } from './constants';
import * as timezones from './timezones.json';

export const DisplayDateFormat = 'DD MMM YYYY';
// #FIXME - @bigd - actual input date format should not be a constant
// backend should supply per-user input date format
export const InputDateFormat = 'DD/MM/YYYY';
export const MachineDateFormat = 'YYYY-MM-DD';
export const DateTimeFormat = 'DD MMM YYYY HH:mm:ss';
export const MachineDateTimeFormat = 'YYYY-MM-DDTHH:mm:ss.SSSZ';

moment.tz.load(timezones as any);

type JsDateGetter = () => number;
// HACK: Types are being super-weird
type MomentConstructor = (input?: string | number | moment.Moment, format?: moment.MomentFormatSpecification) => moment.Moment;

const getMoment = moment as unknown as MomentConstructor;

let getDefaultTime: JsDateGetter | undefined;

const UTC = 'UTC';
export const ISO_DATE_ONLY_FORMAT = 'YYYY-MM-DD';

let timeTravelOffsetMs = 0;
let timeZone = UTC;

export const useDefaultTime = (getter: JsDateGetter) => {
  getDefaultTime = getter;
};

export const isTimeTraveling = timeTravelOffsetMs !== 0;

export const setTimeTravelOffset = (offset?: number) =>
  (timeTravelOffsetMs = offset || timeTravelOffsetMs);

export const getTimeTravelOffsetInDays = () => {
  return moment.duration(timeTravelOffsetMs, UNIT_MILLISECOND).asDays();
};

export const setTimeZone = (tz?: string) => (timeZone = tz || timeZone);

export const getTimeZone = () => timeZone;

export const time = (timeValue?: string, format?: moment.MomentFormatSpecification) => {
  if (timeValue != null) {
    return moment.tz(timeValue, format!, timeZone);
  }

  const baseTime = getDefaultTime != null
    ? getMoment(getDefaultTime()).tz(timeZone)
    : getMoment().tz(timeZone);

  return timeTravelOffsetMs
    ? baseTime.add(timeTravelOffsetMs, UNIT_MILLISECOND)
    : baseTime;
};

export const today = (format?: string): DateStr =>
  (format
    ? moment.tz(UTC).format(format)
    : moment.tz(UTC).toISOString()) as DateStr;

export const todaysDate = (): DateStr => today(MachineDateFormat);

export const convertISOToDateStr = (timeValue: DateStr, format?: string): DateStr =>
  moment.tz(timeValue, UTC).format(format) as DateStr;

export const convertDateStrToISO = (timeValue: DateStr, format?: string, onlyDate?: boolean): DateStr => {
  const ret = moment.tz(timeValue, format!, UTC);
  if (onlyDate) {
    return ret.format(ISO_DATE_ONLY_FORMAT) as DateStr;
  }
  return ret.toISOString() as DateStr;
};

export const addDays = (date: string | DateStr, amount: Int) =>
  time(date).add(amount, UNIT_DAY).format(ISO_DATE_ONLY_FORMAT) as DateStr;

export const addMonths = (date: string | DateStr, amount: Int) =>
  time(date).add(amount, UNIT_MONTH).format(ISO_DATE_ONLY_FORMAT) as DateStr;

export const asStartOfMonth = (date: string | DateStr) =>
  time(date).startOf(UNIT_MONTH).format(ISO_DATE_ONLY_FORMAT) as DateStr;

export const asStartOfYear = (date: string | DateStr) => {
  const day = time(date).date();
  return time(date).startOf(UNIT_YEAR).date(day).format(ISO_DATE_ONLY_FORMAT) as DateStr;
};

export const getYearsSince = (date: DateStr, format?: string) =>
  time().diff(moment.tz(date, format!, timeZone), UNIT_YEAR);

export const differenceBetweenDates = (d1: DateStr, d2: DateStr, format: string) =>
  getMoment(d1 as string, format!).diff(getMoment(d2, format!), 'days');

export const fromTimestamp = (timestamp: number) => getMoment(timestamp);

export const differenceBetweenDatesInMinutes = (d1: moment.Moment, d2: moment.Moment) =>
  d1.diff(d2, 'minutes');

export const minutesPassedSince = (date: moment.Moment) =>
  differenceBetweenDatesInMinutes(getMoment(), date);

class Timer {
  private timestamp: number = 0;

  public start = (): void => {
    this.timestamp = moment.now();
  }

  public end = (): number => {
    if (this.timestamp > 0) {
      const now = moment.now();
      const timeSpent = now - this.timestamp;
      this.reset();
      return timeSpent;
    } else {
      throw new Error('Timer not started.');
    }
  }

  private reset = (): void => {
    this.timestamp = 0;
  }
}

export const getTimerInstance = () => new Timer();
