import classNames from 'classnames';
import * as React from 'react';
import { Field } from 'redux-form';

import { get } from '../../util/FunctionalUtils/FunctionalUtils';
import * as Validator from '../validation';
import { I18nContext } from '../I18n/I18n';
import { localizeTextIfMarked } from '../I18n/service';
import {
  FormContext,
  FormControlDescriptor,
  FormControlContext,
  FormValueContext,
} from './Context';
import { FormType } from './interfaces';

import styles from './FormField.module.css';

export interface IFormFieldInternalProps<T> {
  form: string;
  formType?: FormType; // DSL-injected only
  initialValues?: Partial<T>;
  sectionName?: string;
  change: <FK extends DeepKeyOf<T>>(field: FK, value: DeepTypeOf<T, FK>) => void;
}

export interface IFormFieldPublicProps<T, K extends DeepKeyOf<T>, P, V = any> {
  autoComplete?: string;
  className?: string; // Maps to container class name
  componentClassName?: string;
  labelClassName?: string;
  hideRequiredStar?: boolean;
  component: React.ComponentType<P>;
  description?: string;
  disabled?: boolean | ((values: Partial<T>) => boolean);
  readOnly?: boolean | ((values: Partial<T>) => boolean);
  inline?: boolean;
  label?: string;
  name: K;
  placeholder?: string;
  required?: boolean | ((values: Partial<T>) => boolean)
  validate?: Array<Validator.Validator<DeepTypeOf<T, K> & V, T, P>>;
  warn?: Array<Validator.Validator<DeepTypeOf<T, K> & V, T, P>>;
  defaultValue?: DeepTypeOf<T, K> & V;
  prefillOnMount?: boolean;
  clearOnUnmount?: boolean;
  // When not specified or true/true-returning, the field will be visible, otherwise it will not render
  visible?: boolean | ((values: Partial<T>) => boolean);
  props?: Partial<P>;
  format?(value?: DeepTypeOf<T, K> & V): DeepTypeOf<T, K> & V;
  parse?(value?: DeepTypeOf<T, K> & V): DeepTypeOf<T, K> & V;
  normalize?(value?: DeepTypeOf<T, K> & V, previousValue?: DeepTypeOf<T, K> & V, allValues?: Partial<T>, allPreviousValues?: Partial<T>): DeepTypeOf<T, K> & V;
  normalizeOnBlur?(value?: DeepTypeOf<T, K> & V): DeepTypeOf<T, K> & V;
  onChange?(event: React.ChangeEvent<any> | undefined, value: DeepTypeOf<T, K> & V | undefined, oldValue: DeepTypeOf<T, K> & V | undefined, change: (key: DeepKeyOf<T>, value: any) => any): void;
  onBlur?(event: React.FocusEvent<any> | undefined, value: DeepTypeOf<T, K> & V | undefined, change: (key: DeepKeyOf<T>, value: any) => any): void;
  onFocus?(event: React.FocusEvent<any> | undefined, name?: string): void;
}

export interface IFormFieldInternal<T, K extends DeepKeyOf<T>, P, V = any> extends
  IFormFieldPublicProps<T, K, P, V>,
  IFormFieldInternalProps<T> {}

export type IExternalFormField<T, K extends DeepKeyOf<T>, V = any, P = any> = Omit<IFormFieldPublicProps<T, K, P, V>, 'component'>;

interface IFormFieldState {
  validate?: Array<Validator.Validator<any, any, any>>;
}

export class FormFieldInternal<T, K extends DeepKeyOf<T>, P = any, V = any> extends React.PureComponent<IFormFieldInternal<T, K, P, V>, IFormFieldState> {
  static defaultProps: Partial<IFormFieldInternal<any, any, any>> = {
    autoComplete: 'nope',
    clearOnUnmount: false,
    prefillOnMount: true,
    props: {},
  };

  public state = {
    validate: this.props.required
      ? [Validator.nonEmpty].concat(this.props.validate || [])
      : this.props.validate,
  };

  private onBlurNormalizeTimeout: any;

  public componentDidMount() {
    const { formType, defaultValue, prefillOnMount, initialValues } = this.props;

    if (formType != null && formType !== FormType.Create) {
      return; // We do not want to prefill on DSL forms that have external defaults
    }

    if (prefillOnMount && defaultValue != null) {
      if (get(initialValues, this.getNameWithSection() as any) != null) {
        return; // We don't set the default value if there is already something in there, that's what default means
      }

      this.change(defaultValue);
    }
  }

  public componentWillUnmount() {
    const { clearOnUnmount } = this.props;

    if (clearOnUnmount) {
      this.change(null);
    }

    if (this.onBlurNormalizeTimeout) {
      window.clearTimeout(this.onBlurNormalizeTimeout);
    }
  }

  public componentDidUpdate(prevProps: IFormFieldInternal<T, K, P>) {
    if (prevProps.required !== this.props.required || prevProps.validate !== this.props.validate) {
      const validate = this.props.required
        ? [Validator.nonEmpty].concat(this.props.validate || [])
        : this.props.validate;
      this.setState({ validate });
    }
  }

  public render() {
    const {
      change,
      format,
      parse,
      className,
      componentClassName,
      component,
      description,
      disabled,
      inline,
      label,
      labelClassName,
      placeholder,
      props,
      onFocus,
      warn,
      required,
      readOnly,
      normalize,
      autoComplete,
    } = this.props;

    const containerClassName = classNames(
      className,
      styles.Field,
      { [styles.Required]: this.isRequired() && !this.props.hideRequiredStar && label != null && label.trim().length > 0 },
    );

    const extraProps = {
      change,
      containerClassName: classNames(containerClassName, { [styles.InlineField]: inline }),
      description,
      hideLabel: label == null || label === '',
      inline,
      isRootFieldRequired: required,
      label,
      labelClassName
    };

    return (
      <Field
        name={this.getStringifiedName()}
        placeholder={placeholder}
        component={component as any}
        disabled={disabled || readOnly}
        validate={this.state.validate}
        format={format}
        parse={parse}
        normalize={normalize}
        onChange={this.onChange}
        onBlur={this.onBlur}
        onFocus={onFocus}
        warn={warn}
        autoComplete={autoComplete}
        props={extraProps}
        className={classNames(componentClassName, styles.Component)}
        // Component is in charge of getting its additional props in the general case, but API allows for
        // passing in extras via props key
        {...props}
      />
    );
  }

  private onChange = (event: React.ChangeEvent<any> | undefined, value: DeepTypeOf<T, K> & V, originalValue: DeepTypeOf<T, K> & V, _name?: string) => {
    if (this.props.onChange) {
      this.props.onChange(event, value, originalValue, this.changeInForm);
    }
  }

  private onBlur = (event: React.FocusEvent<any> | undefined, originalValue: DeepTypeOf<T, K> & V, _name?: string) => {
    const value = this.props.normalizeOnBlur ? this.props.normalizeOnBlur(originalValue) : originalValue;
    if (this.props.onBlur) {
      this.props.onBlur(event, value, this.changeInForm);
    }

    if (this.props.normalizeOnBlur) {
      this.onBlurNormalizeTimeout = window.setTimeout(() => {
        this.change(value);
      }, 0);
    }
  }

  private isRequired = (): boolean => (this.state.validate || []).includes(Validator.nonEmpty);

  private getStringifiedName = (): string => {
    const { name } = this.props;
    return Array.isArray(name) ? name.filter((it) => it != null).join('.') : name as string;
  }

  private getNameWithSection = () => {
    const stringifiedName = this.getStringifiedName();
    const { sectionName } = this.props;
    return sectionName ? `${sectionName}.${stringifiedName}` : stringifiedName;
  }

  private change = (value: DeepTypeOf<T, K> & V | null | undefined): void => {
    const { change } = this.props;
    change(this.getNameWithSection() as unknown as DeepKeyOf<T>, value as DeepTypeOf<T, K>);
  }

  private changeInForm = <K extends DeepKeyOf<T>>(key: K, value: DeepTypeOf<T, K>) =>
    this.props.change(key, value)
}

export function FormField<T, K extends DeepKeyOf<T>, P = any, V = any>(props: IFormFieldPublicProps<T, K, P, V>) {
  const context = React.useContext(FormContext);
  const configContext = React.useContext(FormControlContext);
  const values = React.useContext(FormValueContext);
  const { localize } = React.useContext(I18nContext);

  if (context == null) {
    return null;
  }

  const flatName = Array.isArray(props.name) ? props.name.join('.') : props.name as string;
  const flatSection = Array.isArray(context.sectionName) ? context.sectionName.join('.') : context.sectionName as string;
  const fullName = flatSection != null ? `${flatSection}.${flatName}` : flatName;
  const configProps: FormControlDescriptor<any, any, T> = get(configContext, fullName as any, {});
  const visible = configProps?.visible ?? props.visible;
  //TODO: temporarly disable this until expected behavior is figured out
  const required = configProps?.required ?? (props.required/* && context.forceOptional !== true*/);
  const disabled = configProps?.disabled ?? props.disabled;
  const readOnly = configProps?.readOnly ?? context.readOnly ?? props.readOnly;
  const rawLabel = configProps?.label ?? props.label;
  const inline = configProps?.inline ?? props.inline ?? context.defaultInline;

  if (visible === false || (typeof visible === 'function' && !visible(values))) {
    return null;
  }

  const spreadProps = {
    ...props.props,
    ...configProps.props,
  };

  //respect non-null clearOnUnmount value when set (even false)
  const unmountValue = props.clearOnUnmount != null || context.clearOnUnmount != null || configProps.clearOnUnmount != null
    ? (props.clearOnUnmount != null ? props.clearOnUnmount : context.clearOnUnmount != null ? context.clearOnUnmount : configProps.clearOnUnmount)
    : context.sectionName == null;

  return (
    <FormFieldInternal
      {...props}
      {...context}
      {...(configProps ?? {}) as any}
      props={spreadProps}
      required={typeof required === 'function' ? required(values) : required}
      readOnly={typeof readOnly === 'function' ? readOnly(values) : readOnly}
      disabled={typeof disabled === 'function' ? disabled(values) : disabled}
      label={rawLabel ? localizeTextIfMarked(localize, rawLabel) : undefined}
      inline={inline}
      // Fields that can appear and vanish must be able to clear themselves when unmounting, we don't want stale invisible values
      clearOnUnmount={unmountValue}
    />
  );
}
