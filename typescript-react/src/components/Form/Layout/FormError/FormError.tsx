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
    <span className={classNames(styles.message, { [styles.error]: type === ErrorType.Error, [styles.warning]: type === ErrorType.Warning, [styles.fabulous]: type === ErrorType.QAPink })}>
      <i className='fa fa-exclamation-circle' aria-hidden='true'/>{message}
    </span>
  );
};

FormError.displayName = 'FormError';
