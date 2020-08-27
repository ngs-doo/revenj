import * as React from 'react';

import styles from './NoData.module.css';

export interface INoDataProps {
  noDataContent?: string;
}

export class NoData extends React.PureComponent<INoDataProps> {
  public static defaultProps: INoDataProps = {noDataContent: 'No data found'};

  public render() {
    return (
      <div className={styles.noData}>{this.props.noDataContent}</div>
    );
  }
}
