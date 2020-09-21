import classNames from 'classnames';
import * as React from 'react';

import { Internationalised } from '../I18n/I18n';
import { localizeTextIfMarked } from '../I18n/service';

import styles from './FormSection.module.css';

interface ISection {
  title?: string;
  containerClassName?: string;
  className?: string;
  titleClassName?: string;
  keepExpanded?: boolean;
}

interface ISectionState {
  expanded: boolean;
}

export class Section extends React.PureComponent<ISection, ISectionState> {
  public state: ISectionState = {
    expanded: this.props.keepExpanded === true,
  };

  public render() {
    const { children, containerClassName, className, titleClassName, keepExpanded, title } = this.props;
    const { expanded } = this.state;

    return (
      <section className={classNames(styles.Section, containerClassName)}>
        {
          expanded ? (
            <div className={classNames(styles.Items, className)}>
              {children}
            </div>
          ) : null
        }
        {
          title ? (
            <Internationalised>
              {
                ({ localize}) => (
                  <header
                    className={styles.Header}
                    onClick={this.props.keepExpanded ? undefined : this.onToggleCollapse}
                  >
                    <div className={classNames(styles.Title, titleClassName)}>{localizeTextIfMarked(localize, title)}</div>
                    {
                      !keepExpanded ? (
                        <i
                          className={classNames('fa', styles.Toggle, {
                            'fa-chevron-down dslCollapsedHeader': !expanded,
                            'fa-chevron-up dslExpandedHeader': expanded,
                          })}
                        />
                      ) : null
                    }
                  </header>
                )
              }
            </Internationalised>
          ) : null
        }
      </section>
    );
  }

  private onToggleCollapse = () => this.setState({ expanded: !this.state.expanded })
}
