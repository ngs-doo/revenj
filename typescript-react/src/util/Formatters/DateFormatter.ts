import {
  time,
  DisplayDateFormat,
  InputDateFormat,
  MachineDateFormat,
} from '../time/time';

let dateFormat: string;
let inputDateFormat: string;

export const DateFormatter = {
  dateFromTimestamp: (timestamp: TimestampStr) => {
    return DateFormatter.formatInputDate(timestamp, MachineDateFormat);
  },
  formatDisplayDate: (date: DateStr) => DateFormatter.formatPresentationalDate(date, dateFormat),
  formatInputDate: (date: DateStr | TimestampStr, format?: string): DateStr | null => {
    if (date == null) {
      return null;
    } else {
      return (format
        ? time(date).format(format)
        : inputDateFormat
          ? time(date).format(inputDateFormat)
          : time(date).format(InputDateFormat)) as DateStr;
      }
  },
  formatInternalDate: (date: DateStr): DateStr => time(date).format(MachineDateFormat) as DateStr,
  formatPresentationalDate: (date: DateStr | TimestampStr, format?: string): DateStr | null => {
    if (date == null) {
      return null;
    } else {
      return (format
        ? time(date).format(format)
        : dateFormat
          ? time(date).format(dateFormat)
          : time(date).format(DisplayDateFormat)) as DateStr;
    }
  },
  getInputDateFormat: () => inputDateFormat || InputDateFormat,
  getPresentationalDateFormat: () => dateFormat || DisplayDateFormat,
  setInputDateFormat: (format: string) => inputDateFormat = format != null
    ? sanitizeInputDateFormat(format)
    : InputDateFormat,
  setPresentationalDateFormat: (format: string) => {
    dateFormat = format;
  },
};

export const sanitizeInputDateFormat = (format: string) => {
  return format.toUpperCase().replace(/D+/, 'DD').replace(/M+/, 'MM').replace(/Y+/, 'YYYY');
};
