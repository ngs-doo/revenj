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

const InputComponent: React.FC<InputFieldPublicProps> = ({ containerClassName, meta, input, type, autoComplete, className, id, ...props }) => (
  <div className={containerClassName}>
    <FormControl
      {...props}
      {...input}
      type={type}
      autoComplete={autoComplete}
      formNoValidate
      className={className}
      id={id}
    />
  </div>
);

export const Input = formField<InputFieldPublicProps>()(InputComponent);
