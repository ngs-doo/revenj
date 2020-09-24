import classNames from 'classnames';
import React from 'react';

import styles from './Loading.module.css';

interface ILoading {
  className?: string;
}

export const Loading: React.FC<ILoading> = ({ className }) => (
  <div className={classNames(styles.Container, className)}>
    <div className={styles.Loader}>
      <span className={classNames(styles.Dot, styles.First)} />
      <span className={classNames(styles.Dot, styles.Second)} />
      <span className={classNames(styles.Dot, styles.Third)} />
    </div>
  </div>
)
