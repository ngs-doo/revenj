export type CompareResult = -1 | 0 | 1;
export type Comparator<T> = (a: T, b: T) => CompareResult;

export const compare = <T>(a: T, b: T): CompareResult => {
  if (a < b) {
    return -1;
  }

  if (a > b) {
    return 1;
  }

  return 0;
};

export const reverse = <T>(comparator: Comparator<T>): Comparator<T> =>
  (a: T, b: T): CompareResult =>
    (comparator(a, b) * -1 as CompareResult);

export const compareBy = <T, R>(fn: (x: T) => R, compareFn: Comparator<R> = compare) =>
  (a: T, b: T) => compareFn(fn(a), fn(b));

export const sortBy = <T, R>(getValue: (x: T) => R, xs: T[]): T[] =>
  [...xs].sort(compareBy(getValue));
