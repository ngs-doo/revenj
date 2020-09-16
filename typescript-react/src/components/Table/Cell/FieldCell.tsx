import classNames from 'classnames';
import * as React from 'react';

import { ICellProps } from '../interfaces';

import styles from './Cells.module.css';

export class FieldCell<T, V> extends React.PureComponent<ICellProps<T, V>> {
  public render() {
    const {
      className,
      field,
      index,
      fieldProps,
      sectionName,
    } = this.props;

    const { component: Component, className: fieldClassName, name, onChange, getOptions, onBlur, ...props } = fieldProps!;
    const options = getOptions ? getOptions(index) : undefined;

    return (
      <Component
        {...props}
        name={name ? name(index) : `${sectionName}[${index}].${field}`}
        onChange={onChange ? onChange(index) : undefined}
        onBlur={onBlur ? onBlur(index) : undefined}
        className={classNames(styles.FieldCell, className, fieldClassName)}
        label={undefined}
        {...{options}}
      />
    );
  }
}
