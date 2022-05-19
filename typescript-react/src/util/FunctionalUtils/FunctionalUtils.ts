import fastDeepEqual from 'fast-deep-equal';
import isPlainObject from 'is-plain-object';

export const mapOrElse = (
  value: any,
  mappingFunction: (...args: any[]) => any,
  orElseValue: any
) =>
  value !== null && value !== undefined ? mappingFunction(value) : orElseValue;

export function identity<T>(val: T): T {
  return val;
}

export const isFunction = (x: any): x is Function => typeof x === 'function'; // tslint:disable-line ban-types

export const noop = (..._args: any[]) => {};

export const falsy = (val: any): boolean => !val;

export const truthy = (val: any): boolean => val;

export const eq = (a: any) => (b: any) => a === b;

// Add more if needed
export function pipe<A, B>(f1: (x1: A) => B): (initial: A) => B;
export function pipe<A, B, C>(
  f1: (x1: A) => B,
  f2: (x2: B) => C
): (initial: A) => C;
export function pipe<A, B, C, D>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D
): (initial: A) => D;
export function pipe<A, B, C, D, E>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E
): (initial: A) => E;
export function pipe<A, B, C, D, E, F>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E,
  f5: (x5: E) => F
): (initial: A) => F;
export function pipe<A, B, C, D, E, F, G>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E,
  f5: (x5: E) => F,
  f6: (x6: F) => G
): (initial: A) => G;
export function pipe<A, B, C, D, E, F, G, H>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E,
  f5: (x5: E) => F,
  f6: (x6: F) => G,
  f7: (x7: G) => H
): (initial: A) => H;
export function pipe<A, B, C, D, E, F, G, H, I>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E,
  f5: (x5: E) => F,
  f6: (x6: F) => G,
  f7: (x7: G) => H,
  f8: (x8: H) => I
): (initial: A) => I;
export function pipe<A, B, C, D, E, F, G, H, I, J>(
  f1: (x1: A) => B,
  f2: (x2: B) => C,
  f3: (x3: C) => D,
  f4: (x4: D) => E,
  f5: (x5: E) => F,
  f6: (x6: F) => G,
  f7: (x7: G) => H,
  f8: (x8: H) => I,
  f9: (x9: I) => J
): (initial: A) => J;
export function pipe(...fns: IFunctionAny[]) {
  return (initial: any) => fns.reduce((arg, f) => f(arg), initial);
}

export const first = <T>(arr: T[]): T => arr[0];

export const last = <T>(arr: T[]): T => arr[arr.length - 1];

export const zip = <F, S>(xs: F[], ys: Array<S | undefined>): Array<[F, S]> =>
  xs
    .map((x: F, index: number): [F, S | undefined] => [x, ys[index]])
    .filter(([_x, y]) => y !== undefined) as Array<[F, S]>;

export const fst = <T>([f, _s]: [T, any]): T => f;

export const snd = <T>([_f, s]: [any, T]): T => s;

export function count<T>(items: T[], itemOrPredicate: T | ((x: T) => boolean)) {
  if (typeof itemOrPredicate === 'function') {
    return items.filter(itemOrPredicate as (x: T) => boolean).length;
  } else {
    return items.filter((item) => item === itemOrPredicate).length;
  }
}

export const filterValues = <T extends {}>(
  obj: T,
  fn: <K extends keyof T>(val: T[K], key: string) => boolean
) =>
  Object.keys(obj).reduce((acc, key: string) => {
    if (fn(obj[key as keyof T], key)) {
      return { ...acc, [key]: obj[key as keyof T] };
    }

    return acc;
  }, {});

export const tryOrDefault = <T>(fn: () => T, fallback: T) => {
  try {
    return fn();
  } catch (e) {
    return fallback;
  }
};

export const not = (fn: (...args: any[]) => boolean) => (...args: any[]) =>
  !fn(...args);

export const range = (from: number, to: number, step: number = 1) => {
  const arr = [];

  for (let i = from; i < to; i += step) {
    arr.push(i);
  }

  return arr;
};
interface IIdLookup<T> {
  [index: string]: T;
}

export function dictionaryForIds<
  T extends { [key: string]: string | number } & any
>(collection: T[], field: keyof T): IIdLookup<T> {
  return collection.reduce(
    (acc: IIdLookup<T>, item: T) => ({
      ...acc,
      [String(get(item, field))]: item
    }),
    {}
  );
}

const keys = (it: IObjectAny | any[]): string[] => {
  if (it == null) {
    return [];
  } else if (Array.isArray(it)) {
    return it.map((_: any, index) => String(index));
  } else if (isObject(it)) {
    return Object.keys(it);
  } else {
    return [];
  }
};

export const deepKeys = (
  obj: IObjectAny,
  diveIntoArrays: boolean = false
): string[] => {
  return keys(obj).reduce((keys: string[], key: string) => {
    if (diveIntoArrays && obj[key] && Array.isArray(obj[key])) {
      if (obj[key].length === 0) {
        return keys;
      }

      const producedKeys = flatMap(obj[key], (it: IObjectAny, index) =>
        deepKeys(it, diveIntoArrays).map(
          (deepKey) => `${key}[${index}].${deepKey}`
        )
      );

      return producedKeys.length === 0
        ? [
            ...keys,
            ...obj[key].map((_: any, index: number) => `${key}[${index}]`)
          ]
        : [...keys, ...producedKeys];
    } else if (
      obj[key] &&
      isObject(obj[key]) &&
      !Array.isArray(obj[key]) &&
      typeof obj[key] !== 'function'
    ) {
      return [
        ...keys,
        ...deepKeys(obj[key], diveIntoArrays).map(
          (deepKey) => `${key}.${deepKey}`
        )
      ];
    } else {
      return [...keys, key];
    }
  }, []);
};

export type Equality<T> = (a: T, b: T) => boolean;

export const objectDiffBy = (
  equalsFn: Equality<any>,
  objA: IObjectAny,
  objB: IObjectAny
) => {
  const keysA = deepKeys(objA);
  const keysB = deepKeys(objB);
  const allKeys = new Set([...keysA, ...keysB]);
  const diff = {};

  allKeys.forEach((key: string) => {
    const valueA = get(objA, key);
    const valueB = get(objB, key);
    set(diff, key as any, !equalsFn(valueA, valueB));
  });

  return diff;
};

export const objectDiff = (
  objA: IObjectAny = {},
  objB: IObjectAny = {}
): IObjectAny => {
  return objectDiffBy((a, b) => a === b, objA, objB);
};

export const existsDeepDiffBy = (
  equalsFn: Equality<any>,
  objA: IObjectAny,
  objB: IObjectAny
) => {
  const keysA = deepKeys(objA);
  const keysB = deepKeys(objB);
  const allKeys = Array.from(new Set([...keysA, ...keysB]));

  return allKeys.some((key) => !equalsFn(get(objA, key), get(objB, key)));
};

export const existsDeepDiff = (objA: IObjectAny, objB: IObjectAny) =>
  existsDeepDiffBy((a, b) => a === b, objA, objB);

export const deduplicateBy = <T>(key: keyof T, items: T[]): T[] => {
  const uniqueProps = Array.from(new Set(items.map((item: T) => item[key])));
  return uniqueProps.map((value) =>
    items.find((item: T) => item[key] === value)
  ) as T[];
};

export const valueOrNull = <T>(x: T): T | null => {
  if (x == null) {
    return null;
  } else if (typeof x === 'number' || typeof x === 'boolean') {
    return x;
  } else {
    return !isEmpty(x) ? x : null;
  }
};

export const flatten = <T>(xss: T[][]): T[] =>
  xss.reduce((memo, val) => memo.concat(val), [] as T[]);

export const flatMap = <T, S = T>(
  xs: T[],
  f: (x: T, index: number, arr: T[]) => S[]
): S[] => flatten(xs.map(f));

export const omit = <T, K extends keyof T>(o: T, ...keys: K[]): Omit<T, K> => {
  const target: any = {};
  Object.keys(o).forEach((key: string) => {
    if (!keys.includes(key as K)) {
      target[key] = o[key as keyof T];
    }
  });
  return target as Omit<T, K>;
};

export const pick = <T, K extends keyof T>(o: T, ...keys: K[]): Pick<T, K> => {
  const target: any = {};
  keys.forEach((key) => {
    target[key] = o[key];
  });
  return target as Pick<T, K>;
};

export const isObject = isPlainObject;

export const isEmpty = (x: any): boolean => {
  if (!x) {
    return true;
  }

  if (Array.isArray(x) || typeof x === 'string') {
    return x.length === 0;
  }

  if (isObject(x)) {
    return Object.keys(x).filter((k) => x[k] != null).length === 0;
  }

  return true;
};

export function groupBy<T, K extends string>(
  xs: T[],
  grouper: (x: T) => K
): { [key: string]: T[] };
export function groupBy<T, K extends number>(
  xs: T[],
  grouper: (x: T) => K
): { [key: number]: T[] };
export function groupBy<T, _K>(xs: T[], grouper: IFunctionAny) {
  const lookup: any = {};

  xs.forEach((x: T) => {
    const key: keyof T = grouper(x);
    if (lookup[key] == null) {
      lookup[key] = [];
    }
    lookup[key].push(x);
  });

  return lookup;
}

type ILookup<K extends string | number, T> = K extends string
  ? { [key: string]: T }
  : { [key: number]: T };

export function mapValues<T, K extends string, R>(
  map: ILookup<K, T>,
  fn: (x: T, key?: string) => R
): { [key: string]: R };
export function mapValues<T, K extends number, R>(
  map: ILookup<K, T>,
  fn: (x: T, key?: string) => R
): { [key: number]: R };
export function mapValues(map: any, fn: IFunctionAny) {
  return Object.keys(map).reduce(
    (acc, key) => ({ ...acc, [key]: fn(map[key], key) }),
    {}
  );
}

export const isDeepEqual = fastDeepEqual;

export const isEqual = <T>(xs?: T, ys?: T): boolean => {
  if (xs === ys) {
    return true;
  }

  if (
    (typeof xs === 'string' || xs instanceof String) &&
    (typeof ys === 'string' || ys instanceof String)
  ) {
    return xs.toString() === ys.toString();
  }

  if (
    (typeof xs === 'number' || xs instanceof Number) &&
    (typeof ys === 'number' || ys instanceof Number)
  ) {
    if (
      Number.isNaN(Number(xs).valueOf()) &&
      Number.isNaN(Number(ys).valueOf())
    ) {
      // tslint:disable-line no-construct
      return true;
    }
    return Number(xs).valueOf() === Number(ys).valueOf(); // tslint:disable-line no-construct
  }

  if (
    (typeof xs === 'boolean' || xs instanceof Boolean) &&
    (typeof ys === 'boolean' || ys instanceof Boolean)
  ) {
    const bxs = xs instanceof Boolean ? xs.valueOf() : xs;
    const bys = ys instanceof Boolean ? ys.valueOf() : ys;
    return bxs === bys;
  }

  if (Array.isArray(xs) && Array.isArray(ys)) {
    return (
      (xs as any).length === (ys as any).length &&
      (xs as any).every((x: any, i: number) => isEqual(x, ys[i]))
    );
  }

  if (xs instanceof Set && ys instanceof Set) {
    if (xs.size !== ys.size) {
      return false;
    }

    for (const x of Array.from(xs)) {
      if (!ys.has(x)) {
        return false;
      }
    }

    return true;
  }

  if (isObject(xs) && isObject(ys)) {
    const keysXs = Object.keys(xs!);
    const keysYs = Object.keys(ys!);
    if (keysXs.length !== keysYs.length) {
      return false;
    }

    return keysXs.every((key) =>
      isEqual(xs![key as keyof T], ys![key as keyof T])
    );
  }

  return false;
};

export type Path = string | string[];

const normalizePath = <T>(path: Path): DeepKeyOf<T> & string[] =>
  Array.isArray(path) ? path : path.replace(/\[(.+?)\]/g, '.$1').split('.');

const isNumberKey = (key: string | number) => {
  if (typeof key === 'number') {
    return key >= 0;
  }

  const parsed = Number.parseInt(key, 10);
  return !Number.isNaN(parsed) && parsed >= 0;
};

export const set = <T, K extends DeepKeyOf<T>>(
  x: T,
  path: K,
  value?: DeepTypeOf<T, K>
): T => {
  const normalizedPath = normalizePath(path as Path);
  let target: any = x;

  normalizedPath.forEach((fragment, index) => {
    const nextFragment = normalizedPath[index + 1];
    const key = isNumberKey(fragment) ? parseInt(fragment, 10) : fragment;
    const nextKey = isNumberKey(nextFragment)
      ? parseInt(nextFragment, 10)
      : nextFragment;
    if (index === normalizedPath.length - 1) {
      target[key] = value;
      return;
    }

    if (target[key] == null) {
      target[key] = typeof nextKey === 'number' ? [] : {};
    }

    target = target[key];
  });

  return x;
};

export const get = <T, K extends DeepKeyOf<T>>(
  x: T,
  path: K,
  fallback?: DeepTypeOf<T, K>
): DeepTypeOf<T, K> | undefined => {
  if (path == null) {
    return fallback;
  }

  const normalizedPath = normalizePath(path as Path);
  let target: any = x;
  let index = 0;

  for (const fragment of normalizedPath) {
    if (index++ === normalizedPath.length - 1) {
      if (target instanceof Map) {
        return target.get(fragment) || fallback;
      }

      return target != null && target[fragment] != null
        ? target[fragment]
        : fallback;
    }

    if (target == null || target[fragment] == null) {
      return fallback;
    }

    target = target[fragment];
  }

  return fallback;
};

export const mapIfDefined = <T, R>(
  x: T | undefined | null,
  f: (a: T) => R
): R | undefined | null => (x != null ? f(x) : null);

export const defaulting = <T>(value: T | undefined | null, fallback: T): T =>
  value != null ? value : fallback;

export const mapOrDefault = <T, R>(
  x: T | undefined | null,
  f: (a: T) => R,
  fallback: R
): R => (x != null ? f(x) : fallback);

export const takeWhile = <T>(xs: T[], p: (a: T) => boolean): T[] => {
  const result = [];
  for (const x of xs) {
    if (p(x)) {
      result.push(x);
    } else {
      break;
    }
  }

  return result;
};

export const squashObject = <T>(x?: T): T | undefined =>
  x == null ||
  Object.keys(x).filter((k) => x[k as keyof T] != null).length === 0
    ? undefined
    : x;

// Checks if the passed object type is Set, and if not makes it a Set object accordingly
// This function added to make sure selectors having the correct data types after Rehydration.
// TODO: See IFOA-186 for details.
export const ensureSet = <T>(item: IObjectAny): Set<T> => {
  let output: Set<T> | null = null;

  if (!(item instanceof Set)) {
    output =
      Array.isArray(item) && (item as T[]).length > 0
        ? new Set<T>(item)
        : new Set<T>();
  }

  return output != null ? output : new Set(item as Set<T>);
};

export const concatUnique = <T>(xs: T[][]): T[] => {
  const result = [];
  const items = flatten(xs);
  for (const item of items) {
    if (result.find((i) => isEqual(item, i)) == null) {
      result.push(item);
    }
  }
  return result;
};

export const moveFromToIndex = <T>(xs: T[], i: number, j: number): T[] => {
  const x = xs[i];
  const result = [...xs];

  if (i === j - 1) {
    return result;
  }

  result.splice(i, 1);
  result.splice(j, 0, x);
  return result;
};
