import classNames from 'classnames';
import * as React from 'react';
import Button from 'react-bootstrap/Button';
import { connect } from 'react-redux';
import { compose } from 'recompose';
import { Dispatch } from 'redux';
import {
  getFormSubmitErrors,
  getFormValues,
  reduxForm,
  ConfigProps,
  Form as ReduxForm,
  FormErrors,
  InjectedFormProps,
} from 'redux-form';

import {
  FormContextProvider,
  FormValueContext,
  IFormContext,
} from './Context';
import { FormError } from './GlobalFormError';
import {
  ErrorType,
  FormType,
} from './interfaces';
import { deepKeys } from '../../util/FunctionalUtils/FunctionalUtils';

import styles from './Form.module.css';

const genericError = 'Please correct all the required fields.';

const scrollToFirstError = (errors: FormErrors<FormData, ErrorType> | undefined = {}) => {
  const fieldNames = deepKeys(errors);

  // select library and datepicker don't have a name attribute on input, so the only way to target them is through data-qa-element-id
  // data-qa-element-id has an unfortunate naming, and changing it now would be a breaking change, would be much prettier if it was data-element-id
  // adding another data attribute would just duplicate stuff, as it would have the same value as data-qa-element-id
  for (const fieldName of fieldNames) {
    const el = document.querySelectorAll(`[name="${fieldName}"], [data-qa-element-id="${fieldName}"] input`)[0] as HTMLElement;

    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      el.focus({ preventScroll: true });
      break;
    }
  }
}

interface IFormStateProps<T> {
  submitErrors?: FormErrors<T>;
  values: T;
}

export type SubmitHandler<T> = (value: T, dispatch?: Dispatch<any>, props?: IForm<T>) => Promise<T>;

export interface IFormPublicProps<T> {
  autoComplete?: boolean;
  disableSubmit?: boolean;
  className?: string;
  externalError?: string;
  staticButtonsOnMobile?: boolean;
  formType?: FormType;
  hideButtons?: boolean;
  submitButtonText?: string;
  submitButtonClassName?: string;
  cancelButtonText?: string;
  readOnly?: boolean;
  renderError?: (error: string) => JSX.Element | string;
  onSubmit: SubmitHandler<T>;
  onCancel?: () => void;
}

interface IForm<T> extends IFormPublicProps<T>, InjectedFormProps<T, IForm<T>, {}>, IFormStateProps<T> {}

interface IFormState<T> extends IFormContext<T> {}

const mapState = (state: IObjectAny, ownProps: IFormPublicProps<any> & ConfigProps<any, any, any>): IFormStateProps<any> => ({
  submitErrors: getFormSubmitErrors(ownProps.form!)(state),
  values: getFormValues(ownProps.form!)(state),
});

class FormBare<T> extends React.Component<IForm<T>, IFormState<T>> {
  /* tslint:disable member-ordering */
  private change = <K extends DeepKeyOf<T>>(key: K, value: DeepTypeOf<T, K>) =>
    Array.isArray(key) ? this.props.change!(key.join('.'), value) : this.props.change!(key as string, value)

  public state: IFormState<T> = {
    change: this.change,
    form: this.props.form!,
    formType: this.props.formType,
    initialValues: this.props.initialValues,
    readOnly: this.props.readOnly,
    reset: this.props.reset!,
    submitErrors: this.props.submitErrors,
  };

  static defaultProps: Partial<IForm<any>> = {
    cancelButtonText: 'Cancel',
    staticButtonsOnMobile: true,
    submitButtonText: 'Submit',
  };

  public componentDidUpdate(prevProps: IForm<T>) {
    if (this.props.form !== prevProps.form) {
      this.setState({ form: this.props.form!, reset: this.props.reset! });
    }

    if (this.props.initialValues !== prevProps.initialValues) {
      this.setState({ initialValues: this.props.initialValues });
    }

    if (this.props.readOnly !== prevProps.readOnly) {
      this.setState({ readOnly: this.props.readOnly });
    }

    if (this.props.formType !== prevProps.formType) {
      this.setState({ formType: this.props.formType });
    }

    if (this.props.submitErrors !== prevProps.submitErrors) {
      this.setState({ submitErrors: this.props.submitErrors });
    }
  }

  public render() {
    const {
      autoComplete,
      className,
      submitButtonText,
      submitButtonClassName,
      cancelButtonText,
      children,
      disableSubmit,
      handleSubmit,
      hideButtons,
      staticButtonsOnMobile,
      error,
      externalError,
      submitFailed,
      submitting,
      onCancel,
      onSubmit,
      values,
      warning,
      renderError,
    } = this.props;

    const errorText = error || externalError || genericError;

    return (
      <ReduxForm
        className={classNames(className, styles.FormComponent)}
        onSubmit={handleSubmit!(onSubmit as any) as any} // TODO: Figure out where I messed up the typings when moving to a new project
        autoComplete={autoComplete ? 'on' : 'off'}
      >
        <FormContextProvider value={this.state}>
          <FormValueContext.Provider value={values}>
            {children}
          </FormValueContext.Provider>
        </FormContextProvider>

        <section className={styles.Errors}>
          <FormError
            className='jsFormError'
            trigger={error != null || submitFailed || externalError != null}
            error={errorText && renderError ? renderError(String(errorText)) : String(errorText)}
          />
          <FormError className='jsFormWarning' trigger={warning != null} error={String(error)} type={ErrorType.Warning} />
        </section>

        {
          !hideButtons ? (
            <section className={classNames(styles.Buttons, { [styles.Static]: staticButtonsOnMobile })}>
              <Button
                className={classNames(styles.Button, submitButtonClassName)}
                type='submit'
                disabled={submitting || disableSubmit}
              >
                { submitting ? 'Submitting...' : submitButtonText }
              </Button>
              {
                onCancel != null ? (
                  <Button
                    className={styles.Button}
                    type='button'
                    variant='outline-primary'
                    onClick={onCancel}
                  >
                    {cancelButtonText}
                  </Button>
                ) : null
              }
            </section>
          ) : null
        }
      </ReduxForm>
    );
  }
}

const FormUntyped = compose(
  reduxForm({ onSubmitFail: scrollToFirstError }),
  connect(mapState), // Must happen after so values are not overriden
)(FormBare as any) as unknown as React.ComponentClass<IFormPublicProps<any> & ConfigProps<any, any, any>>;

export class Form<T> extends React.PureComponent<IFormPublicProps<T> & ConfigProps<T, IFormPublicProps<T>, {}>> {
  public render() {
    return (
      <FormUntyped {...this.props} />
    );
  }
}
