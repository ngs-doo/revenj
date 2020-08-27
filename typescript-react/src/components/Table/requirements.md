# What do we need from new Tables

List of requirements:

- (+) Detail Row
- (+) Forms inside columns/rows
- (+) Header
- (+) Footer
- (+) Pagination (both remote and local skip + take)
- (+) Row states - styling (To garner focus depending on the state aka color - red, green, yellow...)
- (+) Row states - disabled, enabled, something darkside
- (+) Responsive (collapsable columns)

- Select column, select all
- Dynamic column sizing
- Column sorting
- Custom row/column renderers (pivots)

## Columns

We already have a list of row presets, do we need anything new?

- Link
- Currency
- Status
- Text
- Number
- Date
- Form

## Rows

I would rather opt for a singular row type which can receive a custom renderer which
enables us to do all cool things like column folding, dynamicly vertically sized rows, etc...

Allow rows to be of different heights

### Row states

Only a visual modifier to focus eyes on them (css mainly)

### Row folding and expanding 'detail' rows

I put these here together because they are really similar and use expanding to be represented

#### Conditions needed to show expandable rows

Row is expandable if it has one of these conditions met:

- A column was folded
- Extra Detail is available for the row
  - Another table (pivots for example)
  - Extra meta
  - A form
  - A notice... you name it

#### Folding

- When a cell can't possibly fit and be displayed correctly it has to be folded
- A cell definition has `folded` prop set to `true`
- Folded cells appear as children in the `<Folded>{...}</Folded>` section of the collapsable row
  - Status cells might affect row states maybe?
  - Cells can have their rendering altered in any of the two states by specifying render functions for both `cellRenderer` and `foldedRenderer` props

#### Extra detail

- When a row has some extra stuff associated with it, it can be shown inside the `<Detail>{...}</Detail>` section of the collapsable row
- Detail section can be anything and pretty much is governed by the custom render function passed to the `<Table>` `detailRenderer` prop

## Table

- Dynamic column sizing and folding (on fixed|dynamic breakpoints) - this is the only thing table needs to care about
- Resize causes possible folding of columns

## Where to put state

- Local state - well, a lot easier to implement, pain to optimize for rerenders via writing a bunch of manual `componentDidRecevieProps`
- Redux - predictable, well defined action set, and we get memoized selectors to optimize renders
  - Personally (@bigD) i would opt for this

## How to use new tables (API design ideas)

```tsx

const cellConfig: ICellConfig = {
  id: {
    type: CellType.Identifier,
  },
  name: {
    type: CellType.Identifier,
    sortBy: true,
  },
  status: {
    type: CellType.Status,
    folded: true,
  },
};

const rowConfig: IRowConfig = {
  detailRenderer: CustomDetailComponent
};

@table({
  name: 'SomeTable', // Table name under which the table will put its state resulting in state.table.SomeTablet
  dataSelector: someTableDataSelector, // a selector/adapter which feeds the data into the table
})
class SomeTable extends React.Component<> {
  render() {
    return (
      <Table
        cellConfig={cellConfig} // this one is required
        rowConfig={rowConfig} // this one is optional
      />
    );
  }
}
```
