import * as React from 'react';

import { ICellProps } from '../interfaces';

export class CustomCell<T, V> extends React.PureComponent<ICellProps<T, V>> {
  public render() {
    const {
      customComponent,
      original,
    } = this.props;

    return customComponent ? customComponent(original) : null;
  }
}
