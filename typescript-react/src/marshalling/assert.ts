
import { TypescriptResultSet } from '../ResultSet/ResultSet';
import {
  INT_MAX_VALUE,
  INT_MIN_VALUE,
  SHORT_MAX_VALUE,
  SHORT_MIN_VALUE,
  LONG_MAX_VALUE,
  LONG_MIN_VALUE,
} from '../constants';
import { isObject } from '../util/FunctionalUtils/FunctionalUtils';
import { formatNumber } from '../util/Formatters/NumberFormatter';
import { isNumber } from '../util/NumberUtils/NumberUtils';

type Dictionary<K extends string | number, V> = K extends string ? { [key: string]: V }: { [key: number]: V };

export const assertionWithErrorPrefix = <T>(prefix: string) => (assertion: (value: T) => T): (value: T) => T =>
  (value: T): T => {
    try {
      return assertion(value);
    } catch (error) {
      throw new Error(`${prefix}: ${error instanceof Error ? error.message : String(error)}`);
    }
  };

export const assertPresence = <T>(value: T, isNonNullable: boolean): T | undefined => {
  if (value == null) {
    if (isNonNullable) {
      throw new Error(`Value is required, but is not set!`);
    } else {
      return;
    }
  }

  return value;
};

export const assertPresenceStrict = <T>(value: T) => assertPresence(value, true);

export const assertString = <T extends string>(value: T, length?: number): T => {
  if (typeof value !== 'string') {
    throw new Error(`Expected a String, but value is ${value}`);
  }

  if (length != null && value.length > length!) {
    throw new Error(`Maximum length is ${length!}, but received string of length ${value.length}`);
  }

  return value;
};

export const assertInt = <T extends Int>(value: T): T => {
  const number = Number.parseInt(String(value), 10) as T;

  if (Number.isNaN(number) || value > INT_MAX_VALUE || value < INT_MIN_VALUE || typeof value !== 'number' || value !== number) {
    throw new Error(`Expected an Int, but value is ${value}`);
  }
  return number;
};

/**
 * Asserts that a given value is within the range of a 64-bit signed Long.
 * This is necessary because JavaScript loses numeric precision beyond Number.MAX_SAFE_INTEGER.
 * Ideally, serialization should be done using strings and deserialized into BigInt or a bignum,
 * but for now, this approach is acceptable.
 */
export const assertLong = <T extends Long>(value: T): T => {
  try {
    const number = BigInt(value);
    if (number < BigInt(LONG_MIN_VALUE) || number > BigInt(LONG_MAX_VALUE)) {
      throw new Error(`Expected a Long, but value is ${value}`);
    }
    if (number >= Number.MIN_SAFE_INTEGER && number <= Number.MAX_SAFE_INTEGER) {
      return Number.parseInt(String(value), 10) as unknown as T;
    }
    return number.toString() as unknown as T;
  } catch (error) {
    throw new Error(`Expected a Long, but value is ${value}`);
  }
};

export const assertShort = <T extends Short>(value: T): T => {
  const number = assertInt(value as unknown as Int);
  if (number > SHORT_MAX_VALUE || number < SHORT_MIN_VALUE) {
    throw new Error(`Number ${number} out of Short range`);
  }

  return number as unknown as T;
};

export const assertFloatingPoint = <T extends number>(value: T): T => {
  const number = Number.parseFloat(String(value)) as T;
  if (Number.isNaN(number) || typeof value !== 'number') {
    throw new Error(`Expected a floating-point number, but value is ${value}`);
  }
  return number;
};

export const assertDecimal = <T extends DecimalStr>(value: T): T => {
  const number = formatNumber(value) as T;
  if (!isNumber(number)) {
    throw new Error(`Expected a floating-point number, but value is ${value}`);
  }
  return number;
};

export const serializeBinary = (value: BinaryStr): BinaryStr =>
  btoa(
    encodeURIComponent(assertString(value))
    .replace(/%([0-9A-F]{2})/g, (_, g) => String.fromCharCode(parseInt(g, 16))),
  ) as BinaryStr;

export const deserializeBinary = (value: BinaryStr): BinaryStr =>
  decodeURIComponent(
    Array.from(atob(assertString(value)))
    .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''),
  ) as BinaryStr;

export const assertArray = <T>(it: T[]): T[] => {
  if (!Array.isArray(it)) {
    throw new Error(`Expected an array, but value is ${it}`);
  }

  return Array.from(it);
};

export const serializeSet = <T>(it: Set<T>): T[] => {
  if (!(it instanceof Set)) {
    throw new Error(`Expected a Set, but value is ${it}`);
  }

  return Array.from(it);
};

export const deserializeSet = <T>(it: T[]): Set<T> => {
  if (!Array.isArray(it)) {
    throw new Error(`Expected an array, but value is ${it}`);
  }

  return new Set(it);
};

export const serializeMap = <K extends string | number, V>(it: Map<K, V>): Dictionary<K, V> => {
  if (it instanceof Map) {
    const ret: Dictionary<K, V> = {} as Dictionary<K, V>;
    it.forEach((value, key) => {
      ret[key as any] = value;
    });
    return ret;
  }

  if (isObject(it)) {
    return it;
  }

  throw new Error(`Expected a Map, but value is ${it}`);
};

export const deserializeMap = <K extends string | number, V>(it: Dictionary<K, V>): Map<K, V> => {
  // A... non-exhaustive list
  if (!isObject(it) || Array.isArray(it) || it instanceof Map || it instanceof Set) {
    throw new Error(`Expected a dictionary object, but value is ${it}`);
  }

  const ret = new Map<K, V>();
  Object.keys(it).forEach((key) => {
    ret.set(key as unknown as K, it[key]);
  });
  return ret;
};

export const assertIterable = <T>(it: Iterable<T>): T[] => {
  if (it == null || (typeof it[Symbol.iterator] !== 'function')) {
    throw new Error(`Expected an iterable collection, but value is ${it}`);
  }

  return Array.from(it);
};

export const serializeResultSet = (it: TypescriptResultSet): ResultSet => {
  return assertArray(TypescriptResultSet.serialize(it));
};

export const deserializeResultSet = (it: ResultSet): TypescriptResultSet => {
  return TypescriptResultSet.deserialize(assertArray(it));
};

export const assertEnum = <T>(values: T[], name: string) => (it: T, isNonNullable: boolean, _: string): T => {
  if (assertPresence(it, isNonNullable) == null && !isNonNullable) {
    return it;
  }

  if (!values.includes(it)) {
    throw new Error(`Value ${it} is not a valid member of enumeration ${name}`);
  }

  return it;
};

interface IDictionary {
  [key: string]: string;
}

export const assertDictionary = (value: any, field: string): IDictionary => {
  if (
    typeof value !== 'object' &&
    Object.keys(value).some((key) => typeof key !== 'string') &&
    Object.values(value).some((value) => typeof value !== 'string')
  ) {
    throw new Error(`${field} must be a Dictionary, received ${JSON.stringify(value)}`);
  }
  return value as IDictionary;
};

// Composite Type Assertions

export const assertS3 = (value: any): S3 => {
  assertionWithErrorPrefix<any>('S3 key')(assertString)(value.key);
  assertionWithErrorPrefix<any>('S3 bucket')(assertString)(value.bucket);
  assertionWithErrorPrefix<any>('S3 length')(assertLong)(value.length);

  return {
    bucket: assertionWithErrorPrefix<string>('S3 bucket')(assertString)(value.bucket),
    key: assertionWithErrorPrefix<string>('S3 key')(assertString)(value.key),
    length: assertionWithErrorPrefix<Long>('S3 length')(assertLong)(value.length),
    metadata: value.metadata != null ? assertDictionary(value.metadata, 'S3.metadata') : undefined,
    mimeType: value.mimeType != null ? assertionWithErrorPrefix<string>('S3 mime type')(assertString)(value.mimeType) : undefined,
    name: value.name != null ? assertionWithErrorPrefix<string>('S3 name')(assertString)(value.name) : undefined,
  };
};
