import classNames from 'classnames';
import * as React from 'react';
import { CSSTransition } from 'react-transition-group';

import styles from './Tooltip.module.css';

export type TooltipType = 'warning' | 'error' | 'info';
export type TooltipPosition = 'bottom' | 'top';
export interface ITooltip {
  type?: TooltipType | '';
  message: string | React.ReactElement<any>;
  className?: string;
  containerClassName?: string;
  position?: TooltipPosition;
  onClick?(): void;
}

interface ITooltipState {
  visible: boolean;
}

export const Tooltip: React.FC<ITooltip> = ({ className, type = '', message, onClick, position = 'bottom' }) => {
  const typeName = styles[type];
  const tooltipPosition = styles[position];
  const fullClassName = classNames(className, styles.tip, typeName, tooltipPosition);
  return (
    <div className={fullClassName} onClick={onClick}>{message}</div>
  );
};

export class TooltipOnHover extends React.PureComponent<ITooltip, ITooltipState> {
  state = {
    visible: false,
  };

  render() {
    const { children, className, containerClassName, ...props } = this.props;
    const fullClassName = classNames(className, styles.HoverTooltip);

    return (
      <span
        className={classNames(styles.TooltipHoverContainer, containerClassName)}
        onMouseEnter={this.showTooltip}
        onMouseLeave={this.hideTooltip}
      >
        {children}
        <CSSTransition
          classNames='fade'
          timeout={200}
        >
          <React.Fragment>
            { this.state.visible ? <Tooltip {...props} className={fullClassName} /> : null }
          </React.Fragment>
        </CSSTransition>
      </span>
    );
  }

  private showTooltip = () => this.setState({ visible: true });

  private hideTooltip = () => this.setState({ visible: false });
}

export class TooltipOnClick extends React.PureComponent<ITooltip, ITooltipState> {
  state = {
    visible: false,
  };

  render() {
    const { children, className, containerClassName, ...props } = this.props;
    const fullClassName = classNames(className, styles.HoverTooltip);

    return (
      <span
        className={classNames(styles.TooltipHoverContainer, containerClassName)}
        onClick={this.toggleTooltip}
        onMouseLeave={this.hideTooltip} // To simulate blur
      >
        {children}
        <CSSTransition
          classNames='fade'
          timeout={200}
        >
          <React.Fragment>
            { this.state.visible ? <Tooltip {...props} className={fullClassName} /> : null }
          </React.Fragment>
        </CSSTransition>
      </span>
    );
  }

  private toggleTooltip = () => this.setState({ visible: !this.state.visible });

  private hideTooltip = () => this.setState({ visible: false });
}
