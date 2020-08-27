import {
  isValidEmailCreator,
  isValidURLCreator,
  matchesRegexCreator,
  matchesRegexFieldCreator,
} from '../text';

describe('validation', () => {
  describe('text', () => {
    describe('isValidURLCreator', () => {
      const isValidURL = (value: any) => isValidURLCreator()(value, {});

      it('should return undefined if input value is a valid URL', () => {
        expect(isValidURL('https://www.potato.net:9000/potato')).toBeUndefined();
        expect(isValidURL('https://www.potato.technologies/potato')).toBeUndefined();
        expect(isValidURL('http://somethi.ng/special.json')).toBeUndefined();
        expect(isValidURL('https://another.thing.com:9000/even/more/special.json?offset=10&limit=50')).toBeUndefined();
        expect(isValidURL('')).toBeUndefined();
      });

      it('should return a validation message if input value is not a valid URL', () => {
        expect(isValidURL('http://localhost:9000/something/special.json')).toBe('Value must be valid URL');
        expect(isValidURL('https://127.0.0.1:9000/something/even/more/special.json?offset=10&limit=50')).toBe('Value must be valid URL');
        expect(isValidURL('htp://localhost:9000/something/special.json')).toBe('Value must be valid URL');
        expect(isValidURL('sftp://127.0.0.1:9000/something/even/more/special.json?offset=10&limit=50')).toBe('Value must be valid URL');
        expect(isValidURL('https://www.potato.<>net:9000/potato')).toBe('Value must be valid URL');
        expect(isValidURL('smudge')).toBe('Value must be valid URL');
      });
    });

    describe('isValidEmailCreator', () => {
      const isValidEmail = (value: any) => isValidEmailCreator()(value, {});

      it('should return undefined if input value is a valid email', () => {
        expect(isValidEmail('john.doe@unknown.technologies.sa')).toBeUndefined();
        expect(isValidEmail('it-is@awesome')).toBeUndefined();
        expect(isValidEmail('potato@potato')).toBeUndefined();
        expect(isValidEmail('potato@potato.tomato')).toBeUndefined();
        expect(isValidEmail('#hashtag@whatever')).toBeUndefined();
        expect(isValidEmail(undefined)).toBeUndefined();
        expect(isValidEmail('')).toBeUndefined();
      });

      it('should return a validation message if input value is not a valid email', () => {
        expect(isValidEmail('smudge')).toBe('Value must be valid mail');
      });
    });

    describe('matchesRegexCreator', () => {
      const matchesFiveW = ((value: any) => matchesRegexCreator(/^\w{5}$/)()(value, {}));
      const matchesDigitsWithMessage = (value: any) => matchesRegexCreator(/^\d+$/, 'Must be just digits')()(value, {});

      it('should return undefined if it matches or is empty', () => {
        expect(matchesFiveW('aaaaa')).toBeUndefined();
        expect(matchesFiveW('AAAAA')).toBeUndefined();
        expect(matchesFiveW('0101a')).toBeUndefined();
        expect(matchesFiveW(undefined)).toBeUndefined();
        expect(matchesFiveW('')).toBeUndefined();
        expect(matchesDigitsWithMessage('123')).toBeUndefined();
      });

      it('should return a validation message if input does not match', () => {
        expect(matchesFiveW('+-')).toBe('Value must match the defined pattern');
        expect(matchesFiveW('four')).toBe('Value must match the defined pattern');
        expect(matchesFiveW('evenmore')).toBe('Value must match the defined pattern');
      });

      it('should accept a custom validation message', () => {
        expect(matchesDigitsWithMessage('99999P')).toBe('Must be just digits');
      });
    });

    describe('matchesRegexFieldCreator', () => {
      const matchesRegex = matchesRegexFieldCreator({ path: 'regex', label: 'pattern' })();

      it('should do nothing if regex is not set', () => {
        expect(matchesRegex('test', { regex: undefined })).toBeUndefined();
        expect(matchesRegex('text', { regex: '' })).toBeUndefined();
        expect(matchesRegex('text', { regex: ' ' })).toBeUndefined();
      });

      it('should return a undefined if the pattern matches', () => {
        const values = { regex: '\\w+' };

        expect(matchesRegex('test', values)).toBeUndefined();
        expect(matchesRegex('123', values)).toBeUndefined();
        expect(matchesRegex('_', values)).toBeUndefined();
      });

      it('should return a validation message if the input does not match the pattern', () => {
        const values = { regex: '\\d+' };

        expect(matchesRegex(' ', values)).toBe('Does not match pattern');
        expect(matchesRegex('test', values)).toBe('Does not match pattern');
      });
    });
  });
});
