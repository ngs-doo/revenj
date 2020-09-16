import { DateFormatter } from '../DateFormatter';

describe('DateFormatter', () => {
  beforeAll(() => {
    const dateNow = jest.fn(() => 0);
    (global as any).Date.oldNow = global.Date.now;
    global.Date.now = dateNow;
  });

  afterAll(() => {
    global.Date.now = (global as any).Date.oldNow;
    (global as any).Date.oldNow = undefined;
  });

  it('formatDisplayDate with undefined date', () => {
    const date = DateFormatter.formatDisplayDate(undefined as any);

    expect(date).toEqual(null);
  });

  it('formatDisplayDate with date', () => {
    const date = DateFormatter.formatDisplayDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('01 Jan 1970');
  });

  it('formatPresentationalDate with undefined params', () => {
    const date = DateFormatter.formatPresentationalDate(undefined as any, undefined);

    expect(date).toEqual(null);
  });

  it('formatPresentationalDate with date and format', () => {
    const date = DateFormatter.formatPresentationalDate('01 Jan 1970' as DateStr, 'DD/MM/YYYY');

    expect(date).toEqual('01/01/1970');
  });

  it('formatPresentationalDate with date', () => {
    const date = DateFormatter.formatPresentationalDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('01 Jan 1970');
  });

  it('setPresentationalDateFormat with undefined format', () => {
    DateFormatter.setPresentationalDateFormat(undefined as any);
    const date = DateFormatter.formatPresentationalDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('01 Jan 1970');
    expect(DateFormatter.getPresentationalDateFormat()).toEqual('DD MMM YYYY');
  });

  it('setPresentationalDateFormat with defined format', () => {
    DateFormatter.setPresentationalDateFormat('YYYY DD MM');
    const date = DateFormatter.formatPresentationalDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('1970 01 01');
    expect(DateFormatter.getPresentationalDateFormat()).toEqual('YYYY DD MM');
  });

  it('setInputDateFormat with undefined format', () => {
    DateFormatter.setInputDateFormat(undefined as any);
    const date = DateFormatter.formatInputDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('01/01/1970');
    expect(DateFormatter.getInputDateFormat()).toEqual('DD/MM/YYYY');
  });

  it('setInputDateFormat with defined format', () => {
    DateFormatter.setInputDateFormat('YYYY DD MM');
    const date = DateFormatter.formatInputDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('1970 01 01');
    expect(DateFormatter.getInputDateFormat()).toEqual('YYYY DD MM');
  });

  it('setInputDateFormat with defined format', () => {
    DateFormatter.setInputDateFormat('Y D M');
    const date = DateFormatter.formatInputDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('1970 01 01');
    expect(DateFormatter.getInputDateFormat()).toEqual('YYYY DD MM');
  });

  it('setInputDateFormat with defined format', () => {
    DateFormatter.setInputDateFormat('YY DDDD MM');
    const date = DateFormatter.formatInputDate('01 Jan 1970' as DateStr);

    expect(date).toEqual('1970 01 01');
    expect(DateFormatter.getInputDateFormat()).toEqual('YYYY DD MM');
  });

  it('getInputDateFormat', () => {
    const inputDateFormat = DateFormatter.getInputDateFormat();

    expect(inputDateFormat).toEqual('YYYY DD MM');
  });

});
