import { ColumnType, TypescriptResultSet } from '../../../ResultSet/ResultSet';
import { CurrencyFormat, CurrencyFormatter } from '../../../util/Formatters/CurrencyFormatter';
import { getResultSetColumnDefinitions } from '../helpers';

const getColumnDefinition = (type: ColumnType) => {
  const rs = new TypescriptResultSet(['value'], [type], []);
  return getResultSetColumnDefinitions(rs)[0];
};

const getFormatter = (type: ColumnType) =>
  getColumnDefinition(type).formatter as ((value: any) => any) | undefined;

describe('getResultSetColumnDefinitions', () => {
  describe('getFormatter', () => {
    afterEach(() => {
      CurrencyFormatter.setFormat(CurrencyFormat);
    });

    describe('numeric columns', () => {
      const numericTypes = [
        ColumnType.Decimal,
        ColumnType.Float,
        ColumnType.Int,
        ColumnType.Long,
        ColumnType.Short,
      ];
      const decimalTypes = [ColumnType.Decimal, ColumnType.Float];
      const integerTypes = [ColumnType.Int, ColumnType.Long, ColumnType.Short];

      it('assigns a formatter to every numeric column type', () => {
        numericTypes.forEach((type) => {
          expect(typeof getFormatter(type)).toBe('function');
        });
      });

      describe('"#,##0.00" format ("," group, "." decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#,##0.00');
        });

        it('preserves the value decimals for decimal types', () => {
          decimalTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1,234');
            expect(formatter('1234.5')).toBe('1,234.5');
            expect(formatter('1234.55')).toBe('1,234.55');
            expect(formatter('1234.567')).toBe('1,234.567');
            expect(formatter('1234567')).toBe('1,234,567');
          });
        });

        it('formats integer types without decimals', () => {
          integerTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1,234');
            expect(formatter('1234.5')).toBe('1,235');
            expect(formatter('1234567')).toBe('1,234,567');
          });
        });
      });

      describe('"#.##0,00" format ("." group, "," decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#.##0,00');
        });

        it('preserves the value decimals for decimal types', () => {
          decimalTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1.234');
            expect(formatter('1234.5')).toBe('1.234,5');
            expect(formatter('1234.55')).toBe('1.234,55');
            expect(formatter('1234.567')).toBe('1.234,567');
            expect(formatter('1234567')).toBe('1.234.567');
          });
        });

        it('formats integer types without decimals', () => {
          integerTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1.234');
            expect(formatter('1234.5')).toBe('1.235');
            expect(formatter('1234567')).toBe('1.234.567');
          });
        });
      });

      describe('"#.##0" format ("." group, "," decimal)', () => {
        beforeEach(() => {
          CurrencyFormatter.setFormat('#.##0');
        });

        it('preserves the value decimals for decimal types', () => {
          decimalTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1.234');
            expect(formatter('1234.5')).toBe('1.234,5');
            expect(formatter('1234.55')).toBe('1.234,55');
            expect(formatter('1234.567')).toBe('1.234,567');
            expect(formatter('1234567')).toBe('1.234.567');
          });
        });

        it('formats integer types without decimals', () => {
          integerTypes.forEach((type) => {
            const formatter = getFormatter(type)!;
            expect(formatter('1234')).toBe('1.234');
            expect(formatter('1234.5')).toBe('1.235');
            expect(formatter('1234567')).toBe('1.234.567');
          });
        });
      });
    });

    describe('array columns', () => {
      it('joins array values with a comma', () => {
        const formatter = getFormatter(ColumnType.Array)!;
        expect(formatter(['a', 'b', 'c'])).toBe('a, b, c');
      });

      it('stringifies a non-array value', () => {
        const formatter = getFormatter(ColumnType.Array)!;
        expect(formatter('single' as any)).toBe('single');
      });
    });

    describe('null columns', () => {
      it('renders an em dash', () => {
        const formatter = getFormatter(ColumnType.Null)!;
        expect(formatter(null as any)).toBe('—');
      });
    });

    describe('other columns', () => {
      it('does not assign a formatter to non-numeric, non-array, non-null types', () => {
        [
          ColumnType.Boolean,
          ColumnType.Date,
          ColumnType.String,
          ColumnType.Timestamp,
          ColumnType.Url,
        ].forEach((type) => {
          expect(getFormatter(type)).toBeUndefined();
        });
      });
    });
  });
});
