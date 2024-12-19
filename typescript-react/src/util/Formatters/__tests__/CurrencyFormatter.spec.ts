import { CurrencyFormatter, CurrencySymbolPlacement } from '../CurrencyFormatter';

const DefaultCurrencyFormat = '#,##0.00';

describe('Currency Formatter', () => {
  beforeAll(() => {
    CurrencyFormatter.setCurrencySymbol('$', CurrencySymbolPlacement.Postfix);
  });

  describe('formatNumber', () => {
    beforeEach(() => {
      // Reset the currency format
      CurrencyFormatter.setFormat(DefaultCurrencyFormat);
    });

    it('should format correctly-denominated numbers according to the format', () => {
      CurrencyFormatter.setFormat('#0');
      expect(CurrencyFormatter.formatNumber('1')).toBe('1');
      expect(CurrencyFormatter.formatNumber('12')).toBe('12');
      expect(CurrencyFormatter.formatNumber('123')).toBe('123');
      expect(CurrencyFormatter.formatNumber('1234')).toBe('1234');
      expect(CurrencyFormatter.formatNumber('1.0')).toBe('1');
      expect(CurrencyFormatter.formatNumber('1.00')).toBe('1');

      CurrencyFormatter.setFormat('#,##0');
      expect(CurrencyFormatter.formatNumber('1')).toBe('1');
      expect(CurrencyFormatter.formatNumber('12')).toBe('12');
      expect(CurrencyFormatter.formatNumber('123')).toBe('123');
      expect(CurrencyFormatter.formatNumber('1234')).toBe('1,234');
      expect(CurrencyFormatter.formatNumber('1.0')).toBe('1');
      expect(CurrencyFormatter.formatNumber('1.00')).toBe('1');

      CurrencyFormatter.setFormat('#,##0.00');
      expect(CurrencyFormatter.formatNumber('1')).toBe('1.00');
      expect(CurrencyFormatter.formatNumber('12')).toBe('12.00');
      expect(CurrencyFormatter.formatNumber('123')).toBe('123.00');
      expect(CurrencyFormatter.formatNumber('1234')).toBe('1,234.00');
      expect(CurrencyFormatter.formatNumber('1.0')).toBe('1.00');
      expect(CurrencyFormatter.formatNumber('1.00')).toBe('1.00');
      expect(CurrencyFormatter.formatNumber('1,000,000.00')).toBe('1,000,000.00');

      CurrencyFormatter.setFormat('#.##0,00');
      expect(CurrencyFormatter.formatNumber('1')).toBe('1,00');
      expect(CurrencyFormatter.formatNumber('12')).toBe('12,00');
      expect(CurrencyFormatter.formatNumber('123')).toBe('123,00');
      expect(CurrencyFormatter.formatNumber('1234')).toBe('1.234,00');
      expect(CurrencyFormatter.formatNumber('1234.00')).toBe('1.234,00');
      expect(CurrencyFormatter.formatNumber('1234.50')).toBe('1.234,50');
    });

    it('should strip decimals if the input has them, but the format does not [IN-9133] [Regression]', () => {
      CurrencyFormatter.setFormat('#0');
      expect(CurrencyFormatter.formatNumber('1.00')).toBe('1');
      expect(CurrencyFormatter.formatNumber('1000.00')).toBe('1000');

      CurrencyFormatter.setFormat('#,##0');
      expect(CurrencyFormatter.formatNumber('1.00')).toBe('1');
      expect(CurrencyFormatter.formatNumber('1000.00')).toBe('1,000');
    });

    it('should work correctly if used as references [IN-9964]', () => {
      CurrencyFormatter.setFormat('#,##0.00');

      const formatter = CurrencyFormatter.formatNumber;
      expect(formatter('1')).toBe('1.00');

      const currencyFormatter = CurrencyFormatter.formatCurrency;
      expect(currencyFormatter('1')).toBe('1.00 $');

      const machineFormatter = CurrencyFormatter.machineFormatCurrency;
      expect(machineFormatter('1')).toBe('1.00');
    });
  });

  describe('machineFormatCurrency', () => {
    const testCases = [
      // Valid integers
      { input: '1', output: '1.00' },
      { input: '12', output: '12.00' },
      { input: '123', output: '123.00' },
      { input: '1234', output: '1234.00' },
      { input: '12345', output: '12345.00' },
      { input: '123456789', output: '123456789.00' },
      // Valid zero and negative numbers
      { input: '0', output: '0.00' },
      { input: '-1', output: '-1.00' },
      { input: '-12345', output: '-12345.00' },
      // Valid decimals
      { input: '1.1', output: '1.10' },
      { input: '1.123', output: '1.12' },
      { input: '1234.5678', output: '1234.57' },
      { input: '1000000000000.23', output: '1000000000000.23' },
      // Input with leading zeros
      { input: '000123', output: '123.00' },
      { input: '0000', output: '0.00' },
      // Input with trailing dot
      { input: '123.', output: '123.00' },
      { input: '123.0', output: '123.00' },
      // Invalid inputs
      { input: '', output: '' },
      { input: 'abc', output: '' },
      { input: '12abc', output: '' },
      { input: '1.2.3', output: '' },
    ];

    const formats = [
      '#0',
      '#,##0',
      '#.##0',
      '#,##0.00',
      '#.##0,00',
      '#,##,##0',
    ];

    formats.forEach((format) => {
      it(`should format correctly according to the ${format} format`, () => {
        CurrencyFormatter.setFormat(format);

        testCases.forEach(({ input, output }) => {
          expect(CurrencyFormatter.machineFormatCurrency(input)).toBe(output);
        });
      });
    });
  });
});
