import classNames from 'classnames';
import * as React from 'react';
import Button from 'react-bootstrap/Button';

import { isNullOrEmpty } from '../../util/StringUtils/StringUtils';
import { NavigationContext } from '../Navigation/NavigationContext';
import { ISimpleButtonAction } from '../Presenters/Actions';
import { TooltipOnHover } from '../Tooltip/Tooltip';
import { ButtonMenu } from './ButtonMenu';
import { ButtonItem, isComponentButton } from './interfaces';

import styles from './Buttons.module.css';

interface IButtons {
  buttons: ButtonItem[];
  disabled?: boolean;
  maxVisibleButtons?: number;
}

export class Buttons extends React.PureComponent<IButtons> {
  public static defaultProps: Partial<IButtons> = {
    maxVisibleButtons: 3,
  };

  public render() {
    const { buttons, maxVisibleButtons } = this.props;
    const visibleButtons = buttons.slice(0, maxVisibleButtons);
    const menuButtons = buttons.slice(maxVisibleButtons);

    return (
      <div className={styles.Buttons}>
        { visibleButtons.map(this.renderButton) }
        {
          menuButtons.length > 0 ? (
            <ButtonMenu
              className={classNames(styles.Button, styles.Inverted)}
              buttons={menuButtons}
            />
          ) : null
        }
      </div>
    );
  }

  private renderButton = (btn: ButtonItem, index: number) => {
    if (isComponentButton(btn)) {
      const { values, ...rest } = btn;

      return (
        <div key={index} className={styles.ComponentButton}>
          <btn.Component
            props={{ item: values }}
            {...rest}
          />
        </div>
      );
    } else {
      const buttonComponent = btn.url != null && !btn.isExternalUrl
        ? (
          <NavigationContext.Consumer>
            {
              ({ Link }) => (
                <Link
                  key={index}
                  className={classNames('btn btn-primary', styles.Button)}
                  to={btn.url!}
                >
                  {btn.label}
                </Link>
              )
            }
          </NavigationContext.Consumer>
          )
        : (
          <Button
            key={index}
            className={styles.Button}
            href={btn.url}
            onClick={btn.onClick}
            disabled={btn.disabled}
            variant={(btn as ISimpleButtonAction).variant || 'primary'}
          >
            {btn.label}
          </Button>
        );
      return btn.tooltip && !isNullOrEmpty(btn.tooltip)
        ? (
          <TooltipOnHover
            key={index}
            message={btn.tooltip}
            containerClassName={styles.Tooltip}
            type='info'
          >
            {buttonComponent}
          </TooltipOnHover>
        )
        : buttonComponent;
    }
  }
}
