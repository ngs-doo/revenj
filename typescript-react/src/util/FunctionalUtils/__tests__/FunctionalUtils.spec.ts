// tslint:disable:rule no-construct
import {
  concatUnique,
  deduplicateBy,
  deepKeys,
  ensureSet,
  flatten,
  flattenName,
  flatMap,
  get,
  groupBy,
  isEmpty,
  isEqual,
  moveFromToIndex,
  objectDiff,
  omit,
  pick,
  set,
  squashObject,
  takeWhile,
  valueOrNull,
} from '../FunctionalUtils';

interface IThing {
  firstName: string;
  middleName?: string;
  lastName: string;
  dogs: string[];
}

const person: IThing = {
  dogs: ['woof'],
  firstName: 'Bobo',
  lastName: 'Bobic',
};

const cloneOfAPerson: IThing = {...person};
// import { deduplicateBy, deepKeys, isEmpty, objectDiff } from '../functional';

describe('functional.ts', () => {
  describe('deepKeys', () => {
    it('should work like Object.keys for shallow objects', () => {
      const obj = {
        a: 1,
        b: '2',
        c: false,
      };

      expect(Object.keys(obj)).toEqual(deepKeys(obj));
    });

    it('should fetch key paths in a nested object', () => {
      const obj = {
        a: {
          b: 'ok',
          c: {
            d: 3,
          },
        },
        e: false,
      };

      const expectedPaths = ['a.b', 'a.c.d', 'e'];

      expect(deepKeys(obj).sort()).toEqual(expectedPaths.sort());
    });

    it('should not produce deep paths for arrays (indices)', () => {
      const obj = {
        a: {
          b: ['1', '5'],
          c: {
            d: 3,
          },
        },
        e: false,
      };

      const expectedPaths = ['a.b', 'a.c.d', 'e'];

      expect(deepKeys(obj).sort()).toEqual(expectedPaths.sort());
    });

    it('should dive into arrays if the flag is set to do so', () => {
      const obj = {
        a: [1, 2, 3],
        b: [
          { c: 1 },
          { d: [0]},
        ],
        c: [],
      };

      const expectedPaths = ['a[0]', 'a[1]', 'a[2]', 'b[0].c', 'b[1].d[0]'];
      expect(deepKeys(obj, true).sort()).toEqual(expectedPaths.sort());
    });
  });

  describe('objectDiff', () => {
    it('should produce an object describing the difference between two objects with bool-per-key', () => {
      const a = {
        a: 1,
        b: 2,
        c: 3,
        d: 3,
      };

      const b = {
        a: 1,
        b: 5,
        c: 3.1,
        e: 3,
      };

      const expectedDiff = {
        a: false,
        b: true,
        c: true,
        d: true,
        e: true,
      };

      expect(objectDiff(a, b)).toEqual(expectedDiff);
    });

    it('should work for deep-nested objects', () => {
      const a = {
        deposit: {
          original: '124',
        },
        loan: {
          first: '123',
          second: '432',
        },
      };

      const b = {
        deposit: {
          copy: '124',
        },
        loan: {
          first: '125',
          second: '432',
        },
      };

      const expectedDiff = {
        deposit: {
          copy: true,
          original: true,
        },
        loan: {
          first: true,
          second: false,
        },
      };

      expect(objectDiff(a, b)).toEqual(expectedDiff);
    });

    it('should declare everything to be different when given only one object', () => {
      const a = {
        a: {
          b: 1,
        },
        c: 3,
      };

      const expectedDiff = {
        a: {
          b: true,
        },
        c: true,
      };

      expect(objectDiff(a)).toEqual(expectedDiff);
    });
  });

  describe('deduplicateBy', () => {
    const items = [
      {
        id: 1,
        name: 'One',
      },
      {
        id: 2,
        name: 'Two',
      },
      {
        id: 1,
        name: 'Ein',
      },
      {
        id: 3,
        name: 'Three',
      },
      {
        id: 1,
        name: 'Ett',
      },
    ];

    it('should find unique values in an array of objects by a property name', () => {
      const deduped = deduplicateBy('id', items);
      expect(deduped.length).toBe(3);
    });

    it('should take the first item for duplicate key values', () => {
      const deduped = deduplicateBy('id', items);
      expect(deduped.find((item) => item.id === 1)!.name).toBe('One');
      expect(deduped.find((item) => item.id === 2)!.name).toBe('Two');
      expect(deduped.find((item) => item.id === 3)!.name).toBe('Three');
    });

    it('should do nothing if there are no duplicates', () => {
      expect(deduplicateBy('name', items)).toEqual(items);
    });

    it('should return an empty array when given an empty array', () => {
      expect(deduplicateBy('id', [])).toEqual([]);
    });

    it('should return only one item if all elements have the same key value', () => {
      const duplicateItems = [
        { id: 1, name: 'One' },
        { id: 1, name: 'Duplicate One' },
        { id: 1, name: 'Duplicate Two' },
      ];
      const deduped = deduplicateBy('id', duplicateItems);
      expect(deduped.length).toBe(1);
      expect(deduped[0].name).toBe('One');
    });
  });

  describe('flatten', () => {
    it('should take an array of arrays and make a one level flatter array out of it', () => {
      expect(flatten([])).toEqual([]);
      expect(flatten([[1]])).toEqual([1]);
      expect(flatten([[1], [2], [3]])).toEqual([1, 2, 3]);
      expect(flatten([[1], [2, 3]])).toEqual([1, 2, 3]);
    });
  });

  describe('flatMap', () => {
    it('should take an array and a function that transforms each element in an array, and return a flattened array of results', () => {
      const doubler = <T>(it: T) => [it, it];
      expect(flatMap([], doubler)).toEqual([]);
      expect(flatMap([1], doubler)).toEqual([1, 1]);
      expect(flatMap([1, 3], doubler)).toEqual([1, 1, 3, 3]);
    });
  });

  describe('omit', () => {
    it('should not modify the original', () => {
      expect(person).toEqual(cloneOfAPerson);
      omit(person);
      expect(person).toEqual(cloneOfAPerson);
    });

    it('should return an object without values for the provided keys', () => {
      expect(omit({...person})).toEqual(person);
      expect(omit({...person}, 'dogs')).toEqual({
        firstName: person.firstName,
        lastName: person.lastName,
      });
      expect(omit({...person}, 'dogs', 'firstName')).toEqual({
        lastName: person.lastName,
      });
      expect(omit({...person}, 'dogs', 'firstName', 'lastName')).toEqual({});
    });

    it('should keep the value undefined if it is not set', () => {
      expect(omit({...person}, 'middleName')).toEqual(person);
    });
  });

  describe('pick', () => {
    it('should not modify the original', () => {
      expect(person).toEqual(cloneOfAPerson);
      pick(person);
      expect(person).toEqual(cloneOfAPerson);
    });

    it('should return an object with only values for the provided keys', () => {
      expect(pick({...person})).toEqual({});
      expect(pick({...person}, 'dogs')).toEqual({
        dogs: person.dogs,
      });
      expect(pick({...person}, 'dogs', 'firstName')).toEqual({
        dogs: person.dogs,
        firstName: person.firstName,
      });
      expect(pick({...person}, 'dogs', 'firstName', 'lastName')).toEqual(person);
    });

    it('should keep the value undefined if it is not set', () => {
      expect(pick({...person}, 'middleName')).toEqual({});
    });
  });

  describe('isEmpty', () => {
    it('should say strings are empty if they are length 0', () => {
      expect(isEmpty('')).toBe(true);
      expect(isEmpty(' ')).toBe(false);
      expect(isEmpty('asdadas')).toBe(false);
    });

    it('should say arrays are empty if they are length 0', () => {
      expect(isEmpty([])).toBe(true);
      expect(isEmpty([null])).toBe(false);
      expect(isEmpty([1, 2])).toBe(false);
    });

    it('should say objects are empty if they have no non-nully keys', () => {
      expect(isEmpty({})).toBe(true);
      expect(isEmpty({ bla: undefined })).toBe(true);
      expect(isEmpty({ bla: null })).toBe(true);
      expect(isEmpty({ bla: 1 })).toBe(false);
    });

    it('should say number are not empty', () => {
      expect(isEmpty(0)).toBe(false);
      expect(isEmpty(-123)).toBe(false);
      expect(isEmpty(123)).toBe(false);
    });

    it('should say everything else is empty', () => {
      expect(isEmpty(null)).toBe(true);
      expect(isEmpty(false)).toBe(true);
      expect(isEmpty(true)).toBe(true);
    });
  });

  describe('groupBy', () => {
    it('should collect objects into groups keyed by the return value for the function', () => {
      const grouper = <T extends { name: string }>(it: T) => it.name;
      const a = { name: 'foo' };
      const b = { name: 'bar' };
      const c = { name: 'baz' };
      const d = { name: 'foo' };
      expect(groupBy([], grouper)).toEqual({});
      expect(groupBy([a], grouper)).toEqual({ foo: [a] });
      expect(groupBy([a, b], grouper)).toEqual({ foo: [a], bar: [b] });
      expect(groupBy([a, b, c], grouper)).toEqual({ foo: [a], bar: [b], baz: [c] });
      expect(groupBy([a, b, c, d], grouper)).toEqual({ foo: [a, d], bar: [b], baz: [c] });
    });
  });

  describe('isEqual', () => {
    it('should work like === for simple values', () => {
      expect(isEqual(1, 1)).toBe(true);
      expect(isEqual(1, 2)).toBe(false);
      expect(isEqual(null, null)).toBe(true);
      expect(isEqual(null, undefined)).toBe(false);
      expect(isEqual('x', 'x')).toBe(true);
      expect(isEqual('x', 'y')).toBe(false);
    });

    it('should compare arrays by members, in order', () => {
      expect(isEqual([], [])).toBe(true);
      expect(isEqual([1], [])).toBe(false);
      expect(isEqual([1], [1])).toBe(true);
      expect(isEqual([1, 2], [1])).toBe(false);
      expect(isEqual([1, 2], [1, 2])).toBe(true);
      expect(isEqual([1, 2], [2, 1])).toBe(false);
    });

    it('should compare sets by values irrespective of order in construction', () => {
      expect(isEqual(new Set(), new Set())).toBe(true);
      expect(isEqual(new Set([1, 2]), new Set([2, 1]))).toBe(true);
      expect(isEqual(new Set([1]), new Set())).toBe(false);
      expect(isEqual(new Set([1, 2, 3]), new Set(['1', '2', '3'] as any))).toBe(false);
    });

    it('should deep compare objects, key order should not matter', () => {
      expect(isEqual({}, {})).toBe(true);
      expect(isEqual({ a: 1 }, {})).toBe(false);
      expect(isEqual({ a: 1 }, { a: 1 })).toBe(true);
      expect(isEqual({ a: 1 }, { a: 2 })).toBe(false);
      expect(isEqual({ a: 1, b: 2 }, { a: 1 })).toBe(false);
      expect(isEqual({ a: 1, b: 2 }, { b: 2, a: 1 })).toBe(true);
    });

    it('should work for nested stuff', () => {
      expect(isEqual({
        a: { b: 1 },
      }, {
        a: { b: 1 },
      })).toBe(true);
      expect(isEqual({
        a: { b: 1 },
      }, {
        a: { b: 2 },
      })).toBe(false);
      expect(isEqual({
        a: { b: [1] },
      }, {
        a: { b: [1] },
      })).toBe(true);
      expect(isEqual({
        a: { b: [{ c: 1 }] },
      }, {
        a: { b: [{ c: 1 }] },
      })).toBe(true);
    });

    it('should work for Lodash tests', () => {
      // Basic equality and identity comparisons.
      expect(isEqual(null, null)).toBeTruthy();

      expect(!isEqual(null, void 0)).toBeTruthy();
      expect(!isEqual(void 0, null)).toBeTruthy();

      // String object and primitive comparisons.
      expect(isEqual('Curly', 'Curly')).toBeTruthy();
      expect(isEqual(new String('Curly'), 'Curly')).toBeTruthy();
      expect(isEqual('Curly', new String('Curly'))).toBeTruthy();

      expect(!isEqual('Curly', 'Larry')).toBeTruthy();
      expect(!isEqual(new String('Curly'), new String('Larry'))).toBeTruthy();
      expect(!isEqual(new String('Curly'), {toString: () => 'Curly'} as any)).toBeTruthy();

      // Number object and primitive comparisons.
      expect(isEqual(75, 75)).toBeTruthy();
      expect(isEqual(new Number(75), new Number(75))).toBeTruthy();
      expect(isEqual(75, new Number(75))).toBeTruthy();
      expect(isEqual(new Number(75), 75)).toBeTruthy();

      expect(!isEqual(new Number(75), new Number(63))).toBeTruthy();
      expect(!isEqual(new Number(63), {valueOf: () => 63 } as any)).toBeTruthy();

      // Comparisons involving `NaN`.
      expect(isEqual(NaN, NaN)).toBeTruthy();
      expect(isEqual(new Number(NaN), NaN)).toBeTruthy();
      expect(!isEqual(61, NaN)).toBeTruthy();
      expect(!isEqual(new Number(79), NaN)).toBeTruthy();
      expect(!isEqual(Infinity, NaN)).toBeTruthy();

      // Boolean object and primitive comparisons.
      expect(isEqual(true, true)).toBeTruthy();
      expect(isEqual(new Boolean(), new Boolean())).toBeTruthy();
      expect(isEqual(true, new Boolean(true))).toBeTruthy();
      expect(isEqual(new Boolean(true), true)).toBeTruthy();

      // Common type coercions.
      expect(!isEqual(new Boolean(false), true)).toBeTruthy();
      expect(!isEqual('75', 75 as unknown as string)).toBeTruthy();
      expect(!isEqual(new Number(63), new String(63) as unknown as number)).toBeTruthy();
      expect(!isEqual(75, '75' as unknown as number)).toBeTruthy();
      expect(!isEqual(0, '' as unknown as number)).toBeTruthy();
      expect(!isEqual(1, true as unknown as number)).toBeTruthy();
      expect(!isEqual(new Boolean(false), new Number(0) as unknown as boolean)).toBeTruthy();
      expect(!isEqual(false, new String('') as unknown as boolean)).toBeTruthy();
      expect(!isEqual(12564504e5, new Date(2009, 9, 25) as unknown as number)).toBeTruthy();

      // Dates.
      expect(!isEqual(new Date(2009, 9, 25), new Date(2009, 11, 13))).toBeTruthy();
      expect(!isEqual(new Date(2009, 11, 13), {
        getTime: () => 12606876e5,
      } as any)).toBeTruthy();
      expect(!isEqual(new Date('Curly'), new Date('Curly'))).toBeTruthy();
    });
  });

  describe('get', () => {
    it('should work like lodash get', () => {
      expect(get({ a: 1 }, 'a')).toBe(1);
      expect(get({ a: { b: 2 } }, 'a.b' as any)).toBe(2);
      expect(get({ a: { b: 3 } }, 'a.c' as any)).toBeUndefined();
      expect(get({ a: { b: 4 } }, 'a.c.f' as any)).toBeUndefined();
      expect(get({ a: { b: 5 } }, 'a.c.f' as any, 3)).toBe(3);
      expect(get({ a: { b: 6 } }, ['a', 'c', 'f'] as any, 3)).toBe(3);
      expect(get({
        a: [{ b : 7}],
      }, 'a[0].b' as any)).toBe(7);
      expect(get({
        a: [{ b : 1}],
      }, 'a[1].b' as any)).toBeUndefined();
      expect(get({ a: ['a'] }, 'a[0]' as any)).toBe('a');
      expect(get({
        a: [{ 'b c' : 8}],
      }, 'a[0][b c]' as any)).toBe(8);
      expect(get({
        a: [{ 'b c' : 9}],
      }, ['a', '0', 'b c'] as any)).toBe(9);
    });
    expect(get(new Map([['a', 1]]), 'a' as any)).toBe(1);
    expect(get(new Map([['a', false]]), 'a' as any)).toBe(false);
  });

  describe('set', () => {
    it('should work like lodash set', () => {
      expect(set({}, 'a' as any, 1)).toEqual({ a: 1 });
      expect(set({}, 'a.b' as any, 1)).toEqual({ a: { b: 1 } });
      expect(set({}, 'a[0]' as any, 1)).toEqual({ a: [1] });
      expect(set({}, ['a', '0'] as any, 1)).toEqual({ a: [1] });
      expect(set({}, 'a[foo]' as any, 1)).toEqual({ a: { foo: 1 } });
      expect(set({}, 'a[foo bar]' as any, 1)).toEqual({ a: { 'foo bar': 1 } });
      expect(set({}, ['a', 'foo'] as any, 1)).toEqual({ a: { foo: 1 } });
      expect(set({
        a: [
          { b: 1 },
        ],
      }, 'a[0].b' as any, 2)).toEqual({
        a: [
          { b: 2 },
        ],
      });
    });
  });

  describe('valueOrNull', () => {
    it('should return nullonly for  null, undefined, empty objects, empty arrays and empty strings', () => {
      expect(valueOrNull(null)).toBeNull();
      expect(valueOrNull(undefined)).toBeNull();
      expect(valueOrNull([])).toBeNull();
      expect(valueOrNull({})).toBeNull();
      expect(valueOrNull('')).toBeNull();

      expect(valueOrNull(0)).toBe(0);
      expect(valueOrNull(false)).toBe(false);
      expect(valueOrNull(true)).toBe(true);
      expect(valueOrNull('0')).toBe('0');
      expect(valueOrNull([0])).toEqual([0]);
      expect(valueOrNull({ a: 0 })).toEqual({ a: 0 });
    });
  });

  describe('takeWhile', () => {
    it('should take values as long as the predicate is satisfied', () => {
      expect(takeWhile([1, 2, 3, 4], (x) => x < 10)).toEqual([1, 2, 3, 4]);
      expect(takeWhile([1, 2, 10, 4], (x) => x < 10)).toEqual([1, 2]);
      expect(takeWhile([1, 12, 3, 4], (x) => x > 10)).toEqual([]);
      expect(takeWhile([], () => true)).toEqual([]);
    });
  });

  describe('squashObject', () => {
    it('should return undefined for null-y values', () => {
      expect(squashObject(null)).toBe(undefined);
      expect(squashObject(undefined)).toBe(undefined);
    });

    it('should return undefined for empty objects and objects with no values defined', () => {
      expect(squashObject({})).toBe(undefined);
      expect(squashObject({
        key: null,
      })).toBe(undefined);
      expect(squashObject({
        key: undefined,
      })).toBe(undefined);
    });

    it('should return the object in full if it has any values', () => {
      const objs = [
        {
          key: false,
        },
        {
          key: false,
          other: undefined,
        },
      ];
      objs.forEach((o) => {
        expect(squashObject(o)).toEqual(o);
      });
    });
  });

  describe('ensureSet', () => {
    it('should return a Set object if an array, object or primitive value are passed', () => {
      const arr = [1, 2, 3];
      const obj = { a: 1};

      expect(ensureSet(arr)).toBeInstanceOf(Set);
      expect(ensureSet(obj)).toBeInstanceOf(Set);
      expect(ensureSet('i am a string' as any)).toBeInstanceOf(Set);
    });

    it('should return a filled Set object with correct values if a non-empty array is passed', () => {
      const arr = [1, 2, 3];
      const setObj = ensureSet(arr);

      expect(setObj.has(1)).toBe(true);
      expect(setObj.has(2)).toBe(true);
      expect(setObj.has(3)).toBe(true);
      expect(setObj.has(6)).toBe(false);
    });
  });

  describe('concatUnique', () => {
    it('should concatenate arrays of arrays', () => {
      expect(concatUnique([[1, 2], [3], [4, 5]])).toEqual([1, 2, 3, 4, 5]);
      expect(concatUnique([[1, 2], [], [4]])).toEqual([1, 2, 4]);
    });

    it('should keep only keep the first occurrence of a unique value', () => {
      expect(concatUnique([[1, 2], [1, 3]])).toEqual([1, 2, 3]);
      expect(concatUnique([[1, 2], [1, 3, 1]])).toEqual([1, 2, 3]);
      expect(concatUnique([['a'], ['a']])).toEqual(['a']);
      expect(concatUnique([[{}], [{}]])).toEqual([{}]);
    });
  });

  describe('[Regression](https://oradian.atlassian.net/browse/IN-12151)', () => {
    it('should correctly check for equality of objects', () => {
      const first = [
        { id: 1, displayName: 'Male', active: true },
        { id: 2, displayName: 'Female', active: true },
      ];

      const second = [
        { id: 1, displayName: 'Male', active: true },
        { id: 2, displayName: 'Female', active: true },
      ];

      const expected = [
        { id: 1, displayName: 'Male', active: true },
        { id: 2, displayName: 'Female', active: true },
      ];

      expect(concatUnique([first, second])).toEqual(expected);
    });
  });

  describe('moveFromToIndex', () => {
    const xs = [1, 2, 3, 4, 5];

    it('should move an item at one index to the other, pushing others back', () => {
      expect(moveFromToIndex(xs, 0, 1)).toEqual([2, 1, 3, 4, 5]);
      expect(moveFromToIndex(xs, 2, 1)).toEqual([1, 3, 2, 4, 5]);
      expect(moveFromToIndex(xs, 4, 1)).toEqual([1, 5, 2, 3, 4]);
    });

    it('should work inserting at the zeroth index', () => {
      expect(moveFromToIndex(xs, 2, 0)).toEqual([3, 1, 2, 4, 5]);
    });

    it('should work for inserting at the last index', () => {
      expect(moveFromToIndex(xs, 1, 4)).toEqual([1, 3, 4, 5, 2]);
      expect(moveFromToIndex(xs, 3, 4)).toEqual([1, 2, 3, 5, 4]);
    });

    it('should return the same array when using the same index both times', () => {
      expect(moveFromToIndex(xs, 0, 0)).toEqual([1, 2, 3, 4, 5]);
      expect(moveFromToIndex(xs, 3, 3)).toEqual([1, 2, 3, 4, 5]);
    });

    it('should return the same array when an index is negative or out of bounds', () => {
      expect(moveFromToIndex(xs, -1, 2)).toEqual([1, 2, 3, 4, 5]);
      expect(moveFromToIndex(xs, 1, -2)).toEqual([1, 2, 3, 4, 5]);
      expect(moveFromToIndex(xs, 1, 10)).toEqual([1, 2, 3, 4, 5]);
      expect(moveFromToIndex(xs, 10, 1)).toEqual([1, 2, 3, 4, 5]);
      expect(moveFromToIndex(xs, -1, 10)).toEqual([1, 2, 3, 4, 5]);
    });
  });

  describe('flattenName', () => {
    it('should return the same string as the one provided', () => {
      expect(flattenName('name')).toEqual('name');
      expect(flattenName('prefix.name')).toEqual('prefix.name');
    });

    it('should flatten the name if an array is provided', () => {
      expect(flattenName([])).toEqual('');
      expect(flattenName(['first'])).toEqual('first');
      expect(flattenName(['first', 'second'])).toEqual('first.second');
      expect(flattenName(['first', 'second', 'third'])).toEqual('first.second.third');
    });

    it('should ignore undefined values and empty strings', () => {
      expect(flattenName('')).toEqual('');
      expect(flattenName(' ')).toEqual('');
      expect(flattenName(null)).toEqual('');
      expect(flattenName(undefined)).toEqual('');
      expect(flattenName([undefined, 'value', null, '', ' '])).toEqual('value');
    });
  });
});
