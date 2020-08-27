import {
  camelToWord,
  capitalize,
  isNullOrEmpty,
  safeIsNullOrEmpty,
  safeToString,
  trim,
} from '../StringUtils';

describe('string utils', () => {
  describe('trim', () => {
    it('should trim the string of whitespaces', () => {
      const str = 'potato  ';
      expect(trim(str)).toEqual('potato');
    });
  });

  describe('safeToString', () => {
    it('should fallback to \'-\' when value is null or undefined', () => {
      expect(safeToString<any>(null)).toBe('-');
      expect(safeToString<any>(undefined)).toBe('-');
    });

    it('should fallback to fallback argument when value is null or undefined', () => {
      expect(safeToString<any>(null, '*')).toBe('*');
      expect(safeToString<any>(undefined, '*')).toBe('*');
    });

    it('should convert values to string if they are not null or undefiend', () => {
      expect(safeToString(true)).toBe('true');
      expect(safeToString(false)).toBe('false');
      expect(safeToString(0)).toBe('0');
      expect(safeToString(22)).toBe('22');
      expect(safeToString('watman')).toBe('watman');
    });
  });

  describe('isNullOrEmpty', () => {
    it('should count null and undefined as empty', () => {
      expect(isNullOrEmpty()).toBe(true);
      expect(isNullOrEmpty(undefined)).toBe(true);
      expect(isNullOrEmpty(null as unknown as string)).toBe(true);
    });

    it('should count empty or whitespace only strings as empty', () => {
      expect(isNullOrEmpty('')).toBe(true);
      expect(isNullOrEmpty(' ')).toBe(true);
      expect(isNullOrEmpty('   ')).toBe(true);
      expect(isNullOrEmpty('\t')).toBe(true);
      expect(isNullOrEmpty('\n')).toBe(true);
    });

    it('should count others strings as non-empty', () => {
      expect(isNullOrEmpty('x')).toBe(false);
      expect(isNullOrEmpty('  x  ')).toBe(false);
      expect(isNullOrEmpty('  x')).toBe(false);
      expect(isNullOrEmpty('x  ')).toBe(false);
      expect(isNullOrEmpty('null')).toBe(false);
      expect(isNullOrEmpty('undefined')).toBe(false);
    });
  });

  describe('safeIsNullOrEmpty', () => {
    it('should count null and undefined as empty', () => {
      expect(safeIsNullOrEmpty()).toBe(true);
      expect(safeIsNullOrEmpty(undefined)).toBe(true);
      expect(safeIsNullOrEmpty(null)).toBe(true);
    });

    it('should count empty or whitespace only strings as empty', () => {
      expect(safeIsNullOrEmpty('')).toBe(true);
      expect(safeIsNullOrEmpty(' ')).toBe(true);
      expect(safeIsNullOrEmpty('   ')).toBe(true);
      expect(safeIsNullOrEmpty('\t')).toBe(true);
      expect(safeIsNullOrEmpty('\n')).toBe(true);
    });

    it('should count other strings as non-empty', () => {
      expect(safeIsNullOrEmpty('x')).toBe(false);
      expect(safeIsNullOrEmpty('  x  ')).toBe(false);
      expect(safeIsNullOrEmpty('  x')).toBe(false);
      expect(safeIsNullOrEmpty('x  ')).toBe(false);
      expect(safeIsNullOrEmpty('null')).toBe(false);
      expect(safeIsNullOrEmpty('undefined')).toBe(false);
    });

    it('should count other non-empty values as non-empty', () => {
      expect(safeIsNullOrEmpty(0)).toBe(false);
      expect(safeIsNullOrEmpty(7)).toBe(false);
      expect(safeIsNullOrEmpty(7.7)).toBe(false);
      expect(safeIsNullOrEmpty({number: 7})).toBe(false);
      expect(safeIsNullOrEmpty(['7'])).toBe(false);
      expect(safeIsNullOrEmpty([7])).toBe(false);
      expect(safeIsNullOrEmpty(true)).toBe(false);
      expect(safeIsNullOrEmpty(false)).toBe(false);
    });
  });

  describe('camelToWord', () => {
    it('should correctly split camelCase into capitalised words', () => {
      expect(camelToWord('')).toBe('');
      expect(camelToWord('camelcase')).toBe('Camelcase');
      expect(camelToWord('camelCase')).toBe('Camel Case');
      expect(camelToWord('PascalCase')).toBe('Pascal Case');
      expect(camelToWord('longCamelCaseWord')).toBe('Long Camel Case Word');
    });

    it('should cope with acronyms and series of uppercase letters', () => {
      expect(camelToWord('OLBSum')).toBe('OLB Sum');
      expect(camelToWord('teamIT')).toBe('Team IT');
    });

    it('should not duplicate spaces', () => {
      expect(camelToWord('just a few wordsHere')).toBe('Just a few words Here');
    });
  });

  describe('capitalize', () => {
    it('should capitalize the words in a sentence', () => {
      expect(capitalize('name')).toBe('Name');
      expect(capitalize('name and surname')).toBe('Name and surname');
      expect(capitalize('1st')).toBe('1st');
    });
  });
});
