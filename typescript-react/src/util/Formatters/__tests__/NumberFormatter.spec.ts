import BigNumber from 'bignumber.js';

import { CurrencyFormat, CurrencyFormatter } from '../CurrencyFormatter';
import {
  constructFormat,
  formatNumberToDecimals,
  getBigNumberConfig,
  getBigNumberConstructor,
  getPrecision,
  parseNumberIncludingMachineFormat,
} from '../NumberFormatter';

describe('NumberFormatter', () => {
  describe('parseNumberIncludingMachineFormat', () => {
    it('should parse numbers when they match the format', () => {
      const format = '#,##0.00';

      expect(parseNumberIncludingMachineFormat(100.00, format)).toBe('100');
      expect(parseNumberIncludingMachineFormat('200.00', format)).toBe('200');
      expect(parseNumberIncludingMachineFormat('0.00', format)).toBe('0');
      expect(parseNumberIncludingMachineFormat('200.5', format)).toBe('200.5');
      expect(parseNumberIncludingMachineFormat('200.51', format)).toBe('200.51');
      expect(parseNumberIncludingMachineFormat('0.5', format)).toBe('0.5');
      expect(parseNumberIncludingMachineFormat('300', format)).toBe('300');
      expect(parseNumberIncludingMachineFormat('1,000', format)).toBe('1000');
      expect(parseNumberIncludingMachineFormat('1,234.56', format)).toBe('1234.56');
      expect(parseNumberIncludingMachineFormat('1,234,567.89', format)).toBe('1234567.89');
      expect(parseNumberIncludingMachineFormat('-1,000.50', format)).toBe('-1000.5');
      expect(parseNumberIncludingMachineFormat('.5', format)).toBe('0.5');
      expect(parseNumberIncludingMachineFormat('10.', format)).toBe('10');

      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat(100, nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('0', nonDecimalFormat)).toBe('0');
      expect(parseNumberIncludingMachineFormat('100', nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('1234', nonDecimalFormat)).toBe('1234');
      expect(parseNumberIncludingMachineFormat('1,000', nonDecimalFormat)).toBe('1000');
      expect(parseNumberIncludingMachineFormat('1,234,567', nonDecimalFormat)).toBe('1234567');
      expect(parseNumberIncludingMachineFormat('-1,000', nonDecimalFormat)).toBe('-1000');
    });

    it('should return NaN for values that cannot be parsed', () => {
      const format = '#,##0.00';

      expect(parseNumberIncludingMachineFormat('abc', format)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('12abc', format)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('1.234', format)).toBeNaN();
    });

    it('should return an empty string for empty input', () => {
      const format = '#,##0.00';

      expect(parseNumberIncludingMachineFormat('', format)).toBe('');
    });

    it('should parse machine formatted numbers even if they do not match the format and are equal to the whole number', () => {
      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat(100.00, nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('100.00', nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('0.00', nonDecimalFormat)).toBe('0');
      expect(parseNumberIncludingMachineFormat('0.0', nonDecimalFormat)).toBe('0');
      expect(parseNumberIncludingMachineFormat('12345.00', nonDecimalFormat)).toBe('12345');
      expect(parseNumberIncludingMachineFormat('-100.00', nonDecimalFormat)).toBe('-100');
      expect(parseNumberIncludingMachineFormat('1,000.00', nonDecimalFormat)).toBe('1000');
      expect(parseNumberIncludingMachineFormat('1,234,567.00', nonDecimalFormat)).toBe('1234567');
      expect(parseNumberIncludingMachineFormat('1000.000', nonDecimalFormat)).toBe('1000');
    });

    it('should not mess with machine formatted numbers that are not \.0+ and they should just fail to be parsed', () => {
      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat('100.5', nonDecimalFormat)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('100.51', nonDecimalFormat)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('1.5', nonDecimalFormat)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('0.1', nonDecimalFormat)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('1,000.5', nonDecimalFormat)).toBeNaN();
    });

    it('should only trigger if the decimal separator is not there', () => {
      const format = '#.##0,00';

      expect(parseNumberIncludingMachineFormat('100,00', format)).toBe('100');
      expect(parseNumberIncludingMachineFormat('0,00', format)).toBe('0');
      expect(parseNumberIncludingMachineFormat('100,51', format)).toBe('100.51');
      expect(parseNumberIncludingMachineFormat('100,5', format)).toBe('100.5');
      expect(parseNumberIncludingMachineFormat('1.234,00', format)).toBe('1234');
      expect(parseNumberIncludingMachineFormat('1.234,56', format)).toBe('1234.56');
      expect(parseNumberIncludingMachineFormat('-1.000,50', format)).toBe('-1000.5');
    });

    it('should keep grouping intact but still strip machine decimals when "." is the group separator', () => {
      const format = '#.##0';

      // ungrouped values stay intact
      expect(parseNumberIncludingMachineFormat('1', format)).toBe('1');
      expect(parseNumberIncludingMachineFormat('12', format)).toBe('12');
      expect(parseNumberIncludingMachineFormat('123', format)).toBe('123');
      expect(parseNumberIncludingMachineFormat('1234', format)).toBe('1234');
      expect(parseNumberIncludingMachineFormat('12345', format)).toBe('12345');
      expect(parseNumberIncludingMachineFormat('1000000', format)).toBe('1000000');
      expect(parseNumberIncludingMachineFormat('0', format)).toBe('0');
      // machine decimals are stripped
      expect(parseNumberIncludingMachineFormat('1.0', format)).toBe('1');
      expect(parseNumberIncludingMachineFormat('-1.0', format)).toBe('-1');
      expect(parseNumberIncludingMachineFormat('1.00', format)).toBe('1');
      expect(parseNumberIncludingMachineFormat('100.00', format)).toBe('100');
      expect(parseNumberIncludingMachineFormat('1000000.00', format)).toBe('1000000');
      // grouping is preserved
      expect(parseNumberIncludingMachineFormat('1.000', format)).toBe('1000');
      expect(parseNumberIncludingMachineFormat('10.000', format)).toBe('10000');
      expect(parseNumberIncludingMachineFormat('100.000', format)).toBe('100000');
      expect(parseNumberIncludingMachineFormat('999.999.999', format)).toBe('999999999');
      expect(parseNumberIncludingMachineFormat('1.000.000', format)).toBe('1000000');
      expect(parseNumberIncludingMachineFormat('12.345.678', format)).toBe('12345678');
      expect(parseNumberIncludingMachineFormat('1.234.567.890', format)).toBe('1234567890');
      expect(parseNumberIncludingMachineFormat('-1.000', format)).toBe('-1000');
      expect(parseNumberIncludingMachineFormat('-1.000.000', format)).toBe('-1000000');
    });
  });

  describe('formatNumberToDecimals', () => {
    describe('when precision is provided', () => {
      beforeEach(() => {
        CurrencyFormatter.setFormat('#0.00');
      });

      afterEach(() => {
        CurrencyFormatter.setFormat(CurrencyFormat);
      });

      it('should format a number to a given number of decimals', () => {
        expect(formatNumberToDecimals(100.25, 2)).toBe('100.25');
        expect(formatNumberToDecimals('100.25', 2)).toBe('100.25');
        expect(formatNumberToDecimals(25.5, 1)).toBe('25.5');
        expect(formatNumberToDecimals(1000.5, 1)).toBe('1000.5');
        expect(formatNumberToDecimals('100', 0)).toBe('100');
        expect(formatNumberToDecimals(0, 2)).toBe('0.00');
        expect(formatNumberToDecimals(-5.5, 2)).toBe('-5.50');
        expect(formatNumberToDecimals(1000000, 0)).toBe('1000000');
      });

      it('should round the numbers if they have too many decimals', () => {
        expect(formatNumberToDecimals('100.5', 0)).toBe('101');
        expect(formatNumberToDecimals('100.25', 1)).toBe('100.3');
        expect(formatNumberToDecimals('100.24', 1)).toBe('100.2');
        expect(formatNumberToDecimals('1.5', 0)).toBe('2');
        expect(formatNumberToDecimals(123.456, 2)).toBe('123.46');
        expect(formatNumberToDecimals('0.001', 2)).toBe('0.00');
      });

      it('should append decimals when insufficient precision is provided', () => {
        expect(formatNumberToDecimals(100, 2)).toBe('100.00');
        expect(formatNumberToDecimals(1000, 2)).toBe('1000.00');
        expect(formatNumberToDecimals('10.55', 4)).toBe('10.5500');
      });

      it('should return NaN for values that are not numbers', () => {
        expect(formatNumberToDecimals('abc', 2)).toBe('NaN');
      });
    });

    describe('when no precision is given', () => {
      afterEach(() => {
        CurrencyFormatter.setFormat(CurrencyFormat);
      });

      describe('"#,##0.00" format ("," group, "." decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#,##0.00');
        });

        it('preserves the value decimals', () => {
          expect(formatNumberToDecimals('100')).toBe('100');
          expect(formatNumberToDecimals('100.25')).toBe('100.25');
          expect(formatNumberToDecimals(25.5)).toBe('25.5');
          expect(formatNumberToDecimals('123.456')).toBe('123.456');
          expect(formatNumberToDecimals(0)).toBe('0');
          expect(formatNumberToDecimals(-5.5)).toBe('-5.5');
        });

        it('groups thousands while preserving decimals', () => {
          expect(formatNumberToDecimals('1000.5')).toBe('1,000.5');
          expect(formatNumberToDecimals(1000000)).toBe('1,000,000');
          expect(formatNumberToDecimals('1234567.89')).toBe('1,234,567.89');
        });

        it('preserves trailing zeros the value was given with', () => {
          expect(formatNumberToDecimals('0.50')).toBe('0.50');
          expect(formatNumberToDecimals('1.500')).toBe('1.500');
          expect(formatNumberToDecimals('100.00')).toBe('100.00');
        });

        it('does not round', () => {
          expect(formatNumberToDecimals('0.001')).toBe('0.001');
          expect(formatNumberToDecimals('123.4567')).toBe('123.4567');
        });
      });

      describe('"#.##0,00" format ("." group, "," decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#.##0,00');
        });

        it('preserves the value decimals', () => {
          expect(formatNumberToDecimals('100')).toBe('100');
          expect(formatNumberToDecimals('100.25')).toBe('100,25');
          expect(formatNumberToDecimals(25.5)).toBe('25,5');
          expect(formatNumberToDecimals('123.456')).toBe('123,456');
          expect(formatNumberToDecimals(0)).toBe('0');
          expect(formatNumberToDecimals(-5.5)).toBe('-5,5');
        });

        it('groups thousands while preserving decimals', () => {
          expect(formatNumberToDecimals('1000.5')).toBe('1.000,5');
          expect(formatNumberToDecimals(1000000)).toBe('1.000.000');
          expect(formatNumberToDecimals('1234567.89')).toBe('1.234.567,89');
        });

        it('preserves trailing zeros the value was given with', () => {
          expect(formatNumberToDecimals('0.50')).toBe('0,50');
          expect(formatNumberToDecimals('1.500')).toBe('1,500');
          expect(formatNumberToDecimals('100.00')).toBe('100,00');
        });

        it('does not round', () => {
          expect(formatNumberToDecimals('0.001')).toBe('0,001');
          expect(formatNumberToDecimals('123.4567')).toBe('123,4567');
        });
      });

      describe('"#.##0" format ("." group, "," decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#.##0');
        });

        it('preserves the value decimals', () => {
          expect(formatNumberToDecimals('100')).toBe('100');
          expect(formatNumberToDecimals('100.25')).toBe('100,25');
          expect(formatNumberToDecimals(25.5)).toBe('25,5');
          expect(formatNumberToDecimals('123.456')).toBe('123,456');
          expect(formatNumberToDecimals(0)).toBe('0');
          expect(formatNumberToDecimals(-5.5)).toBe('-5,5');
        });

        it('groups thousands while preserving decimals', () => {
          expect(formatNumberToDecimals('1000.5')).toBe('1.000,5');
          expect(formatNumberToDecimals(1000000)).toBe('1.000.000');
          expect(formatNumberToDecimals('1234567.89')).toBe('1.234.567,89');
        });

        it('preserves trailing zeros the value was given with', () => {
          expect(formatNumberToDecimals('0.50')).toBe('0,50');
          expect(formatNumberToDecimals('1.500')).toBe('1,500');
          expect(formatNumberToDecimals('100.00')).toBe('100,00');
        });

        it('does not round', () => {
          expect(formatNumberToDecimals('0.001')).toBe('0,001');
          expect(formatNumberToDecimals('123.4567')).toBe('123,4567');
        });
      });
    });
  });

  describe('caching', () => {
    const getConfig = (pattern: string): BigNumber.Config => ({
      FORMAT: constructFormat(pattern) as BigNumber.Format,
    });

    describe('getBigNumberConfig', () => {
      const decimalSeparatorFor = (pattern: string) =>
        getBigNumberConfig(getConfig(pattern)).FORMAT!.decimalSeparator;

      it('keeps the declared decimal separator', () => {
        expect(decimalSeparatorFor('#.##0,00')).toBe(',');
        expect(decimalSeparatorFor('#,##0.00')).toBe('.');
      });

      it('derives a decimal separator when the format declares none', () => {
        expect(decimalSeparatorFor('#.##0')).toBe(',');
        expect(decimalSeparatorFor('#,##0')).toBe('.');
      });

      it('returns the same cached config for the same config', () => {
        const config = getConfig('#.##0,00');

        expect(getBigNumberConfig(config)).toBe(getBigNumberConfig(config));
      });
    });

    describe('getBigNumberConstructor', () => {
      it('returns a BigNumber constructor configured with the format', () => {
        const BigNum = getBigNumberConstructor(getConfig('#.##0,00'));

        expect(typeof BigNum).toBe('function');
        expect(new BigNum('1234.5').toFormat(2)).toBe('1.234,50');
      });

      it('derives the decimal separator when the format declares none', () => {
        const BigNum = getBigNumberConstructor(getConfig('#.##0'));

        expect(new BigNum('1234.5').toFormat(2)).toBe('1.234,50');
      });

      it('returns the same cached constructor for the same config', () => {
        const config = getConfig('#,##0.00');

        expect(getBigNumberConstructor(config)).toBe(
          getBigNumberConstructor(config),
        );
      });

      it('returns different constructors for different formats', () => {
        expect(getBigNumberConstructor(getConfig('#,##0.00'))).not.toBe(
          getBigNumberConstructor(getConfig('#.##0,00')),
        );
      });
    });

    describe('getPrecision', () => {
      it('returns the precision declared by the format', () => {
        expect(getPrecision(getConfig('#,##0.00'))).toBe(2);
        expect(getPrecision(getConfig('#.##0,00'))).toBe(2);
        expect(getPrecision(getConfig('#,##0.000'))).toBe(3);
        expect(getPrecision(getConfig('#.##0'))).toBe(0);
      });

      it('defaults to zero when the config has no format', () => {
        expect(getPrecision({} as BigNumber.Config)).toBe(0);
      });
    });
  });
});
