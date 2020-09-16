import classNames from 'classnames';
import * as React from 'react';

import {
  Tooltip,
  TooltipOnHover,
} from '../Tooltip/Tooltip';

import styles from './Description.module.css';

export interface IDescription {
  className?: string;
  message: string | React.ReactElement<any>;
}

export interface ILabelWithDescription extends IDescription {
  labelClassName?: string;
}

export interface ISpanWithIconDescription {
  iconClassName: string;
  message: string | React.ReactElement<any>;
  tooltipClassName?: string;
}

export const Description: React.FC<IDescription> = ({ className, message }) => {
  return (
    <div
      className={classNames(styles.Description, className)}
    >
      <Tooltip type='info' message={message} />
    </div>
  );
};

export const LabelWithDescription: React.FC<ILabelWithDescription> = ({ labelClassName, children, ...props }) => (
  <label className={classNames(labelClassName, styles.Label)}>
    {children}
    <Description {...props} />
  </label>
);

export const SpanLabelWithDescription: React.SFC<ILabelWithDescription> = ({ labelClassName, children, ...props }) => (
  <span className={classNames(labelClassName, styles.Label)}>
    {children}
    <Description {...props} />
  </span>
);

export const SpanWithIconDescription: React.SFC<ISpanWithIconDescription> = (
  {
    tooltipClassName,
    iconClassName,
    message,
    children,
  }) => (
    <span className={styles.Label}>
      {children}
      <TooltipOnHover
        className={classNames(styles.IconTooltip, tooltipClassName)}
        type='info'
        message={message}
      >
        <i className={classNames(styles.Icon, 'fa', iconClassName)} />
      </TooltipOnHover>
    </span>
);
