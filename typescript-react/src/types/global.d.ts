declare interface INumberFormatter {
  formatNumber(num: any, pattern?: string): string;
  parseNumber(numberString: number | string, pattern: string): number | string;
  fromMachineFormat(numberString: number | string, pattern: string): string;
  parseNumberIncludingMachineFormat(numberString: number | string, pattern: string): number | string;
  validatePattern(pattern: string): /* INumberFormat */ | null | Error;
  constructFormat(pattern: string): /* INumberFormat */ | null;
  isOnlyNumbers(str: string): boolean;
}

// Custom branded types
declare type DateStr = string & { __DateStrBrand__: void };
declare type TimestampStr = string & { __TimestampStrBrand__: void };
declare type Long = number & { __LongBrand__: void };
declare type Int = number & { __IntBrand__: void };
declare type Short = number & { __ShortBrand__: void };
declare type Double = number & { __DoubleBrand__: void };
declare type DecimalStr = string & { __DecimalStrBrand__: void };
declare type BinaryStr = string & { __BinaryStrBrand__: void };
declare type UUID = string & { __UUIDBrand__: void };
declare type URLStr = string & { __URLBrand__: void };
declare type TextStr = string & { __TextBrand__: void };
declare type IPAddress = string & { __IPAddressBrand__: void };
declare type MoneyStr = string & { __MoneyStrBrand__: void };
declare type XmlStr = string & { __XmlBrand__: void };
declare type TreePathStr = string & { __TreePathBrand__: void };
declare type Streaming<T> = Iterable<T>;
declare type ResultSet = any[][];
declare type S3 = {
  readonly bucket: string;
  readonly key: string;
  readonly length: Long;
  readonly name?: string;
  readonly mimeType?: string;
  readonly metadata?: { [key: string]: string; };
};
declare type Point = {
  x: Double;
  y: Double;
}

declare type List<T> = Array<T>;
declare type Detail<T> = Array<T>; // TODO: Improve?

declare interface IEnumEntryMeta {
  description: string;
}

declare interface IEnumMeta {
  [key: string]: IEnumEntryMeta;
}

declare interface IEnumHelper<T> {
  forDescription: (label: string) => T | undefined;
  getMeta: (t: T) => IEnumEntryMeta | undefined;
  values: () => Array<T>;
}

declare interface IObjectAny {
  [key: string]: any;
}

declare interface IFunctionAny {
  (...args: any[]): any;
}

declare type JSONAny = IObjectAny | any[] | string | null; // TODO use this instead of object where applicable

declare type Diff<T extends string, U extends string> = ({ [P in T]: P } & { [P in U]: never } & { [x: string]: never })[T];

// #region Deep Paths, shamelessly stolen from https://github.com/Microsoft/TypeScript/issues/12290
type KeyOf<T> = keyof T;

// TODO: FIXME: @bigd -> do proper lens-like drilldown for indexable types instead of this heavyweight solution
interface DeepKeyOfArray<T> extends Array<string> {
  ['0']?: KeyOf<T>;
  ['1']?: this extends {
    ['0']?: infer K0
  } ? K0 extends KeyOf<T> ? KeyOf<T[K0]> : never : never;
  ['2']?: this extends {
    ['0']?: infer K0;
    ['1']?: infer K1;
  } ? K0 extends KeyOf<T> ? K1 extends KeyOf<T[K0]> ? KeyOf<T[K0][K1]> : never : never : never;
  ['3']?: this extends {
    ['0']?: infer K0;
    ['1']?: infer K1;
    ['2']?: infer K2;
  } ? K0 extends KeyOf<T> ? K1 extends KeyOf<T[K0]> ? K2 extends KeyOf<T[K0][K1]> ? KeyOf<T[K0][K1][K2]> : never : never : never : never;
}

type ArrayHasIndex<MinLenght extends string> = { [K in MinLenght]: any; };

declare type DeepTypeOfArray<T, L extends DeepKeyOfArray<T>> =
  L extends ArrayHasIndex<'4'> ? any :
  L extends ArrayHasIndex<'3'> ? T[L['0']][L['1']][L['2']][L['3']] :
  L extends ArrayHasIndex<'2'> ? T[L['0']][L['1']][L['2']] :
  L extends ArrayHasIndex<'1'> ? T[L['0']][L['1']] :
  L extends ArrayHasIndex<'0'> ? T[L['0']] :
  T;

declare type DeepKeyOf<T> = DeepKeyOfArray<T> | KeyOf<T>;

declare type DeepTypeOf<T, L extends DeepKeyOf<T>> =
  L extends DeepKeyOfArray<T> ? DeepTypeOfArray<T, L> :
  L extends KeyOf<T> ? T[L] :
  never;

declare type RootType<T> = T extends any[] ? T[0] : T;
// #endregion
