export enum ColumnType {
  'Array' = 'Array',
  'Binary' = 'Binary',
  'Blob' = 'Blob',
  'Boolean' = 'Boolean',
  'Date' = 'Date',
  'Decimal' = 'Decimal',
  'Float' = 'Float',
  'Int' = 'Int',
  'Long' = 'Long',
  'Null' = 'Null',
  'Short' = 'Short',
  'SQL' = 'SQL',
  'String' = 'String',
  'XML' = 'XML',
  'Time' = 'Time',
  'Timestamp' = 'Timestamp',
  'Unknown' = 'Unknown',
  'Url' = 'Url',
}

export const numericResultSetTypes: Set<ColumnType> = new Set([
  ColumnType.Decimal,
  ColumnType.Float,
  ColumnType.Int,
  ColumnType.Long,
  ColumnType.Short,
]);

export class TypescriptResultSet {
  public static serialize(rs: TypescriptResultSet): any[][] {
    if (rs == null || rs.columns == null || rs.types == null || rs.rows == null) {
      throw new Error(`Expected a ResultSet, but received ${rs}`);
    }
    return [rs.columns, rs.types, ...rs.rows];
  }

  public static deserialize(rs: any[][]): TypescriptResultSet {
    if (rs == null || rs.length < 2) {
      throw new Error(`Expected a result set of shape [columns[], types[], ...values[]], but received ${rs}`);
    }
    return new TypescriptResultSet(
      rs[0],
      rs[1],
      rs.slice(2),
    );
  }

  public constructor(
    public readonly columns: string[],
    public readonly types: ColumnType[],
    public readonly rows: any[][] = [],
  ) {}

  public toObjects(): IObjectAny[] {
    return this.rows.map((fields) => {
      return fields.reduce((result, field, index) => ({ ...result, [this.columns[index]]: field }), {});
    });
  }
}
