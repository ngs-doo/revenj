import {
  formatNumberToDecimals,
  parseNumberIncludingMachineFormat,
} from '../NumberFormatter';

describe('NumberFormatter', () => {
  describe('parseNumberIncludingMachineFormat', () => {
    it('should parse numbers when they match the format', () => {
      const format = '#,##0.00';

      expect(parseNumberIncludingMachineFormat(100.00, format)).toBe('100');
      expect(parseNumberIncludingMachineFormat('200.00', format)).toBe('200');
      expect(parseNumberIncludingMachineFormat('200.5', format)).toBe('200.5');
      expect(parseNumberIncludingMachineFormat('200.51', format)).toBe('200.51');
      expect(parseNumberIncludingMachineFormat('300', format)).toBe('300');
      expect(parseNumberIncludingMachineFormat('1,000', format)).toBe('1000');

      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat(100, nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('100', nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('1,000', nonDecimalFormat)).toBe('1000');
    });

    it('should parse machine formatted numbers even if they do not match the format and are equal to the whole number', () => {
      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat(100.00, nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('100.00', nonDecimalFormat)).toBe('100');
      expect(parseNumberIncludingMachineFormat('1,000.00', nonDecimalFormat)).toBe('1000');
    });

    it('should not mess with machine formatted numbers that are not \.0+ and they should just fail to be parsed', () => {
      const nonDecimalFormat = '#,##0';

      expect(parseNumberIncludingMachineFormat('100.5', nonDecimalFormat)).toBeNaN();
      expect(parseNumberIncludingMachineFormat('100.51', nonDecimalFormat)).toBeNaN();
    });

    it('should only trigger if the decimal separator is not there', () => {
      const format = '#.##0,00';

      expect(parseNumberIncludingMachineFormat('100,00', format)).toBe('100');
      expect(parseNumberIncludingMachineFormat('100,51', format)).toBe('100.51');
    });
  });

  describe('formatNumberToDecimals', () => {
    it('should format a number to a given number of decimals', () => {
      expect(formatNumberToDecimals(100.25, 2)).toBe('100.25');
      expect(formatNumberToDecimals('100.25', 2)).toBe('100.25');
      expect(formatNumberToDecimals(25.5, 1)).toBe('25.5');
      expect(formatNumberToDecimals(1000.5, 1)).toBe('1000.5');
      expect(formatNumberToDecimals('100', 0)).toBe('100');
    });

    it('should round the numbers if they have too many decimals', () => {
      expect(formatNumberToDecimals('100.5', 0)).toBe('101');
      expect(formatNumberToDecimals('100.25', 1)).toBe('100.3');
      expect(formatNumberToDecimals('100.24', 1)).toBe('100.2');
    });

    it('should append decimals when insufficient precision is provided', () => {
      expect(formatNumberToDecimals(100, 2)).toBe('100.00');
      expect(formatNumberToDecimals(1000, 2)).toBe('1000.00');
      expect(formatNumberToDecimals('10.55', 4)).toBe('10.5500');
    });
  });
});
