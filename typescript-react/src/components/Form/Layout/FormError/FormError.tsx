import classNames from 'classnames';
import * as React from 'react';

import { ErrorType } from '../../interfaces';

import styles from './FormError.module.css';

interface IFormError {
  message: string | JSX.Element;
  type?: ErrorType;
}

export const FormError = ({ message, type }: IFormError) => {
  if (message == null) {
    console.warn('Form error message not supplied. Skipping form error.');
    return null;
  }
  return (
    <span className={classNames(styles.Message, { [styles.Error]: type === ErrorType.Error, [styles.Warning]: type === ErrorType.Warning, [styles.Fabulous]: type === ErrorType.QAPink })}>
      <i className={classNames('fa fa-exclamation-circle', styles.Icon)} aria-hidden='true'/>{message}
    </span>
  );
};

FormError.displayName = 'FormError';
