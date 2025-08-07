import { Marshaller } from '../Marshaller';
import { initialize } from '../boot';
import {
  OptionalFields,
  Person,
} from '../__mocks__/mocks';

const marshaller = Marshaller.getInstance();

describe('Instafin marshalling', () => {
  beforeAll(() => {
    initialize({});
  });

  it('should deserialize simple types', () => {
    expect(marshaller.deserialize(10, 'Int', true, 'test')).toEqual(10);
    expect(marshaller.deserialize(10, 'Long', true, 'test')).toEqual(10);
    expect(marshaller.serialize('9007199254740995', 'Long', true, 'test')).toEqual('9007199254740995');
    expect(marshaller.deserialize(10.5, 'Double', true, 'test')).toEqual(10.5);
    expect(marshaller.deserialize(10.5, 'Float', true, 'test')).toEqual(10.5);
    expect(marshaller.deserialize('10.5', 'Decimal', true, 'test')).toEqual('10.5');
    expect(marshaller.deserialize('1000', 'Money', true, 'test')).toEqual('1000.00');
    expect(marshaller.deserialize('2000-01-01T20:00:00', 'Date', true, 'test')).toEqual('2000-01-01');
    expect(marshaller.deserialize('2000-01-01T20:00:00', 'Timestamp', true, 'test')).toEqual('2000-01-01T20:00:00.000+00:00');
    expect(marshaller.deserialize('dGVzdA==', 'Binary', true, 'test')).toEqual('test');
  });

  it('should serialize simple types', () => {
    expect(marshaller.serialize(10, 'Int', true, 'test')).toEqual(10);
    expect(marshaller.serialize(10, 'Long', true, 'test')).toEqual(10);
    expect(marshaller.serialize('9007199254740995', 'Long', true, 'test')).toEqual('9007199254740995');
    expect(marshaller.serialize(10.5, 'Double', true, 'test')).toEqual(10.5);
    expect(marshaller.deserialize(10.5, 'Float', true, 'test')).toEqual(10.5);
    expect(marshaller.serialize('10.5', 'Decimal', true, 'test')).toEqual('10.5');
    expect(marshaller.serialize('1000', 'Money', true, 'test')).toEqual('1000.00');
    expect(marshaller.serialize('2000-01-01T20:00:00', 'Date', true, 'test')).toEqual('2000-01-01');
    expect(marshaller.serialize('2000-01-01T20:00:00', 'Timestamp', true, 'test')).toEqual('2000-01-01T20:00:00.000+00:00');
    expect(marshaller.serialize('test', 'Binary', true, 'test')).toEqual('dGVzdA==');
  });

  it('should serialize nullable simple types when sending an empty string', () => {
    expect(marshaller.serialize('', 'Int', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Long', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Double', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Float', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Decimal', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Money', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Date', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Timestamp', false, 'test')).toEqual(undefined);
    expect(marshaller.serialize('', 'Binary', false, 'test')).toEqual(undefined);
  });

  it('should deserialize nullable simple types when sending an empty string', () => {
    expect(marshaller.deserialize('', 'Int', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Long', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Double', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Float', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Decimal', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Money', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Date', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Timestamp', false, 'test')).toEqual(undefined);
    expect(marshaller.deserialize('', 'Binary', false, 'test')).toEqual(undefined);
  });

  it('should default to 0 when serializing non-nullable numbers', () => {
    expect(marshaller.serialize(undefined, 'Int', true, 'test')).toEqual(0);
    expect(marshaller.serialize(undefined, 'Long', true, 'test')).toEqual(0);
    expect(marshaller.serialize(undefined, 'Double', true, 'test')).toEqual(0);
    expect(marshaller.serialize(undefined, 'Float', true, 'test')).toEqual(0);
    expect(marshaller.serialize(undefined, 'Decimal', true, 'test')).toEqual('0');
    expect(marshaller.serialize(undefined, 'Money', true, 'test')).toEqual('0.00');
  });

  it('should default to 0 when deserializing non-nullable numbers', () => {
    expect(marshaller.deserialize(undefined, 'Int', true, 'test')).toEqual(0);
    expect(marshaller.deserialize(undefined, 'Long', true, 'test')).toEqual(0);
    expect(marshaller.deserialize(undefined, 'Double', true, 'test')).toEqual(0);
    expect(marshaller.deserialize(undefined, 'Float', true, 'test')).toEqual(0);
    expect(marshaller.deserialize(undefined, 'Decimal', true, 'test')).toEqual('0');
    expect(marshaller.deserialize(undefined, 'Money', true, 'test')).toEqual('0.00');
  });

  it('should serialize Array, List, and Set to array', () => {
    expect(marshaller.serialize([1, 2, 3], 'Array', true, 'test')).toEqual([1, 2, 3]);
    expect(marshaller.serialize([1, 2, 3], 'List', true, 'test')).toEqual([1, 2, 3]);
    expect(marshaller.serialize(new Set([1, 2, 3]), 'Set', true, 'test')).toEqual([1, 2, 3]);
  });

  it('should serialize Array, List, and Set to their respective types', () => {
    expect(marshaller.deserialize([1, 2, 3], 'Array', true, 'test')).toEqual([1, 2, 3]);
    expect(marshaller.deserialize([1, 2, 3], 'List', true, 'test')).toEqual([1, 2, 3]);
    expect(marshaller.deserialize([1, 2, 3], 'Set', true, 'test')).toEqual(new Set([1, 2, 3]));
  });

  it('should accept a required boolean to be unset, behave as though it is false, but not actually send it to conserve space', () => {
    expect(marshaller.serialize(undefined, 'Boolean', true, 'test')).not.toBeDefined();
    expect(marshaller.serialize(undefined, 'Boolean', false, 'test')).not.toBeDefined();
    expect(marshaller.serialize(false, 'Boolean', false, 'test')).toEqual(false);
    expect(marshaller.serialize(false, 'Boolean', true, 'test')).not.toBeDefined();
    expect(marshaller.serialize(true, 'Boolean', false, 'test')).toEqual(true);
    expect(marshaller.serialize(true, 'Boolean', true, 'test')).toEqual(true);
  });

  it('should expand undefined collections into empty object iff they are required during serialization, to avoid boilerplate', () => {
    expect(marshaller.serialize(undefined, 'List', true, 'test')).toEqual([]);
    expect(marshaller.serialize(null, 'List', true, 'test')).toEqual([]);
    expect(marshaller.serialize(undefined, 'Array', true, 'test')).toEqual([]);
    expect(marshaller.serialize(null, 'Array', true, 'test')).toEqual([]);
    expect(marshaller.serialize(undefined, 'Streaming', true, 'test')).toEqual([]);
    expect(marshaller.serialize(null, 'Streaming', true, 'test')).toEqual([]);
    expect(marshaller.serialize(undefined, 'ResultSet', true, 'test')).toEqual([[], []]);
    expect(marshaller.serialize(null, 'ResultSet', true, 'test')).toEqual([[], []]);
    expect(marshaller.serialize(undefined, 'Set', true, 'test')).toEqual([]);
    expect(marshaller.serialize(null, 'Set', true, 'test')).toEqual([]);
  });

  it('should be possible to cleanly process collections of types', () => {
    const set = new Set(['100', '200']);
    const list = Array.from(set);

    // Serialization of set
    const serializedSet = Marshaller.Util.mapSeq(
      marshaller.serialize(set, 'Set', true, 'test')!,
      (it: any, path: string) => marshaller.serialize(it, 'Money', true, path),
      'test',
    );
    expect(serializedSet).toEqual(['100.00', '200.00']);

    // Deserialization of set
    const deserializedSet = marshaller.deserialize(
      Marshaller.Util.mapSeq(list, (it: any, path: string) => marshaller.deserialize(it, 'Money', true, path), 'test'),
      'Set',
      true,
      'test',
    );
    expect(deserializedSet).toEqual(new Set(['100.00', '200.00']));
  });

  it('should handle errors on collections of types', () => {
    const arr = [1, 'test', 3];

    const fn = () => Marshaller.Util.mapSeq(
      marshaller.serialize(arr, 'List', true, 'test'),
      (it: any, path: string) => marshaller.serialize(it, 'Int', true, path),
      'test',
    );

    expect(fn).toThrow();
    expect(fn).toThrowError('test[1]');
    expect(fn).toThrowError('Expected an Int');
  });

  it('should be possible to cleanly process maps', () => {
    const map = new Map();
    map.set('dueAmount', '100');
    const obj = {
      dueAmount: '100',
    };

    // Serializing a map
    const serializedMap = marshaller.serialize(Marshaller.Util.mapMap(
      map,
      (it: any, path: string) => marshaller.serialize(it, 'string', true, path),
      (it: any, path: string) => marshaller.serialize(it, 'Money', true, path),
      'test',
    ), 'Map', true, 'test');
    expect(serializedMap).toEqual({
      dueAmount: '100.00',
    });

    // Deserializing a map
    const deserializedMap = Marshaller.Util.mapMap(
      marshaller.deserialize<IObjectAny, Map<any, any>>(obj, 'Map', true, 'test')!,
      (it: any, path: string) => marshaller.deserialize(it, 'string', true, path),
      (it: any, path: string) => marshaller.deserialize(it, 'Money', true, path),
      'test',
    );
    const resultMap = new Map();
    resultMap.set('dueAmount', '100.00');
    expect(deserializedMap).toEqual(resultMap);
  });

  it('should handle errors on map keys', () => {
    const map = new Map();
    map.set('test', 1);

    const fn = () => marshaller.serialize(
      Marshaller.Util.mapMap(
        map,
        (it: any, path: string) => marshaller.serialize(it, 'Int', true, path),
        (it: any, path: string) => marshaller.serialize(it, 'Int', true, path),
        'test',
      ),
      'Map',
      true,
      'test',
    );

    expect(fn).toThrow();
    expect(fn).toThrowError('test.key');
    expect(fn).toThrowError('Expected an Int');
  });

  it('should handle errors on map values', () => {
    const map = new Map();
    map.set(1, 'test');

    const fn = () => marshaller.serialize(
      Marshaller.Util.mapMap(
        map,
        (it: any, path: string) => marshaller.serialize(it, 'Int', true, path),
        (it: any, path: string) => marshaller.serialize(it, 'Int', true, path),
        'test',
      ),
      'Map',
      true,
      'test',
    );

    expect(fn).toThrow();
    expect(fn).toThrowError('test.value');
    expect(fn).toThrowError('Expected an Int');
  });

  describe('more complex serialize (mocking DSL generation)', () => {
    it('should report shallow errors with the correct path', () => {
      const personWithMissingName = {
        tagIDs: [],
      };

      expect(() => Person.serialize(personWithMissingName as any, true)).toThrow();
      expect(() => Person.serialize(personWithMissingName as any, true)).toThrowError('name');
      expect(() => Person.serialize(personWithMissingName as any, true)).toThrowError('Value is required, but is not set');
      expect(() => Person.serialize(personWithMissingName as any, true)).toThrowErrorMatchingSnapshot();
    });

    it('should respect provided root labels', () => {
      const personWithMissingName = {
        tagIDs: [],
      };

      expect(() => Person.serialize(personWithMissingName as any, true, 'fakeroot')).toThrow();
      expect(() => Person.serialize(personWithMissingName as any, true, 'fakeroot')).toThrowError('fakeroot.name');
      expect(() => Person.serialize(personWithMissingName as any, true, 'fakeroot')).toThrowError('Value is required, but is not set');
      expect(() => Person.serialize(personWithMissingName as any, true, 'fakeroot')).toThrowErrorMatchingSnapshot();
    });

    it('should report nested errors with the correct path', () => {
      const personWithMissingPetName = {
        name: 'test',
        pets: new Set([{}]),
        tagIDs: [],
      };

      const personWithIncorrectTagType = {
        name: 'test',
        tagIDs: ['bla'],
      };

      expect(() => Person.serialize(personWithMissingPetName as any, true)).toThrow();
      expect(() => Person.serialize(personWithMissingPetName as any, true)).toThrowError('pets[0].name');
      expect(() => Person.serialize(personWithMissingPetName as any, true)).toThrowError('Value is required, but is not set');
      expect(() => Person.serialize(personWithMissingPetName as any, true)).toThrowErrorMatchingSnapshot();

      expect(() => Person.serialize(personWithIncorrectTagType as any, true)).toThrow();
      expect(() => Person.serialize(personWithIncorrectTagType as any, true)).toThrowError('tagIDs[0]');
      expect(() => Person.serialize(personWithIncorrectTagType as any, true)).toThrowError('Expected an Int');
      expect(() => Person.serialize(personWithIncorrectTagType as any, true)).toThrowErrorMatchingSnapshot();
    });

    it('should validate requiredness of root concept', () => {
      expect(() => Person.serialize(undefined, false)).not.toThrow();
      expect(Person.serialize(undefined, false)).toBeUndefined();
      expect(() => Person.serialize(undefined, true)).toThrow();
      expect(() => Person.serialize(undefined, true)).toThrowError('Value is required, but is not set');
      expect(() => Person.serialize(undefined, true)).toThrowErrorMatchingSnapshot();
    });

    it('should correctly serialise and deserialise', () => {
      const person: Person = {
        age: 30 as Int,
        name: 'Ivo',
        pets: new Set([
          { name: 'Fido' },
        ]),
        tagIDs: [22 as Int],
      };

      expect(() => Person.serialize(person, true)).not.toThrow();

      const serialized = Person.serialize(person, true)!;

      expect(serialized.age).toBe(30);
      expect(serialized.name).toBe('Ivo');
      expect(serialized.pets).toBeInstanceOf(Array);
      expect(serialized.pets).toEqual([ { name: 'Fido' }]);

      expect(() => Person.deserialize(serialized, true)).not.toThrow();
      expect(Person.deserialize(serialized, true)).toEqual(person);
    });
  });

  describe('nesting collections', () => {
    it('should be possible to nest as much as needed', () => {
      const nested: Array<Set<Array<Int>>> = [
        new Set([
          [1 as Int],
        ]),
      ];

      const memberMemberMemberSerializer = (it: any, path: string) => marshaller.serialize(it, 'Int', true, path);
      const memberMemberSerializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'List', true, path),
        memberMemberMemberSerializer,
        path,
      );
      const memberSerializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'Set', true, path),
        memberMemberSerializer,
        path,
      );
      const serializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'Array', true, path),
        memberSerializer,
        path,
      );

      expect(() => serializer(nested, 'test')).not.toThrow();
      expect(serializer(nested, 'test')).toEqual([[[1]]]);
    });

    it('should handle nested errors in collections correctly', () => {
      const badNested: Array<Set<Array<string>>> = [
        new Set([
          ['nope'],
        ]),
      ];

      const memberMemberMemberSerializer = (it: any, path: string) => {
        console.log(it, path, marshaller.serialize(it, 'Int', true, path));
        return marshaller.serialize(it, 'Int', true, path);
      };
      const memberMemberSerializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'List', true, path),
        memberMemberMemberSerializer,
        path,
      );
      const memberSerializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'Set', true, path),
        memberMemberSerializer,
        path,
      );
      const serializer = (it: any, path: string) => Marshaller.Util.mapSeq(
        marshaller.serialize(it, 'Array', true, path),
        memberSerializer,
        path,
      );

      expect(() => serializer(badNested, 'test')).toThrow();
      expect(() => serializer(badNested, 'test')).toThrowError('test[0][0][0]');
      expect(() => serializer(badNested, 'test')).toThrowError('Expected an Int');
    });
  });

  describe('defaulting to empty object', () => {
    it('should transform required but undefined objects with all fields optional to empty objects when serializing', () => {
      expect(() => OptionalFields.serialize(undefined, true)).not.toThrow();
      expect(OptionalFields.serialize(undefined, true)).toEqual({});
    });

    it('should explode on deserialization if undefined and required', () => {
      expect(() => OptionalFields.deserialize(undefined, true)).toThrowErrorMatchingSnapshot();
    });

    it('should not default valid objects with values', () => {
      const example = { name: 'Bla' };
      expect(() => OptionalFields.serialize(example, true)).not.toThrow();
      expect(OptionalFields.serialize(example, true)).toEqual(example);
    });

    it('should not default objects which are not required', () => {
      expect(() => OptionalFields.serialize(undefined, false)).not.toThrow();
      expect(OptionalFields.serialize(undefined, false)).toBeUndefined();
    });
  });

  describe('Streaming<T>', () => {
    it('should serialize streams as arrays', () => {
      const stream = [1, 2, 3];
      const stream2 = new Set([1, 2, 3]);
      expect(marshaller.serialize(stream, 'Streaming', true, 'test')).toEqual([1, 2, 3]);
      expect(marshaller.serialize(stream2, 'Streaming', true, 'test')).toEqual([1, 2, 3]);
    });

    it('should deserialize streams as arrays', () => {
      const stream = [1, 2, 3];
      const stream2 = new Set([1, 2, 3]);
      expect(marshaller.deserialize(stream, 'Streaming', true, 'test')).toEqual([1, 2, 3]);
      expect(marshaller.deserialize(stream2, 'Streaming', true, 'test')).toEqual([1, 2, 3]);
    });
  });
});
