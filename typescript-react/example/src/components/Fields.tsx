import classNames from 'classnames';
import React from 'react';
import {
  formField,
  FormField,
  FormFieldProps,
  PartialFormFieldProps,
  IBaseComponentProps,
  IExternalFormField,
  Validator,
  SHORT_MAX_VALUE,
  SHORT_MIN_VALUE,
} from 'revenj';
import { IFields } from 'revenj/dist/components/FieldRegistry/FieldRegistryContext';

import styles from './Fields.module.css';

export function SimpleFormField<T>(
  Field: React.ComponentType<PartialFormFieldProps<{}> & IBaseComponentProps<any>>,
  className?: string,
): React.ComponentType<IExternalFormField<any, any, T>> {
  const SimpleField = formField({})(Field);

  return ({ props, ...rest }: IExternalFormField<any, any, T>) => (
    <FormField
      {...rest}
      className={classNames(className, rest.className)}
      component={SimpleField}
      props={props}
    />
  );
}

export const Text = SimpleFormField<string>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='text'
  />
));

const Number = SimpleFormField<number>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='number'
  />
));

export const Decimal = Number;
export const Double = Number;
export const Float = Number;
export const Integer: React.FC<IExternalFormField<any, any, Int>> = ({ validate, ...props }) => {
  const validators = React.useMemo(() => [...(validate ?? []), Validator.isInteger], [validate]);
  return (
    <Number {...props} validate={validators} />
  );
}

export const Short: React.FC<IExternalFormField<any, any, Int>> = ({ validate, ...props }) => {
  const validators = React.useMemo(
    () => [...(validate ?? []), Validator.isInteger, Validator.inSegmentCreator({ min: SHORT_MIN_VALUE, max: SHORT_MAX_VALUE})()],
    [validate],
  );
  return (
    <Number {...props} validate={validators} />
  );
}

export const Long = Integer;

export const Currency: React.FC<IExternalFormField<any, any, Int>> = ({ validate, ...props }) => {
  const validators = React.useMemo(
    () => [...(validate ?? []), Validator.multipleOfFactorCreator('0.01', 'Cannot be more precise than 1 cent')],
    [validate],
  );
  return (
    <Number placeholder='$' {...props} validate={validators} />
  );
}

export const DatePicker = SimpleFormField<DateStr>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='date'
  />
));

export const DateTimePicker = SimpleFormField<TimestampStr>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='datetime-local'
  />
));

export const Checkbox = SimpleFormField<boolean>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='checkbox'
  />
));

export const Link = SimpleFormField<string>(({ input, meta, ...props }) => (
  <input
    {...props}
    {...input}
    className={styles.Input}
    type='url'
  />
));

export const Textarea = SimpleFormField<TextStr>(({ input, ...props }) => (
  <textarea
    {...props}
    {...input}
    className={styles.Input}
  ></textarea>
), styles.Textarea);

// Mock component, doesn't actually talk to S3
export const S3FileInput = SimpleFormField<S3>((_props) => {
  throw new Error('S3 not supported in demo application');
});


interface IBaseSelectProps<T> {
  options: Array<{ label: string; value: T; }>
  multi?: boolean;
}

type SelectProps<T> = FormFieldProps<IBaseSelectProps<T>> & IBaseComponentProps<T>;

function SelectField<T>({ className, options, multi, input, ...rest }: SelectProps<T>) {
  return (
    <select
      {...rest}
      {...input}
      multiple={multi}
      className={classNames(className, 'form-control')}
    >
      {
        options.map((option) => (
          <option value={String(option.value)} key={String(option.value)}>{option.label}</option>
        ))
      }
    </select>
  );
}

interface ISelectProps<T> extends IExternalFormField<any, any, T> {
  options: Array<{ label: string; value: T; }>;
}

function Select<T>(props: ISelectProps<T>) {
  const extraProps = React.useMemo(() => ({ options: props.options }), [props.options]);
  return (
    <FormField
      {...props}
      component={SelectField}
      props={extraProps}
    />
  );
}

function Multiselect<T>(props: ISelectProps<T>) {
  const extraProps = React.useMemo(() => ({ options: props.options, multi: true }), [props.options]);
  return (
    <FormField
      {...props}
      component={SelectField}
      props={extraProps}
    />
  );
}

interface IEnumSelectProps extends IExternalFormField<any, any> {
  enum: IEnumHelper<any>; // TODO: we need better enum types
  multi?: boolean;
}

export const EnumSelect = ({ enum: enumeration, multi, ...props}: IEnumSelectProps) => {
  const options = React.useMemo(
    () => enumeration.values().map((it) => ({ label: enumeration.getMeta(it)?.description ?? it, value: it })),
    [enumeration],
  );

  return multi ? (
    <Multiselect<any> {...props} options={options} />
  ) : (
    <Select<any> {...props} options={options} />
  );
}

export const FieldControls: IFields = {
  Checkbox,
  Currency,
  DatePicker,
  DateTimePicker,
  EnumSelect: EnumSelect as any,
  Link,
  LongText: Textarea,
  Integer,
  Long,
  Decimal,
  Double,
  Float,
  Short,
  Multiselect: Multiselect as any,
  S3FileInput,
  Select: Select as any,
  ShortText: Text,
  Text,
};
