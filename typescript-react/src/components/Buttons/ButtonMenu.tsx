import classNames from 'classnames';
import * as React from 'react';
import Button from 'react-bootstrap/Button';
import { CSSTransition } from 'react-transition-group';

import { isNullOrEmpty } from '../../util/StringUtils/StringUtils';
import { TooltipOnHover } from '../Tooltip/Tooltip';
import { NavigationContext } from '../Navigation/NavigationContext';
import { ButtonItem, isComponentButton, ISimpleButton } from './interfaces';

import styles from './ButtonMenu.module.css';

interface IButtonMenu {
  buttons: ButtonItem[];
  buttonClassName?: string;
  className?: string;
  disabled?: boolean;
  dropdownClassName?: string;
}

export interface IButtonMenuState {
  isExpanded: boolean;
}

export class ButtonMenu extends React.PureComponent<IButtonMenu, IButtonMenuState> {
  static defaultProps = {
    bsStyle: 'info',
  };

  public state = {
    isExpanded: false,
  };

  private timeout?: number;
  private mounted = false;

  public componentDidMount() {
    this.mounted = true;
  }

  public componentWillUnmount() {
    this.mounted = false;
    if (this.timeout) {
      window.clearTimeout(this.timeout);
    }
  }

  public render() {
    const { buttonClassName, children, className, disabled, dropdownClassName, buttons } = this.props;

    return (
      <div className={classNames(className, styles.ButtonMenu, { [styles.ButtonMenuDefault]: children == null })}>
        <Button
          disabled={disabled}
          className={classNames(styles.ButtonMenuToggle, buttonClassName)}
          onClick={this.open}
        >
          {
            children || (<i className='fa fa-ellipsis-h' />)
          }
        </Button>
        {
          this.state.isExpanded ? (
            <CSSTransition
              classNames='fade'
              timeout={200}
            >
              <main className={classNames(styles.ButtonMenuDropdown, dropdownClassName)}>
                {
                  buttons.map((item, itemIndex) => {
                    if (isComponentButton(item)) {
                      return (
                        <div key={itemIndex} className={styles.External}>
                          <item.Component />
                        </div>
                      );
                    } else {
                      return this.renderButtonMenuItem(item, itemIndex);
                    }
                  })
                }
              </main>
            </CSSTransition>
          ) : null
        }
      </div>
    );

  }

  private renderButtonMenuItem = (item: ISimpleButton, itemIndex: number) => {
    const itemComponent = item.href ? this.renderLink(item, itemIndex) : this.renderButton(item, itemIndex);
    return item.tooltip && !isNullOrEmpty(item.tooltip)
      ? (
        <TooltipOnHover
          key={itemIndex}
          className={styles.Tooltip}
          message={item.tooltip}
          type='info'
        >
          {itemComponent}
        </TooltipOnHover>
      )
      : itemComponent;
  }

  private renderButton = (item: ISimpleButton, index: number) => (
    <div
      key={index}
      onClick={item.onClick}
      className={classNames(styles.ButtonMenuDropdownItem, {[styles.DisabledBtn]: item.disabled})}
    >
      {item.label}
    </div>
  )

  private renderLink = (item: ISimpleButton, index: number) => (
    item.isExternalUrl ? (
      <a
        key={index}
        href={item.href!}
        className={classNames(item.className, styles.ButtonMenuDropdownItem)}
      >
        {item.label}
      </a>
    ) : (
      <NavigationContext.Consumer>
        {
          ({ Link }) => (
            <Link
              key={index}
              to={item.href!}
              className={classNames(item.className, styles.ButtonMenuDropdownItem)}
            >
              {item.label}
            </Link>
          )
        }
      </NavigationContext.Consumer>
    )
  )

  private toggle = (value?: boolean) => {
    const { isExpanded } = this.state;

    this.setState({ isExpanded: value != null ? value : !isExpanded });

    if (isExpanded) { // closing
      document.body.removeEventListener('click', this.close);
    } else { // opening
      document.body.addEventListener('click', this.close);
    }
  }

  private close = () => {
    // Timeout to avoid close + open on button click
    this.timeout = window.setTimeout(() => {
      if (this.mounted) {
        this.toggle(false);
      }
    }, 0);
  }

  private open = () => {
    if (!this.state.isExpanded && this.mounted) {
      this.toggle(true);
    }
  }
}
