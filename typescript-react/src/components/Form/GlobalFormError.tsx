import * as React from 'react';
import { CSSTransition } from 'react-transition-group';

import { get, isObject } from '../../util/FunctionalUtils/FunctionalUtils';
import { FormContext } from './Context';
import { FormError as FormErrorElement } from './Layout/FormError/FormError';
import { ErrorType, IFormError } from './interfaces';

import styles from './Error.module.css';

interface IFormErrors {
  className?: string;
  trigger?: boolean;
  errors?: IFormError[];
  error?: string | JSX.Element;
  type?: ErrorType;
}

const FormErrorMessage = (props: IFormErrors) => {
  const { errors, error, type } = props;
  const messages = Array.isArray(errors)
    ? errors!
    : ([{ message: error }] as IFormError[]);
  return (
    <div className={styles.FormErrorContainer}>
      {messages.map((singleError: IFormError, index: number) => (
        <FormErrorElement
          message={singleError.message! || ''}
          type={type}
          key={index}
        />
      ))}
    </div>
  );
};

const defaultProps: Partial<IFormErrors> = {
  type: ErrorType.Error,
};

export const FormError: React.FC<IFormErrors> = (props) => {
  const { errors, error, trigger, className, type } = {
    ...defaultProps,
    ...props,
  } as IFormErrors;

  return (
    <CSSTransition classNames='fade' timeout={200}>
      <React.Fragment>
        {trigger ? (
          <div className={className}>
            <FormErrorMessage error={error} errors={errors} type={type} />
          </div>
        ) : null}
      </React.Fragment>
    </CSSTransition>
  );
};
FormError.displayName = 'FormError';

interface IFormErrorForName<T> {
  formName: string;
  name: DeepKeyOf<T>;
  displayName?: string;
}

export class FormErrorForName<T> extends React.PureComponent<
  IFormErrorForName<T>
> {
  public static contextType = FormContext;
  public context: React.ContextType<typeof FormContext>;

  public render() {
    const { name, displayName } = this.props;
    const { submitErrors: errors } = this.context!;
    const error = get(errors, name as any);
    const formattedError =
      Array.isArray(error) || isObject(error)
        ? `Please correct all the ${displayName || name} fields.`
        : error;
    return (
      <FormError
        error={formattedError}
        type={ErrorType.Error}
        trigger={error != null}
      />
    );
  }
}
