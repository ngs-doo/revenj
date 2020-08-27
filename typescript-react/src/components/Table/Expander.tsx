import * as React from 'react';

import styles from './Expander.module.css';

interface IExpander {
  isExpanded: boolean;
}

export const Expander: React.FC<IExpander> = ({
  isExpanded,
}) => (
  <div className={styles.Expander}>
    { isExpanded
      ? <i className='fa fa-chevron-up' />
      : <i className='fa fa-chevron-down' /> }
  </div>
);
