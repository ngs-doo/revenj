import * as React from 'react';
import FormControl from 'react-bootstrap/FormControl';

import {
  formField,
  FormFieldProps,
  IBaseComponentProps,
} from '../decorators/formField';

export interface IInput extends IBaseComponentProps<string | number> {
  type?: 'text' | 'number' | 'date' | 'email' | 'url' | 'password' | 'search';
}

export type InputFieldPublicProps = FormFieldProps<IInput>;

const InputComponent: React.FC<InputFieldPublicProps> = ({ containerClassName, meta, input, type, autoComplete, className, id, onChange, onBlur, onFocus, ...props }) => (
  <div className={containerClassName}>
    <FormControl
      {...input}
      {...props}
      type={type}
      autoComplete={autoComplete}
      formNoValidate
      className={className}
      id={id}
      onChange={(e: any) => onChange?.(e, e.target.value)}
      onBlur={(e: React.FocusEvent<HTMLInputElement>) => onBlur?.(e, e.target.value)}
      onFocus={(e: React.FocusEvent<HTMLInputElement>) => onFocus?.(e, e.target.value)}
    />
  </div>
);

export const Input = formField<InputFieldPublicProps>()(InputComponent);
