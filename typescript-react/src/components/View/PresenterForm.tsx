import classNames from 'classnames';
import * as React from 'react';

import { CreatePresenterContext, UpdatePresenterContext, FormControlContext, IFormControlContext } from '../Form/Context';
import { Form } from '../Form/Form';
import { FormType } from '../Form/interfaces';
import { Loading } from '../Loader/Loader';

import styles from './Form.module.css';

export interface IPresenterFormPublicProps<T> {
  submitButtonText?: string;
  cancelButtonText?: string;
  initialValues?: Partial<T>;
  readOnly?: boolean;
  configuration?: IFormControlContext<T>;
  onSubmitSuccess?: (data: T) => void;
  onCancel?: () => void;
}

interface IPresenterFormInternalProps<T> {
  conceptName: string;
  formType: FormType;
  activeItem?: T;
  onSubmit: (data: T) => Promise<T>;
}

interface IPresenterForm<T> extends IPresenterFormPublicProps<T>, IPresenterFormInternalProps<T> {}

class PresenterFormBare<T = any> extends React.PureComponent<IPresenterForm<T>> {
  public render() {
    const { activeItem, conceptName, configuration, children, initialValues, formType, readOnly, onSubmit, ...props } = this.props;

    const classNameForType = {
      dslCreateForm: formType === FormType.Create,
      dslEditForm: formType === FormType.Edit,
      dslViewForm: formType === FormType.View,
    };

    const child = formType === FormType.Create || activeItem != null ? (
      <Form<T>
        {...props}
        formType={formType}
        form={conceptName}
        onSubmit={onSubmit || Promise.resolve}
        className={classNames(styles.Form, 'dslPresenterForm', classNameForType)}
        initialValues={activeItem != null ? activeItem : initialValues}
        readOnly={readOnly || formType === FormType.View}
        hideButtons={readOnly || formType === FormType.View}
      >
        {children}
      </Form>
    ) : (
      <Loading />
    );

    return configuration ? (
      <FormControlContext.Provider value={configuration}>
        {child}
      </FormControlContext.Provider>
    ) : (
      child
    );
  }
}

export class PresenterForm<T> extends React.PureComponent<IPresenterFormPublicProps<T>> {
  public render() {
    return (
      <React.Fragment>
        <CreatePresenterContext.Consumer>
          {
            (context) => context != null ? (
              <PresenterFormBare submitButtonText='Create' {...this.props} {...context} />
            ) : null
          }
        </CreatePresenterContext.Consumer>
        <UpdatePresenterContext.Consumer>
          {
            (context) => context != null ? (
              <PresenterFormBare {...this.props} {...context} />
            ) : null
          }
        </UpdatePresenterContext.Consumer>
      </React.Fragment>
    );
  }
}
