import classNames from 'classnames';
import * as React from 'react';

import styles from './Header.module.css';

interface IHeader {
  className?: string;
  title: string;
}

export const Header: React.FC<IHeader> = ({ children, className, title }) => (
  <div className={classNames(styles.Header, className)}>
    <h1 className={styles.Title}>{title}</h1>
    {children}
  </div>
);
