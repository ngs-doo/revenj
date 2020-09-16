import {
  time,
  DateTimeFormat,
} from '../time/time';

let dateTimeFormat: string;

export const DateTimeFormatter = {
  formatDateTime: (dateTime: TimestampStr | null, format?: string): TimestampStr | null => {
    if (dateTime == null) {
      return null;
    } else {
      return format
        ? time(dateTime).format(format) as TimestampStr
        : dateTimeFormat
          ? time(dateTime).format(dateTimeFormat) as TimestampStr
          : time(dateTime).format(DateTimeFormat)  as TimestampStr;
    }
  },
  formatDisplayDateTime: (date: TimestampStr) => DateTimeFormatter.formatDateTime(date, dateTimeFormat),
  getFormat: () => {
    return dateTimeFormat;
  },
  setFormat: (format: string) => {
    dateTimeFormat = format;
  },
};
