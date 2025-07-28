import {
  INT_MAX_VALUE,
  INT_MIN_VALUE,
  LONG_MAX_VALUE,
  LONG_MIN_VALUE,
} from '../../constants';
import {
  ColumnType,
  TypescriptResultSet,
} from '../../ResultSet/ResultSet';
import {
  SHORT_MAX_VALUE,
  SHORT_MIN_VALUE,
} from '../../constants';
import * as Assert from '../assert';
import { parseBigNum } from '../../util/NumberUtils/NumberUtils';

describe('data marshalling asserting', () => {
  describe('assertPresence', () => {
    it('should return a value if it is set', () => {
      expect(Assert.assertPresence(1, true)).toEqual(1);
      expect(Assert.assertPresence(1, false)).toEqual(1);
      expect(Assert.assertPresence([1], true)).toEqual([1]);
      expect(Assert.assertPresence([1], false)).toEqual([1]);
      expect(Assert.assertPresence('test', true)).toEqual('test');
      expect(Assert.assertPresence('test', false)).toEqual('test');
    });

    it('should return undefined if the value is not present, but not required', () => {
      expect(Assert.assertPresence(null, false)).not.toBeNull();
      expect(Assert.assertPresence(null, false)).toBeUndefined();
      expect(Assert.assertPresence(undefined as any, false)).toBeUndefined();
    });

    it('should raise an error if the value is not present, but required', () => {
      expect(() => Assert.assertPresence(null, true)).toThrow();
      expect(() => Assert.assertPresence(null, true)).toThrowErrorMatchingSnapshot();
      expect(() => Assert.assertPresence(undefined as any, true)).toThrow();
      expect(() => Assert.assertPresence(undefined as any, true)).toThrowErrorMatchingSnapshot();
    });
  });

  describe('assertString', () => {
    it('should pass through strings', () => {
      const valids = ['bla', '', `test
ing`];
      for (const valid of valids) {
        expect(() => Assert.assertString(valid)).not.toThrow();
        expect(Assert.assertString(valid)).toEqual(valid);

      }
    });

    it('should explode on non-strings', () => {
      const invalids = [null, 12, {}];
      for (const invalid of invalids) {
        expect(() => Assert.assertString(invalid as any)).toThrowErrorMatchingSnapshot();
      }
    });

    it('should also validate length if specified', () => {
      const valids = ['a', 'aa', 'aaa'];
      const invalids = ['aaaaaaa', '    a    '];
      for (const valid of valids) {
        expect(Assert.assertString(valid, 3)).toBe(valid);
      }

      for (const invalid of invalids) {
        expect(() => Assert.assertString(invalid, 3)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('assertInt', () => {
    it('should pass through valid integers', () => {
      const valids = [INT_MIN_VALUE, -10, 0, 235, INT_MAX_VALUE] as Int[];
      for (const valid of valids) {
        expect(Assert.assertInt(valid)).toBe(valid);
      }
    });

    it('should explode on non-integers', () => {
      const invalids = [INT_MIN_VALUE - 1, 'bla', null, 1.55, INT_MAX_VALUE + 1] as any[];
      for (const invalid of invalids) {
        expect(() => Assert.assertInt(invalid)).toThrowErrorMatchingSnapshot();
      }
    });

    it.skip('should assert valid Int range', () => {}); // TODO: Some nice day
  });

  describe('assertLong', () => {
    it('should pass through valid longs', () => {
      const valids = [
        '-10',
        '0',
        '235',
        '-1',
        '1',
        '-9007199254740995',
        '-9007199254740999',
        '9007199254740995',
        '9007199254740999',
        Number.MIN_SAFE_INTEGER.toString(),
        Number.MAX_SAFE_INTEGER.toString(),
        LONG_MAX_VALUE.toString(),
        LONG_MIN_VALUE.toString(),
      ] as Long[];
      for (const valid of valids) {
        expect(Assert.assertLong(valid)).toBe(valid);
      }
    });

    it('should explode on non-longs', () => {
      const invalids = [
        'bla',
        null,
        1.55,
        Number.MAX_VALUE,
        Number.MIN_VALUE,
        Number.POSITIVE_INFINITY,
        Number.NEGATIVE_INFINITY,
      ] as any[];
      for (const invalid of invalids) {
        expect(() => Assert.assertLong(invalid)).toThrowErrorMatchingSnapshot();
      }
    });

    it('should assert valid Long range', () => {
      const minValid = LONG_MIN_VALUE.toString();
      const maxValud = LONG_MAX_VALUE.toString();
      expect(Assert.assertLong(minValid)).toBe(minValid);
      expect(Assert.assertLong(maxValud)).toBe(maxValud);

      const bellowMin = parseBigNum(LONG_MIN_VALUE).minus(1).toString();
      const aboveMax = parseBigNum(LONG_MAX_VALUE).plus(1).toString();
      expect(() => Assert.assertLong(bellowMin)).toThrowErrorMatchingSnapshot();
      expect(() => Assert.assertLong(aboveMax)).toThrowErrorMatchingSnapshot();
    });
  });

  describe('assertShort', () => {
    it('should pass through valid shorts', () => {
      const valids = [-10, 0, 235] as Short[];
      for (const valid of valids) {
        expect(Assert.assertShort(valid)).toBe(valid);
      }
    });

    it('should explode on non-shorts', () => {
      const invalids = ['bla', null, 1.55];
      for (const invalid of invalids) {
        expect(() => Assert.assertShort(invalid as any)).toThrowErrorMatchingSnapshot();
      }
    });

    it('should assert valid Short range', () => {
      const inRange = [SHORT_MIN_VALUE, SHORT_MAX_VALUE] as Short[];
      const outOfRange = [SHORT_MIN_VALUE - 1, SHORT_MAX_VALUE + 1];

      for (const ok of inRange) {
        expect(Assert.assertShort(ok)).toBe(ok);
      }

      for (const bad of outOfRange) {
        expect(() => Assert.assertShort(bad as any)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('serializeBinary', () => {
    it('should encode valid strings', () => {
      expect(Assert.serializeBinary('test' as BinaryStr)).toEqual('dGVzdA==');
    });

    it('should explode on non-strings', () => {
      const invalids = [null, 2, {}] as any[];

      for (const invalid of invalids) {
        expect(() => Assert.serializeBinary(invalid)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('deserializeBinary', () => {
    it('should decode valid strings', () => {
      expect(Assert.deserializeBinary('dGVzdA==' as BinaryStr)).toEqual('test');
    });

    it('should explode on non-strings', () => {
      const invalids = [null, 2, {}] as any[];

      for (const invalid of invalids) {
        expect(() => Assert.deserializeBinary(invalid)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('assertArray', () => {
    it('should pass through arrays', () => {
      const arrays = [
        [],
        [1, 2, 3],
        ['bla', 'bli', 'blu'], // Thanks, Pauline
      ];
      for (const array of arrays) {
        expect(() => Assert.assertArray<any>(array)).not.toThrow();
        expect(Assert.assertArray<any>(array)).toEqual(array);
      }
    });

    it('should explode on non-arrays', () => {
      const badApples = [
        null,
        {},
        new Set([]),
      ];

      for (const badApple of badApples) {
        expect(() => Assert.assertArray<any>(badApple as any)).toThrow();
        expect(() => Assert.assertArray<any>(badApple as any)).toThrowErrorMatchingSnapshot();
      }
    });

    it('should make a copy of the original value', () => {
      const original = [1, 2, 3];
      const copy = Assert.assertArray(original);
      expect(original).toEqual(copy);
      original.push(4);
      expect(original).not.toEqual(copy);
      expect(original).toHaveLength(4);
      expect(copy).toHaveLength(3);
    });
  });

  describe('serializeSet', () => {
    it('should serialize sets into arrays', () => {
      expect(Assert.serializeSet(new Set([]))).toEqual([]);
      expect(Assert.serializeSet(new Set([1, 2, 3]))).toEqual([1, 2, 3]);
      expect(Assert.serializeSet(new Set(['bla', 'bli', 'blu']))).toEqual(['bla', 'bli', 'blu']);
    });

    it('should explode on non-sets', () => {
      const badApples = [
        null,
        {},
        [],
      ];

      for (const badApple of badApples) {
        expect(() => Assert.serializeSet<any>(badApple as any)).toThrow();
        expect(() => Assert.serializeSet<any>(badApple as any)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('deserializeSet', () => {
    it('should deserialize arrays into sets', () => {
      expect(Assert.deserializeSet([])).toEqual(new Set([]));
      expect(Assert.deserializeSet([1, 2, 3])).toEqual(new Set([1, 2, 3]));
      expect(Assert.deserializeSet(['bla', 'bli', 'blu'])).toEqual(new Set(['bla', 'bli', 'blu']));
    });

    it('should explode on non-arrays', () => {
      const badApples = [
        null,
        {},
        new Set([]),
      ];

      for (const badApple of badApples) {
        expect(() => Assert.deserializeSet<any>(badApple as any)).toThrow();
        expect(() => Assert.deserializeSet<any>(badApple as any)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('serializeMap', () => {
    it('should serialize maps into lookups', () => {
      const strMap = new Map();
      strMap.set('a', 'test');
      strMap.set('b', 'test2');

      const intMap = new Map();
      intMap.set(1, 'test');
      intMap.set(2, 'test2');

      expect(Assert.serializeMap<any, any>(new Map())).toEqual({});
      expect(Assert.serializeMap<any, any>(strMap)).toEqual({
        a: 'test',
        b: 'test2',
      });
      expect(Assert.serializeMap<any, any>(intMap)).toEqual({
        [1]: 'test',
        [2]: 'test2',
      });
    });

    it('should explode on non-maps', () => {
      const badApples = [
        null,
        [],
      ];

      for (const badApple of badApples) {
        expect(() => Assert.serializeMap<any, any>(badApple as any)).toThrow();
        expect(() => Assert.serializeMap<any, any>(badApple as any)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('deserializeMap', () => {
    it('should deserialize lookups into maps', () => {
      const strMap = new Map();
      strMap.set('a', 'test');
      strMap.set('b', 'test2');

      expect(Assert.deserializeMap({})).toEqual(new Map());
      expect(Assert.deserializeMap({
        a: 'test',
        b: 'test2',
      })).toEqual(strMap);
    });

    it('should explode on non-lookups', () => {
      const badApples = [
        null,
        [],
        new Map(),
        new Set([]),
        () => { return; },
      ];

      for (const badApple of badApples) {
        expect(() => Assert.deserializeMap(badApple as any)).toThrow();
        expect(() => Assert.deserializeMap(badApple as any)).toThrowErrorMatchingSnapshot();
      }
    });
  });

  describe('serializeResultSet', () => {
    const rsOverTheWire = [['a', 'test'], ['String', 'String'], ['test', 'test'], ['test1', 'test1']];
    const rs = new TypescriptResultSet(['a', 'test'], [ColumnType.String, ColumnType.String], [['test', 'test'], ['test1', 'test1']]);

    it('should turn it into an array of objects', () => {
      expect(() => Assert.serializeResultSet(rs)).not.toThrow();
      expect(Assert.serializeResultSet(rs)).toEqual(rsOverTheWire);
    });

    it('should explode on invalid shapes', () => {
      const badApples: any[] = [
        {},
        [{}],
        [[]],
      ];

      for (const badApple of badApples) {
        expect(() => Assert.serializeResultSet(badApple)).toThrow();
        expect(() => Assert.serializeResultSet(badApple)).toThrowErrorMatchingSnapshot();
      }

    });
  });

  describe('deserializeResultSet', () => {
    const rsOverTheWire = [['a', 'test'], ['String', 'String'], ['test', 'test'], ['test1', 'test1']];
    const rs = new TypescriptResultSet(['a', 'test'], [ColumnType.String, ColumnType.String], [['test', 'test'], ['test1', 'test1']]);

    it('should turn it into an array of objects', () => {
      expect(() => Assert.deserializeResultSet(rsOverTheWire)).not.toThrow();
      expect(Assert.deserializeResultSet(rsOverTheWire)).toEqual(rs);
    });

    it('should explode on invalid shapes', () => {
      const badApples: any[] = [
        {},
        [new Map()],
        [[]],
      ];

      for (const badApple of badApples) {
        expect(() => Assert.deserializeResultSet(badApple)).toThrow();
        expect(() => Assert.deserializeResultSet(badApple)).toThrowErrorMatchingSnapshot();
      }

    });
  });

  describe('assertEnum', () => {
    it('should test that the value is one of the provided values', () => {
      const enumChecker = Assert.assertEnum(['Active', 'Inactive'], 'Status');

      expect(() => enumChecker('Active', true, '')).not.toThrow();
      expect(() => enumChecker('Inactive', true, '')).not.toThrow();
      expect(() => enumChecker(undefined as any, false, '')).not.toThrow();
      expect(() => enumChecker('Other', true, '')).toThrowErrorMatchingSnapshot();
      expect(() => enumChecker('INACTIVE', true, '')).toThrowErrorMatchingSnapshot();
      expect(() => enumChecker(undefined as any, true, '')).toThrowErrorMatchingSnapshot();
    });

    it('should provide the passed in type name on failure', () => {
      const enumChecker = Assert.assertEnum(['Active', 'Inactive'], 'Status');
      expect(() => enumChecker('Other', true, '')).toThrowError('is not a valid member of enumeration Status');
    });
  });

  describe('assertS3', () => {
    it('should produce a correct json formated string from an S3 value', () => {
      const s3 = {
        bucket: 'test',
        key: '58d33650-1f54-4190-b9f6-f85d0b889350',
        length: '1024',
        metadata: {
          type: 'personal document',
        },
      };
      expect(() => Assert.assertS3(s3)).not.toThrow();
      expect(Assert.assertS3(s3)).toEqual(s3);
      expect(Assert.assertS3(Assert.assertS3(s3))).toEqual(s3);
    });

    it('should throw if bad S3 value is provided', () => {
      const badS3 = {
        bucket: 'test',
        key: '58d33650-1f54-4190-b9f6-f85d0b889350',
        length: 'potato',
        metadata: {
          type: 'personal document',
        },
      };
      expect(() => Assert.assertS3(badS3 as any)).toThrowErrorMatchingSnapshot();
      expect(() => Assert.assertS3('{"key":"58d33650-1f54-4190-b9f6-f85d0b889350","length":1024,"metadata":{"type":"personal document"}}')).toThrowErrorMatchingSnapshot();
    });

    it('should throw if bad S3 value is provided which has missing properties', () => {
      const badS3 = {
        bucket: 'test',
        length: 'potato',
        metadata: {
          type: 'personal document',
        },
      };
      expect(() => Assert.assertS3(badS3 as any)).toThrowErrorMatchingSnapshot();
      expect(() => Assert.assertS3('{"length":1024,"metadata":{"type":"personal document"}}')).toThrowErrorMatchingSnapshot();
    });
  });
});
