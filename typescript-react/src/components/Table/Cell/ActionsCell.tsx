import classNames from 'classnames';
import * as React from 'react';

import { NavigationContext } from '../../Navigation/NavigationContext';
import { IActionInfo, ICellProps } from '../interfaces';

import styles from './Cells.module.css';
import { PlaceholderValue } from './PlaceholderValue';

export class ActionsCell<T> extends React.PureComponent<ICellProps<T, any>> {
  static defaultProps: Partial<ICellProps<any, any>> = {
    actions: [],
  };

  public render() {
    const {
      className,
      onClick,
      style,
      actions,
      original,
    } = this.props;

    const actionsToRender = actions!
      .map((action) => ({ isVisible: true, ...action }))
      .filter((action) => typeof action.isVisible === 'function' ? action.isVisible(original) : action.isVisible); // #FIXME @bigd -> deal with this any BooleanMaper shenanigans

    return (
      <div
        style={style}
        className={classNames(className, { [styles.Clickable]: Boolean(onClick) })}
      >
        { actionsToRender.map((action, idx) => this.renderAction(action, idx, original)) }
        { !actionsToRender.length
          ? <PlaceholderValue />
          : null }
      </div>
    );
  }

  private renderAction = (action: IActionInfo<T>, index: number, original: T) => {
    const {
      title,
      icon,
      className,
      disabled,
      onClick,
      tooltip,
      customComponent,
      getUrl,
    } = action;

    const text = typeof title === 'function' ? title(original) : title;
    const hint = typeof tooltip === 'function' ? tooltip(original) : tooltip;

    if (customComponent != null) {
      return customComponent(original);
    }

    return getUrl
      ? (
        <NavigationContext.Consumer>
          {
            ({ Link }) => (
              <Link to={getUrl(original)} className='btn btn-default'>
                <i className={icon} /> {text != null ? text : null}
              </Link>
            )
          }
        </NavigationContext.Consumer>
        )
      : (
        <button
          type='button'
          className={className || 'btn btn-default'}
          onClick={(event) => onClick && onClick(event, original, this.props)}
          disabled={typeof disabled === 'function' ? disabled(original) : disabled}  // #FIXME @bigd -> deal with this any BooleanMaper shenanigans
          key={`action${this.props.index}_${index}`}
          title={hint || ''}
        >
          <i className={icon} /> {text != null ? text : null}
        </button>
        );
  }
}
