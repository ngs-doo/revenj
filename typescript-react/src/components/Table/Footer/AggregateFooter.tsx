import * as React from 'react';

import { omit } from '../../../util/FunctionalUtils/FunctionalUtils';

interface IAggregateFooterProps<T> {
  Cell: React.ComponentClass<any>;
  field: string;
  data: T[];
  format?: (value: T) => string;
  aggregate: (data: T[], rows?: any) => T; // TODO fix the type, @luka.skukan @domagoj.cerjan This type def is a bunch of lies
}

export class AggregateFooter<T> extends React.PureComponent<IAggregateFooterProps<T>> {
  static defaultProps = {
    format: (value: any) => `${value}`,
  };

  render() {
    const { Cell, aggregate, data, field } = this.props;

    const cellProps = omit(this.props, 'Cell', 'format');

    return (
      <div>
        <Cell { ...cellProps } value={aggregate(data.map((row) => row[field]), data.map((row) => (row as any)._original))} />
      </div>
    );
  }
}
