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

const InputComponent: React.FC<InputFieldPublicProps> = (props) => (
  <div className={props.containerClassName}>
    <FormControl
      {...props.input}
      type={props.type}
      autoComplete={props.autoComplete}
      formNoValidate
      className={props.className}
      id={props.id}
      onChange={(e: any) => props.onChange?.(e, e.target.value)}
      onBlur={(e: React.FocusEvent<HTMLInputElement>) => props.onBlur?.(e, e.target.value)}
      onFocus={(e: React.FocusEvent<HTMLInputElement>) => props.onFocus?.(e, e.target.value)}
    />
  </div>
);

export const Input = formField<InputFieldPublicProps>()(InputComponent);
