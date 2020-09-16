import { Marshaller } from '../Marshaller';

const marshaller = Marshaller.getInstance();

describe('data marshalling', () => {
  describe('serialize', () => {
    describe('simple serialization', () => {
      describe('basic processing', () => {
        it('should return the value unmolested by default', () => {
          expect(marshaller.serialize(1, 'Int', true, 'test')).toEqual(1);
          expect(marshaller.serialize(1, 'Int', false, 'test')).toEqual(1);
          expect(marshaller.serialize('x', 'String', true, 'test')).toEqual('x');
        });

        it('should explode with a descriptive error if the value is required but not present', () => {
          expect(() => marshaller.serialize(null, 'Int', true, 'test')).toThrow();
          expect(() => marshaller.serialize(null, 'Int', true, 'test')).toThrowErrorMatchingSnapshot();
        });

        it('should return undefined if the value is null/undefined and not required', () => {
          expect(marshaller.serialize(null, 'Int', false, 'test')).not.toBeNull();
          expect(marshaller.serialize(null, 'Int', false, 'test')).toBeUndefined();
          expect(marshaller.serialize(undefined, 'Int', false, 'test')).toBeUndefined();
        });
      });

      describe('registering type handlers', () => {
        let stringFn: jest.Mock<string>;
        let intFn: jest.Mock<number>;

        beforeEach(() => {
          stringFn = jest.fn((it: string) => it.toUpperCase());
          intFn = jest.fn((it: number) => Number.parseInt(String(it), 10));
          marshaller.registerSerializer('String', stringFn);
          marshaller.registerSerializer('Int', intFn);
        });

        afterEach(() => {
          marshaller.clear();
        });

        it('should use a serializer registered for type if specified', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.serialize('test1', 'String', true, 'test');
          expect(stringFn).toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();
          expect(str).toEqual('TEST1');

          const int = marshaller.serialize(20, 'Int', true, 'test');
          expect(intFn).toHaveBeenCalled();
          expect(int).toEqual(20);

          const int2 = marshaller.serialize('20', 'Int', true, 'test');
          expect(int2).toEqual(20);
        });

        it('should use a serializer in a case-insensitive way', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.serialize('test1', 'string', true, 'test');
          expect(stringFn).toHaveBeenCalled();
          expect(str).toEqual('TEST1');

          const int = marshaller.serialize(20, 'INT', true, 'test');
          expect(intFn).toHaveBeenCalled();
          expect(int).toEqual(20);
        });

        it('should never get to the serializer if the value is not defined', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.serialize(null, 'String', false, 'test');
          const int = marshaller.serialize(undefined, 'Int', false, 'test');
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();
          expect(str).toBeUndefined();
          expect(int).toBeUndefined();
        });
      });

      describe('registering middleware', () => {
        it('should apply all middleware without test on anything', () => {
          const middleware = jest.fn((it: any) => it);
          marshaller.registerSerializerMiddleware(middleware);
          expect(middleware).not.toHaveBeenCalled();

          const str = marshaller.serialize('test1', 'String', true, 'test');
          expect(middleware).toHaveBeenCalled();
          expect(str).toBe('test1');
        });

        it('should be called only if test returns true if it has a test defined', () => {
          const clipIntMiddleware = jest.fn(() => 10);
          const test = (value: any, typeName: string) => typeName.toLocaleLowerCase() === 'int' && Number.parseInt(String(value), 10) > 10;
          marshaller.registerSerializerMiddleware(clipIntMiddleware, test);

          marshaller.serialize('test1', 'String', true, 'test');
          expect(clipIntMiddleware).not.toHaveBeenCalled();

          marshaller.serialize(9, 'Int', true, 'test');
          expect(clipIntMiddleware).not.toHaveBeenCalled();

          const result = marshaller.serialize(11, 'Int', true, 'test');
          expect(clipIntMiddleware).toHaveBeenCalled();
          expect(result).toBe(10);
        });
      });
    });

    describe('simple deserialization', () => {
      describe('basic processing', () => {
        it('should return the value unmolested by default', () => {
          expect(marshaller.deserialize(1, 'Int', true, 'test')).toEqual(1);
          expect(marshaller.deserialize(1, 'Int', false, 'test')).toEqual(1);
          expect(marshaller.deserialize('x', 'String', true, 'test')).toEqual('x');
        });

        it('should explode with a descriptive error if the value is required but not present', () => {
          expect(() => marshaller.deserialize(null, 'Int', true, 'test')).toThrow();
          expect(() => marshaller.deserialize(null, 'Int', true, 'test')).toThrowErrorMatchingSnapshot();
        });

        it('should return undefined if the value is null/undefined and not required', () => {
          expect(marshaller.deserialize(null, 'Int', false, 'test')).not.toBeNull();
          expect(marshaller.deserialize(null, 'Int', false, 'test')).toBeUndefined();
          expect(marshaller.deserialize(undefined, 'Int', false, 'test')).toBeUndefined();
        });
      });

      describe('registering type handlers', () => {
        let stringFn: jest.Mock<string>;
        let intFn: jest.Mock<number>;

        beforeEach(() => {
          stringFn = jest.fn((it: string) => it.toUpperCase());
          intFn = jest.fn((it: number) => Number.parseInt(String(it), 10));
          marshaller.registerDeserializer('String', stringFn);
          marshaller.registerDeserializer('Int', intFn);
        });

        afterEach(() => {
          marshaller.clear();
        });

        it('should use a deserializer registered for type if specified', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.deserialize('test1', 'String', true, 'test');
          expect(stringFn).toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();
          expect(str).toEqual('TEST1');

          const int = marshaller.deserialize(20, 'Int', true, 'test');
          expect(intFn).toHaveBeenCalled();
          expect(int).toEqual(20);

          const int2 = marshaller.deserialize('20', 'Int', true, 'test');
          expect(int2).toEqual(20);
        });

        it('should use a deserializer in a case-insensitive way', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.deserialize('test1', 'string', true, 'test');
          expect(stringFn).toHaveBeenCalled();
          expect(str).toEqual('TEST1');

          const int = marshaller.deserialize(20, 'INT', true, 'test');
          expect(intFn).toHaveBeenCalled();
          expect(int).toEqual(20);
        });

        it('should never get to the deserializer if the value is not defined', () => {
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();

          const str = marshaller.deserialize(null, 'String', false, 'test');
          const int = marshaller.deserialize(undefined, 'Int', false, 'test');
          expect(stringFn).not.toHaveBeenCalled();
          expect(intFn).not.toHaveBeenCalled();
          expect(str).toBeUndefined();
          expect(int).toBeUndefined();
        });
      });

      describe('registering middleware', () => {
        it('should apply all middleware without test on anything', () => {
          const middleware = jest.fn((it: any) => it);
          marshaller.registerDeserializerMiddleware(middleware);
          expect(middleware).not.toHaveBeenCalled();

          const str = marshaller.deserialize('test1', 'String', true, 'test');
          expect(middleware).toHaveBeenCalled();
          expect(str).toBe('test1');
        });

        it('should be called only if test returns true if it has a test defined', () => {
          const clipIntMiddleware = jest.fn(() => 10);
          const test = (value: any, typeName: string) => typeName.toLocaleLowerCase() === 'int' && Number.parseInt(String(value), 10) > 10;
          marshaller.registerDeserializerMiddleware(clipIntMiddleware, test);

          marshaller.deserialize('test1', 'String', true, 'test');
          expect(clipIntMiddleware).not.toHaveBeenCalled();

          marshaller.deserialize(9, 'Int', true, 'test');
          expect(clipIntMiddleware).not.toHaveBeenCalled();

          const result = marshaller.deserialize(11, 'Int', true, 'test');
          expect(clipIntMiddleware).toHaveBeenCalled();
          expect(result).toBe(10);
        });
      });
    });
  });
});
