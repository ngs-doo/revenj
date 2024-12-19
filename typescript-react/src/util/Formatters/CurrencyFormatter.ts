import * as NumberUtils from '../NumberUtils/NumberUtils';
import {
  constructFormat,
  formatNumber,
  INumberFormat,
  MachineBigNumber,
} from './NumberFormatter';

export const CurrencyFormat = '#,##0.00';
export const MachineCurrencyFormat = '#0.00';

export enum CurrencySymbolPlacement {
  Prefix = 'Prefix',
  Postfix = 'Postfix',
}

export class CurrencyFormatterClass {
  private currencyFormat: string = CurrencyFormat;
  private currencySymbol: string = '';
  private currencyPlacement: CurrencySymbolPlacement = CurrencySymbolPlacement.Postfix;
  private format: INumberFormat | null = constructFormat(CurrencyFormat);

  setFormat = (format: string) => {
    if (format == null) {
      console.warn('Currency format not specified, defaulting the currency format to initial value');
    }
    this.currencyFormat = format ? format : CurrencyFormat;
    this.format = constructFormat(this.currencyFormat);
  }

  setCurrencySymbol = (symbol: string, placement: CurrencySymbolPlacement) => {
    this.currencySymbol = symbol;
    this.currencyPlacement = placement;
  }

  machineFormatCurrency = (number: NumberUtils.Numeric) => {
    return formatNumber(new MachineBigNumber(number).toString(), MachineCurrencyFormat);
  }

  formatNumber = (number: NumberUtils.Numeric): string => {
    const normalizedNumber: NumberUtils.Numeric = this.normalizeNumber(number);
    return formatNumber(new MachineBigNumber(normalizedNumber).toString(), this.currencyFormat);
  }

  formatCurrency = (amount: NumberUtils.Numeric): string => {
    const formattedNumber = this.formatNumber(amount);
    return this.currencyPlacement === CurrencySymbolPlacement.Prefix
      ? `${this.currencySymbol} ${formattedNumber}`
      : `${formattedNumber} ${this.currencySymbol}`;
  }

  private normalizeNumber = (number: NumberUtils.Numeric): NumberUtils.Numeric => {
    // Strip decimals if they are not allowed
    const decimalStrippedNumber: NumberUtils.Numeric = number && this.format && this.format.precision === 0 && this.format.decimalSeparator && String(number).includes(this.format.decimalSeparator)
      ? String(number).split(this.format.decimalSeparator)[0]
      : number;

    // Strip commas if they are not allowed but the BE is not sending machine formatted numbers
    if (decimalStrippedNumber && this.format && this.format.groupSeparator) {
      if (this.format.groupSeparator === '.') {
        const [, suffix] = decimalStrippedNumber.toString().split(this.format.groupSeparator);
        if (suffix && suffix.length === 2) { // Assume that the BE is giving us machine formatted numbers, which should be generally true
          return decimalStrippedNumber;
        }
      }

      return String(decimalStrippedNumber).split(this.format.groupSeparator).join('');
    }

    return decimalStrippedNumber;
  }
}

export const CurrencyFormatter = new CurrencyFormatterClass(); // Singleton by default, maintaining compatibility
