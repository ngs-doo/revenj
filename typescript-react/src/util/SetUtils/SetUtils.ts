export const union = <T>(x: Set<T>, y: Set<T>): Set<T> => {
  const result = new Set(x);
  const others = Array.from(y);
  for (const item of others) {
    result.add(item);
  }
  return result;
};

export const intersection = <T>(x: Set<T>, y: Set<T>): Set<T> => new Set<T>([
  ...Array.from(x).filter((mem) => y.has(mem)),
]);

export const difference = <T>(x: Set<T>, y: Set<T>): Set<T> => new Set<T>([
  ...Array.from(x).filter((mem) => !y.has(mem)),
]);

export const isSuperset = <T>(x: Set<T>, y: Set<T>): boolean =>
  Array.from(y).every((it) => x.has(it));

export const isSubset = <T>(x: Set<T>, y: Set<T>): boolean =>
  isSuperset(y, x);

export const areEqual = <T>(x: Set<T>, y: Set<T>): boolean =>
  isSuperset(x, y) && isSubset(x, y);
