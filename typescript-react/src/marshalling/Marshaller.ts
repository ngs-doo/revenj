import { MarshallingError } from './MarshallingError';
import * as Assert from './assert';
import * as Util from './utils';

type Processor<T, R = T> = (value: T) => R;
type CustomProcessor<T, R = T> = (value: T, path: string) => R;

export enum MiddlewareStep {
  Before,
  After,
}

/**
 * Predicate over serialization/deserialization target and metadata
 * @param value The value being processed
 * @param typeName The name of the type of the value being processed
 * @param isNonNullable Is the value non nullable
 * @param isBuiltInType Is the type a built-in type (versus a custom, user-defined type)
 */
type TestPredicate<T> = (value: T, typeName: string, isNonNullable: boolean, isBuiltInType: boolean) => boolean;

export type Serialized<DomObj> = {
  [K in keyof DomObj]: DomObj[K] extends Set<infer T>
    ? T[]
    : DomObj[K] extends Map<any, infer B> // currently limited to string
      ? ({ [key: string]: B } | Map<any, B>)
      : DomObj[K] extends IObjectAny
        ? Serialized<DomObj[K]>
        : DomObj[K]
};

interface IMiddleware<T, R = T> {
  process: Processor<T, R>;
  test?: TestPredicate<T>;
  when?: MiddlewareStep;
}

type ProcessorLookup<T, R = T> = Map<string, Processor<T, R>>;

const identity = <T>(value: T): T => value;

interface IMarshaller {
  /**
   * Restart the marshaller configuration.
   * This will restart all middleware and serializer/deserializer configuration.
   *
   * Primarily meant for usage in tests.
   */
  clear: () => this;
  /**
   * Register a serializer for the type name.
   * The type name is case-insensitive.
   * This function will be used when calling `serialize` with the same type name.
   *
   * Serializers are allowed to (and encouraged to) throw errors if they encounter invalid data
   *
   * @param typeName Type name for which the serializer is being registered
   * @param serializer Function to be called during serialization
   */
  registerSerializer: <T, R = T>(typeName: string, serializer: Processor<T, R>) => this;
  /**
   * Register a deserializer for the type name.
   * The type name is case-insensitive.
   * This function will be used when calling `deserialize` with the same type name.
   *
   * Deserializers are allowed to (and encouraged to) throw errors if they encounter invalid data
   *
   * @param typeName Type name for which the deserializer is being registered
   * @param deserializer Function to be called during deserialization
   */
  registerDeserializer: <T, R = T>(typeName: string, serializer: Processor<T, R>) => this;
  /**
   * Register a middleware function that will be called during serialization.
   * Any number of functions may be registered as middleware, and will be called in the order they were specified in.
   * Middleware will be invoked _before_ any `serialize` function.
   * Middleware will be invoked every time `serialize` is called, but the execution of the processor function can be guarded:
   *  - If the `test` function _is not_ specified, `process` will be invoked on every serialization attempt
   *  - If the `test` function _is_ specified, `process` will be called only when `test` returns `true`
   *
   * @param process Function to be invoked to transform values _before_ serialization. It receives only the value.
   * @param test Optional function to guard against applying the middleware. When not specified, it behaves as though it always returned `true`. It receives the value, the type name, and a boolean indicating whether the value is required.
   */
  registerSerializerMiddleware: <T, R = T>(process: Processor<T, R>, test?: TestPredicate<T>, when?: MiddlewareStep) => this;
  /**
   * Register a middleware function that will be called during deserialization.
   * Any number of functions may be registered as middleware, and will be called in the order they were specified in.
   * Middleware will be invoked _before_ or _after_ any `deserialize` function, depending on the configuration
   * Middleware will be invoked every time `deserialize` is called, but the execution of the processor function can be guarded:
   *  - If the `test` function _is not_ specified, `process` will be invoked on every deserialization attempt
   *  - If the `test` function _is_ specified, `process` will be called only when `test` returns `true`
   *
   * @param process Function to be invoked to transform values _before_ deserialization. It receives only the value.
   * @param test Optional function to guard against applying the middleware. When not specified, it behaves as though it always returned `true`. It receives the value, the type name, and a boolean indicating whether the value is required.
   */
  registerDeserializerMiddleware: <T, R = T>(process: Processor<T, R>, test?: TestPredicate<T>, when?: MiddlewareStep) => this;
  /**
   * Serializes a value for sending it via some communication channel.
   * The serializer will be resolved via `typeName`, in a case-insensitive manner, as defined through `registerSerializer`.
   * If no serializer was registered, an identity function will be used.
   * Before or after serialization, depending on the flag, all serialization middleware are invoked, unless their guard indicates that they should not be.
   *
   * If `value` is undefined/null and `isNonNullable` is `true`, an error will be thrown.
   * If `value` is undefined/null and `isNonNullable` is `false`, `undefined` will be returned.
   * Otherwise, the serializer will be executed.
   * If an error occurs, an error with additional details and `path` will be thrown.
   *
   * @param value The value to serialize
   * @param typeName The name of the type being serializer, and for which to resolve a serializer
   * @param isNonNullable Is the value non nullable
   * @param path The JSON path of the value in the root object, for error reporting
   * @param isBuiltInType Is the type a built-in type (versus custom, user-defined)
   */
  serialize: <T, R = T>(value: T | undefined | null, typeName: string, isNonNullable: boolean, path: string, isBuiltInType: boolean) => R | undefined;
  /**
   * Serializes a value for sending it via some communication channel.
   * The serializer is passed explicitly.
   * Before serialization, all serialization middleware are invoked, unless their guard indicates that they should not be.
   *
   * If `value` is undefined/null and `isNonNullable` is `true`, an error will be thrown.
   * If `value` is undefined/null and `isNonNullable` is `false`, `undefined` will be returned.
   * Otherwise, the serializer will be executed.
   * If an error occurs, an error with additional details and `path` will be thrown.
   *
   * @param value The value to serialize
   * @param typeName The name of the type being serializer, used in guards only
   * @param formatter The function used as a serializer
   * @param isNonNullable Is the value non nullable
   * @param path The JSON path of the value in the root object, for error reporting
   * @param isBuiltInType Is the type a built-in type (versus custom, user-defined)
   */
  serializeWith: <T, R = T>(value: T | undefined | null, typeName: string, formatter: CustomProcessor<T, R>, isNonNullable: boolean, path: string, isBuiltInType: boolean) => R | undefined;
  /**
   * Deserializes a value for receiving via some communication channel.
   * The deserializer will be resolved via `typeName`, in a case-insensitive manner, as defined through `registerDeserializer`.
   * If no deserializer was registered, an identity function will be used.
   * Before deserialization, all deserialization middleware are invoked, unless their guard indicates that they should not be.
   *
   * If `value` is undefined/null and `isNonNullable` is `true`, an error will be thrown.
   * If `value` is undefined/null and `isNonNullable` is `false`, `undefined` will be returned.
   * Otherwise, the deserializer will be executed.
   * If an error occurs, an error with additional details and `path` will be thrown.
   *
   * @param value The value to deserialize
   * @param typeName The name of the type being deserializer, and for which to resolve a deserializer
   * @param isNonNullable Is the value non nullable
   * @param path The JSON path of the value in the root object, for error reporting
   * @param isBuiltInType Is the type a built-in type (versus custom, user-defined)
   */
  deserialize: <T, R = T>(value: T | undefined | null, typeName: string, isNonNullable: boolean, path: string, isBuiltInType: boolean) => R | undefined;
  /**
   * Deserializes a value for receiving via some communication channel.
   * The deserializer is passed explicitly.
   * Before deserialization, all deserialization middleware are invoked, unless their guard indicates that they should not be.
   *
   * If `value` is undefined/null and `isNonNullable` is `true`, an error will be thrown.
   * If `value` is undefined/null and `isNonNullable` is `false`, `undefined` will be returned.
   * Otherwise, the deserializer will be executed.
   * If an error occurs, an error with additional details and `path` will be thrown.
   *
   * @param value The value to deserialize
   * @param typeName The name of the type being deserializer, used in guards only
   * @param formatter The function used as a deserializer
   * @param isNonNullable Is the value non nullable
   * @param path The JSON path of the value in the root object, for error reporting
   * @param isBuiltInType Is the type a built-in type (versus custom, user-defined)
   */
  deserializeWith: <T, R = T>(value: T | undefined | null, typeName: string, formatter: CustomProcessor<T, R>, isNonNullable: boolean, path: string, isBuiltInType: boolean) => R | undefined;
}

export class Marshaller implements IMarshaller {
  /**
   * Serialization utilities, re-exported for easier access in generated code
   */
  public static Util = Util; // Re-export as a namespace for utility
  public static Assert = Assert;

  /**
   * Get an instance of the marshaller.
   * The marshaller is a singleton.
   */
  public static getInstance() {
    if (this.instance == null) {
      return this.instance = new Marshaller();
    }

    return this.instance;
  }

  private static instance: Marshaller;

  private serializeMiddleware: Array<IMiddleware<any, any>> = [];
  private deserializeMiddleware: Array<IMiddleware<any, any>> = [];
  private serializerLookup: ProcessorLookup<any, any> = new Map();
  private deserializerLookup: ProcessorLookup<any, any> = new Map();

  /**
   * Marshaller is a singleton, ergo private constructor
   */
  private constructor() {}

  public registerSerializer<T, R = T>(typeName: string, serializer: Processor<T, R>) {
    this.serializerLookup.set(typeName.toLocaleLowerCase(), serializer);
    return this;
  }

  public registerDeserializer<T, R = T>(typeName: string, deserializer: Processor<T, R>) {
    this.deserializerLookup.set(typeName.toLocaleLowerCase(), deserializer);
    return this;
  }

  /**
   * Shorthand for registering both the serializer and deserializer
   * @param typeName Case-insensitive type name
   * @param formatter Function for formatting and validating the input
   */
  public registerFormatter<T, R = T>(typeName: string, formatter: Processor<T, R>) {
    this.registerSerializer<T, R>(typeName, formatter);
    this.registerDeserializer<T, R>(typeName, formatter);
    return this;
  }

  public registerSerializerMiddleware<T, R = T>(process: Processor<T, R>, test?: TestPredicate<T>, when?: MiddlewareStep) {
    this.serializeMiddleware.push({
      process,
      test,
      when,
    });
    return this;
  }

  public registerDeserializerMiddleware<T, R = T>(process: Processor<T, R>, test?: TestPredicate<T>, when?: MiddlewareStep) {
    this.deserializeMiddleware.push({
      process,
      test,
      when,
    });
    return this;
  }

  public clear() {
    this.serializeMiddleware = [];
    this.deserializeMiddleware = [];
    this.serializerLookup = new Map();
    this.deserializerLookup = new Map();
    return this;
  }

  public serializeWith<T, R = T>(value: T | undefined | null, typeName: string, formatter: CustomProcessor<T, R>, isNonNullable: boolean, path: string, isBuiltInType: boolean = false): R | undefined {
    try {
      const result = this.serializeMiddleware.filter((it) => it.when !== MiddlewareStep.After).reduce((value, { process, test }) => {
        return (test == null || test(value, typeName, isNonNullable, isBuiltInType)) ? process(value) : value;
      }, value);

      if (Assert.assertPresence(result, isNonNullable) == null && !isNonNullable) {
        return;
      }

      const formatted = formatter(result as T, path);
      return this.serializeMiddleware.filter((it) => it.when === MiddlewareStep.After).reduce((value, { process, test }) => {
        return (test == null || test(value, typeName, isNonNullable, isBuiltInType)) ? process(value) : value;
      }, formatted);

    } catch (error) {
      if (error instanceof MarshallingError) {
        throw error;
      }
      const message = error instanceof Error ? error.message : String(error);
      throw new MarshallingError(`Serializing failed on field "${path}" of type ${typeName}${isNonNullable ? '' : '?'}:
    ${message}`);
    }
  }

  public deserializeWith<T, R = T>(value: T | undefined | null, typeName: string, formatter: CustomProcessor<T, R>, isNonNullable: boolean, path: string, isBuiltInType: boolean = false): R | undefined {

    try {
      const result = this.deserializeMiddleware.filter((it) => it.when !== MiddlewareStep.After).reduce((value, { process, test }) => {
        return (test == null || test(value, typeName, isNonNullable, isBuiltInType)) ? process(value) : value;
      }, value);

      if (Assert.assertPresence(result, isNonNullable) == null && !isNonNullable) {
        return;
      }

      const formatted = formatter(result as T, path);
      return this.deserializeMiddleware.filter((it) => it.when === MiddlewareStep.After).reduce((value, { process, test }) => {
        return (test == null || test(value, typeName, isNonNullable, isBuiltInType)) ? process(value) : value;
      }, formatted);
    } catch (error) {
      if (error instanceof MarshallingError) {
        throw error;
      }
      const message = error instanceof Error ? error.message : String(error);
      throw new MarshallingError(`Deserializing failed on field "${path}" of type ${typeName}${isNonNullable ? '' : '?'}:
    ${message}`);
    }
  }

  public serialize<T, R = T>(value: T | undefined | null, typeName: string, isNonNullable: boolean, path: string, isBuiltInType: boolean = true): R | undefined {
    const formatter = (value: T) => this.resolveSerializer<T, R>(typeName)(value);
    return this.serializeWith(value, typeName, formatter, isNonNullable, path, isBuiltInType);
  }

  public deserialize<T, R = T>(value: T | undefined | null, typeName: string, isNonNullable: boolean, path: string, isBuiltInType: boolean = true): R | undefined {
    const formatter = (value: T) => this.resolveDeserializer<T, R>(typeName)(value);
    return this.deserializeWith(value, typeName, formatter, isNonNullable, path, isBuiltInType);
  }

  protected resolveSerializer<T, R = T>(typeName: string): Processor<T, R> {
    const standardName = typeName.toLocaleLowerCase();
    return this.serializerLookup.has(standardName) ? this.serializerLookup.get(standardName)! : identity;
  }

  protected resolveDeserializer<T, R = T>(typeName: string): Processor<T, R> {
    const standardName = typeName.toLocaleLowerCase();
    return this.deserializerLookup.has(standardName) ? this.deserializerLookup.get(standardName)! : identity;
  }
}
