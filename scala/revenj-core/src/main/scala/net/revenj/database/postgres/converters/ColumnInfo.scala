package net.revenj.database.postgres.converters

case class ColumnInfo(
    typeSchema: String,
    typeName: String,
    columnName: String,
    columnSchema: String,
    columnType: String,
    order: Short,
    nonNullable: Boolean,
    isMaintained: Boolean)
