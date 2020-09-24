/* tslint:disable:only-arrow-functions */

import classNames from 'classnames';
import * as React from 'react';
import ControlLabel from 'react-bootstrap/FormLabel';
import FormGroup from 'react-bootstrap/FormGroup';
import { CSSTransition } from 'react-transition-group';
import { WrappedFieldProps } from 'redux-form';

import { omit } from '../../util/FunctionalUtils/FunctionalUtils';
import { Description } from '../Description/Description';

import styles from './FormField.module.css';
import { Tooltip } from '../Tooltip/Tooltip';

export interface IFormFieldWrapper {
  qa?: {
    'data-qa-element-id': string;
  };
  inline?: boolean;
  containerClassName?: string;
  tooltipClassName?: string;
  label?: string;
  hideLabel?: boolean;
  initialValidation?: boolean;
  externalLabel?: boolean;
  inverted?: boolean;
  labelClassName?: string;
  description?: string;
  autoComplete?: string;
}

export interface IBaseComponentProps<T> {
  className?: string;
  onChange?: (event: React.ChangeEvent<any> | T) => void;
  onBlur?: (event: React.FocusEvent<any> | T) => void;
  onFocus?: (event: React.FocusEvent<any> | T) => void;
}

const formFieldProps = [
  'inline',
  'containerClassName',
  'initialValidation',
  'labelClassName',
  'externalLabel',
  'tooltipClassName',
  'validations',
  'change',
  'description',
];

// FIXME: Figure out how to short-cicuit typechecking on formField HoC
// FIXME: in a way that it does not bring typechecking to a halt

// FIXME: @bigd -> I intentionaly do 'as any' to break the chain of typechecking on formField
// FIXME: @bigd -> in this file since it causes too much overhead for building while
// FIXME: @bigd -> not giving anything usefull out, it is only necessary to ensure
// FIXME: @bigd -> interfaces align for non-decorated and decorated field components

// type KeysToOmit =
//   & 'inline'
//   & 'containerClassName'
//   & 'initialValidation'
//   & 'labelClassName'
//   & 'externalLabel'
//   & 'tooltipClassName'
//   & 'validations'
//   & 'change'
//   ;

type PartialWrappedFieldProps =
  & { input?: Partial<WrappedFieldProps['input']>; }
  & { meta?: Partial<WrappedFieldProps['meta']>; }
  ;

export type FormFieldProps<P = {}> = WrappedFieldProps & IFormFieldWrapper & { id?: string } & P;

// FIXME: @bigd -> this partial variant of FormFieldProps exists to avoid refactoring every single instance of select
// and native selects there is in our codebase
// when Select fields get hit with the refact-o-hammer it is enough to just remove this type to easily locate all instances
// of borked select fields and possibly some others also
export type PartialFormFieldProps<P = {}> = Partial<PartialWrappedFieldProps & IFormFieldWrapper & { id?: string } & P>;

const renderLabel = function <P>({ description, externalLabel, hideLabel, label, labelClassName }: Partial<FormFieldProps<P>>) {
  return externalLabel && !hideLabel
    ? (
      <div className={styles.LabelContainer}>
        <ControlLabel className={labelClassName}>{label}</ControlLabel>
        { description ? <Description className={styles.Description} message={description! as string} /> : null }
      </div>
    )
    : null;
};

export type MakeClassName = (params: any) => { [key: string]: boolean } | string | undefined;
interface IFormFieldDecoratorProps {
  makeClassName?: MakeClassName;
  externalLabel?: boolean;
  forceInitialValidation?: boolean;
  invertable?: boolean;
  passQAObjectToChild?: boolean;
}

const formFieldDefaultProps: IFormFieldDecoratorProps = {
  externalLabel: true,
  forceInitialValidation: false,
  invertable: false,
  makeClassName: (_params: any) => '',
  passQAObjectToChild: false,
};

const processNestedErrors = (error: any | IObjectAny, name: string) => {
  // Shamefully assume that error is an object if it's not a string
  // happens with composite fields when nested fields introduce an error
  // fixes this guy - https://oradian.atlassian.net/browse/IN-13633
  if (typeof error !== 'string') {
    return Object.keys(error)
      .filter((key) => key === name)     // don't show errors of nested fields on parent
      .map((key) => error[key]).join('\n');
  } else {
    return error;
  }
};

export const formField = <InnerComponentProps extends IBaseComponentProps<any>>(props?: IFormFieldDecoratorProps) =>
  (Component: React.ComponentType<InnerComponentProps>) => {
    const {
      externalLabel,
      forceInitialValidation,
      invertable,
      makeClassName,
      passQAObjectToChild,
    }: /* IFormFieldDecoratorProps */ any = { ...formFieldDefaultProps, ...props };

    return class FormFieldWrapper extends React.PureComponent</* FormFieldProps<InnerComponentProps> */ any> {
      // default props and statics are still borked hence this cast
      static defaultProps = {
        autoComplete: 'nope',
        inline: false,
        label: '',
      } as /* Partial<FormFieldProps<InnerComponentProps>> */ any;

      public render() {
        const {
          containerClassName,
          id,
          input,
          inverted,
          inline,
          initialValidation,
          meta: { active, touched, error, warning },
          tooltipClassName,
          description,
        } = this.props;

        const processedError = error && processNestedErrors(error, name);
        const shouldShowValidation = !active && (touched || initialValidation || forceInitialValidation);
        const isInverted = invertable && inverted;
        const controlId = id
          ? undefined
          : (input && input.name || '');
        const qaHookId = this.props['data-qa-element-id'] || (input && input.name || '');
        const qaAttrs = { 'data-qa-element-id': qaHookId };

        const childProps: any = omit({
          ...this.props,
          noValidate: true,
        }, ...(formFieldProps as /* KeysToOmit[] */ any));

        if (passQAObjectToChild) {
          childProps.qa = qaAttrs;
        }

        if (!externalLabel) {
          childProps.description = description;
        }

        const groupClassName = classNames(
          containerClassName,
          styles.FormField,
          {
            [styles.Inline]: inline,
            [styles.Error]: shouldShowValidation && Boolean(processedError),
            [styles.Warning]: shouldShowValidation && Boolean(warning) && !Boolean(processedError),
          },
          makeClassName && makeClassName(this.props),
        );

        const renderValidation = () => shouldShowValidation
          /* poor man's pattern matching */
          ? (
            processedError ? <CSSTransition classNames='fade' timeout={200}><Tooltip className={tooltipClassName} type='error' message={processedError} /></CSSTransition> :
            warning ? <CSSTransition classNames='fade' timeout={200}><Tooltip className={tooltipClassName} type='warning' message={warning} /></CSSTransition> :
            /* otherwise */ null
          )
          : null;

        return (
          <FormGroup className={groupClassName} controlId={controlId}>
            { !isInverted ? renderLabel({ externalLabel, ...this.props }) : null }
            <div className={styles.Component}>
              <Component {...childProps} {...qaAttrs} />
              { renderValidation() }
            </div>
            { isInverted ? renderLabel({ externalLabel, ...this.props }) : null }
          </FormGroup>
        );
      }
    } as React.ComponentClass<InnerComponentProps & FormFieldProps<InnerComponentProps>>;
  };
