import { DateTimeFormatter as DateFormatter } from '../DateTimeFormatter';

describe('DateTimeFormatter', () => {
  beforeAll(() => {
    const dateNow = jest.fn(() => 0);
    (global as any).Date.oldNow = global.Date.now;
    global.Date.now = dateNow;
  });

  afterAll(() => {
    global.Date.now = (global as any).Date.oldNow;
    (global as any).Date.oldNow = undefined;
  });

  it('formatDateTime with undefined params', () => {
    const date = DateFormatter.formatDateTime(undefined as any);

    expect(date).toEqual(null);
  });

  it('formatDateTime with timestamp', () => {
    const date = DateFormatter.formatDateTime('01 Jan 1971 00:00:00' as TimestampStr);

    expect(date).toEqual('01 Jan 1971 00:00:00');
  });

  it('formatDateTime with timestamp and format', () => {
    const date = DateFormatter.formatDateTime('01 Jan 1971 00:00:00' as TimestampStr, 'HH:SS:mm YYYY MMM DD');

    expect(date).toEqual('00:00:00 1971 Jan 01');
  });

  it('formatDisplayDateTime with timestamp and format', () => {
    const date = DateFormatter.formatDisplayDateTime('01 Jan 1971 00:00:00' as TimestampStr);

    expect(date).toEqual('01 Jan 1971 00:00:00');
  });

  it('setFormat with undefined format', () => {
    DateFormatter.setFormat(undefined as any);
    const date = DateFormatter.formatDisplayDateTime('01 Jan 1971 00:00:00' as TimestampStr);

    expect(date).toEqual('01 Jan 1971 00:00:00');
  });

  it('setFormat with undefined format', () => {
    DateFormatter.setFormat('HH MMM SS YYYY DD mm');
    const date = DateFormatter.formatDisplayDateTime('01 Jan 1971 00:00:00' as TimestampStr);
    const format = DateFormatter.getFormat();

    expect(date).toEqual('00 Jan 00 1971 01 00');
    expect(format).toEqual('HH MMM SS YYYY DD mm');
  });
});
