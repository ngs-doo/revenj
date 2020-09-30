export const mapMap = <K, V>(map: Map<K, V>, mapKey: (key: K, path: string) => K, mapValue: (value: V, path: string) => V, path: string): Map<NonNullable<K>, NonNullable<V>> => {
  const ret: Map<NonNullable<K>, NonNullable<V>> = new Map();
  map.forEach((value, key) => {
    ret.set(mapKey(key, `${path}.key`)!, mapValue(value, `${path}.value`)!);
  });
  return ret;
};

export const mapSeq = <T>(item: Iterable<T> | null | undefined, fn: (value: T, path: string) => T, path: string): Array<NonNullable<T>> | undefined => {
  if (item == null) {
    return;
  }

  return Array.from(item).map((it, index) => fn(it, `${path}[${index}]`)!);
};
