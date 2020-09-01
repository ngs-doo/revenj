import classNames from 'classnames';
import * as React from 'react';

import { NavigationContext } from '../../Navigation/NavigationContext';
import { ICellProps } from '../interfaces';

import styles from './Cells.module.css';
import { PlaceholderValue } from './PlaceholderValue';

export class LinkCell<T> extends React.PureComponent<ICellProps<T, string>> {
  render() {
    const {
      className,
      constructUrl,
      onClick,
      style,
      value,
      url,
      openInNewTab,
      original,
      isExternal,
      formatter,
      row,
      download,
    } = this.props;

    const displayValue =
      value !== null && value !== undefined ?
        (formatter ? formatter(value, row ? row._original : row) : value) : <PlaceholderValue />;
    const linkUrl = constructUrl ? constructUrl(original, this.props) : url;
    const target = openInNewTab ? '_blank' : '';

    const isExternalUrl = (typeof isExternal === 'function')
      ? isExternal(original, linkUrl)
      : Boolean(isExternal);

    const isDownloadLink = Boolean(download);

    return (
      <div
        style={style}
        className={classNames({ [styles.Clickable]: Boolean(onClick), [className!]: true })}
        onClick={(event) => onClick && onClick(event, original, this.props)}
      >
        { linkUrl
          ? (isExternalUrl
            ? <a href={linkUrl} target={target} className={styles.clickable} download={isDownloadLink}>{displayValue}</a>
            : this.getLink(linkUrl, className, displayValue, isDownloadLink))
          : displayValue }
      </div>
    );
  }

  private getLink(linkUrl: string, className: string | undefined, displayValue: string | JSX.Element, isDownloadLink: boolean) {
    if ((process.env.NODE_ENV as any) === 'storybook') {
      return <a href={linkUrl} target='_blank' className={styles.clickable} download={isDownloadLink}>{displayValue}</a>;
    } else {
      return (
        <NavigationContext.Consumer>
          {
            ({ Link }) => <Link to={linkUrl} className={classNames(className, styles.Clickable)}>{displayValue}</Link>
          }
        </NavigationContext.Consumer>
      );
    }
  }
}
