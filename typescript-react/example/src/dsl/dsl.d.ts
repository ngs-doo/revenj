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
