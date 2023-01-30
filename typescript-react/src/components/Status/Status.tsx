import classNames from 'classnames';
import * as React from 'react';

import { CellStatus } from '../Table/interfaces';
import styles from './Status.module.css';

interface IStatusProps {
  color?: string;
  label: string | React.ReactElement;
  type?: CellStatus;
}

const getStatusStyle = (status: CellStatus) => {
  switch (status) {
  case CellStatus.Danger: return 'label-important';
  case CellStatus.Info: return 'label-info';
  case CellStatus.Success: return 'label-success';
  case CellStatus.Warning:
  default: return 'label-warning';
  }
};

export const Status: React.FC<IStatusProps> = ({color, label, type}) => {
  return (
    <span
      className={classNames(styles.Label, type != null ? getStatusStyle(type) : undefined)}
      style={color ? { color: color } : undefined}
    >
      { label }
    </span>
  );
}
