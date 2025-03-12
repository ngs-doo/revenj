import { TypescriptResultSet } from '../ResultSet/ResultSet';
import { CurrencyFormatter } from '../util/Formatters/CurrencyFormatter';
import { DateFormatter } from '../util/Formatters/DateFormatter';
import { DateTimeFormatter } from '../util/Formatters/DateTimeFormatter';
import {
  MachineDateFormat,
  MachineDateTimeFormat,
} from '../util/time/time';
import {
  Marshaller,
  MiddlewareStep,
} from './Marshaller';
import * as Assert from './assert';

export const assertDate = (value: DateStr): DateStr =>
  DateFormatter.formatPresentationalDate(value, MachineDateFormat)!;

export const assertDateTime = (value: TimestampStr): TimestampStr =>
  DateTimeFormatter.formatDateTime(value, MachineDateTimeFormat) as TimestampStr;

export const assertCurrency = (value: MoneyStr): MoneyStr =>
  CurrencyFormatter.machineFormatCurrency(value) as MoneyStr;

export const isNullOrEmpty = (value: any): boolean => value == null || value === '';

export interface IBootConfig {
  after?: (Marshaller: Marshaller) => Marshaller;
  before?: (marshaller: Marshaller) => Marshaller,
}

export const initialize = ({ before, after }: IBootConfig) => {
  const marshaller = before ? before(Marshaller.getInstance()) : Marshaller.getInstance();
  const result = marshaller
    // Default required undefined objects (non-built-in) to empty objects to avoid having to do defaults
    .registerSerializerMiddleware(
      () => ({}),
      (it, _, isNonNullable, isBuiltInType) => isNonNullable && !isBuiltInType && it == null,
    )
    // Default undefined required collections to empty collection instance to avoid having to do defaults
    .registerSerializerMiddleware(
      () => [],
      (it, typeName, isNonNullable) => isNonNullable && it == null && ['list', 'array', 'streaming'].includes(typeName.toLocaleLowerCase()),
    )
    .registerSerializerMiddleware(
      () => new Set(),
      (it, typeName, isNonNullable) => isNonNullable && it == null && typeName.toLocaleLowerCase() === 'set',
    )
    .registerSerializerMiddleware(
      () => new TypescriptResultSet([], []),
      (it, typeName, isNonNullable) => isNonNullable && it == null && typeName.toLocaleLowerCase() === 'resultset',
    )
    .registerSerializerMiddleware(
      () => null,
      (it, typeName, isNonNullable) => !isNonNullable && it === '' && typeName.toLocaleLowerCase() === 'money',
    )
    // Ensure sets, since forms will actually produce arrays
    .registerSerializerMiddleware(
      (it) => new Set(it as any[]),
      (it, typeName) => typeName.toLocaleLowerCase() === 'set' && it != null && Array.isArray(it),
    )
    // Default booleans into false, so that we don't have to do redux-form active changes (quite expensive)
    .registerSerializerMiddleware(
      () => false,
      (it, typeName, isNonNullable) => isNonNullable && typeName.toLocaleLowerCase() === 'boolean' && it == null,
    )
    // Don't actually _send_ false booleans, it's silly and wastes space when talking to Instafin
    .registerSerializerMiddleware(
      () => undefined,
      (it, typeName, isNonNullable) => isNonNullable && typeName.toLocaleLowerCase() === 'boolean' && it === false,
      MiddlewareStep.After,
    )
    // Optional numbers can be "NaN" or empty string, and it chokes the whole thing
    .registerSerializerMiddleware(
      (_it) => undefined,
      (it, typeName, isNonNullable) => ['Int', 'Long', 'Short', 'Decimal', 'Double', 'Float', 'Money'].includes(typeName) && !isNonNullable && (it === '' || Number.isNaN(it as any)),
    )
    // Defaulting required numbers
    .registerSerializerMiddleware(
      (_it) => 0,
      (it, typeName, isNonNullable) => ['Int', 'Long', 'Short', 'Decimal', 'Double', 'Float', 'Money'].includes(typeName) && isNonNullable && it == null,
    )
    // Number unpacking from string fields
    .registerSerializerMiddleware(
      (it: string) => Number.parseInt(it, 10),
      (it, typeName) => it != null && typeof it === 'string' && ['Int', 'Long', 'Short'].includes(typeName),
    )
    .registerSerializerMiddleware(
      (it: string) => Number.parseFloat(it),
      (it, typeName) => it != null && typeof it === 'string' && ['Long', 'Double'].includes(typeName),
    )
    // Flatten optional empty strings into nothing on serialize
    .registerSerializerMiddleware(
      () => undefined,
      (it, typeName, isNonNullable) => it === '' && !isNonNullable && ['string', 'text', 'date', 'timestamp', 'binary'].includes(typeName.toLocaleLowerCase()),
    )
    // Flatten optional empty strings into nothing on deserialize
    .registerDeserializerMiddleware(
      () => undefined,
      (it, typeName, isNonNullable) => it === '' && !isNonNullable && ['string', 'text', 'date', 'timestamp', 'binary'].includes(typeName.toLocaleLowerCase()),
    )
    // Ensure required missing boolean fields are set to false (BE omits false to save up on cube config payloads)
    .registerDeserializerMiddleware(
      () => false,
      (it, typeName, isNonNullable) => it == null && isNonNullable && typeName.toLocaleLowerCase() === 'boolean',
    )
    // Ensure required missing short/int/long/decimal/double fields are set to 0 (BE omits false to save up on cube config payloads)
    .registerDeserializerMiddleware(
      () => 0,
      (it, typeName, isNonNullable) => it == null && isNonNullable && ['long', 'int', 'short', 'decimal', 'double', 'float', 'money'].includes(typeName.toLocaleLowerCase()),
      MiddlewareStep.Before,
    )
    // Ensure optional missing short/int/long/decimal/double fields are set to undefined (BE omits false to save up on cube config payloads)
    .registerDeserializerMiddleware(
      () => undefined,
      (it, typeName, isNonNullable) => it === '' && !isNonNullable && ['long', 'int', 'short', 'decimal', 'double', 'float', 'money'].includes(typeName.toLocaleLowerCase()),
      MiddlewareStep.Before,
    )
    // Collection types
    .registerSerializer<any[], any[]>('List', Assert.assertArray)
    .registerDeserializer<any[], any[]>('List', Assert.assertArray)
    .registerSerializer<any[], any[]>('Array', Assert.assertArray)
    .registerDeserializer<any[], any[]>('Array', Assert.assertArray)
    .registerSerializer<Set<any>, any[]>('Set', Assert.serializeSet)
    .registerDeserializer<any[], Set<any>>('Set', Assert.deserializeSet)
    .registerFormatter<Streaming<any>>('Streaming', Assert.assertIterable)
    .registerSerializer<TypescriptResultSet, ResultSet>('ResultSet', Assert.serializeResultSet)
    .registerDeserializer<ResultSet, TypescriptResultSet>('ResultSet', Assert.deserializeResultSet)
    .registerSerializer<Map<any, any>, IObjectAny>('Map', Assert.serializeMap)
    .registerDeserializer<IObjectAny, Map<any, any>>('Map', Assert.deserializeMap)
    // Common types with special handling
    .registerFormatter<DateStr>('Date', assertDate)
    .registerFormatter<TimestampStr>('Timestamp', assertDateTime)
    .registerFormatter<Int>('Int', Assert.assertInt)
    .registerFormatter<Long>('Long', Assert.assertLong)
    .registerFormatter<Double>('Double', Assert.assertFloatingPoint)
    .registerFormatter<Float>('Float', Assert.assertFloatingPoint)
    .registerFormatter<DecimalStr>('Decimal', Assert.assertDecimal)
    .registerFormatter<MoneyStr>('Money', assertCurrency)
    .registerSerializer<BinaryStr>('Binary', Assert.serializeBinary)
    .registerDeserializer<BinaryStr>('Binary', Assert.deserializeBinary)
    .registerFormatter<S3>('S3', Assert.assertS3);

    return after ? after(result) : result;
};
