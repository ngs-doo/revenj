import classNames from 'classnames';
import * as React from 'react';
import { FormSection } from 'redux-form';

import {
  FormContext,
  FormContextProvider,
  FormValueContext,
  GlobalFormsContext,
} from './Context';
import { Section } from './FormSection';
import { flattenName } from '../../util/FunctionalUtils/FunctionalUtils';

import styles from './FormSection.module.css';

interface IGroupPublicProps<T> {
  expandedInitially?: boolean;
  name?: DeepKeyOf<T>;
  vertical?: boolean;
  borderless?: boolean;
  title?: string;
  optional?: boolean;
  visibility?: string | ((values: Partial<T>) => boolean);
  height?: number;
  className?: string;
  containerClassName?: string;
  columns?: 1 | 2 | 3;
  /**
   * Works only on named groups. Set this if you want to preserve values (or handle their removal in another way)
   * when the group stops being visible.
   */
  keepOnUnmount?: boolean;
}

interface IGroupInjectedProps<T> {
  values?: Partial<T>;
  isGroupVisible: (marker: string) => boolean;
}

export interface IGroup<T> extends IGroupPublicProps<T>, IGroupInjectedProps<T> {}

export class GroupBare<T = any> extends React.PureComponent<IGroup<T>> {
  public static contextType = FormContext;
  public context: React.ContextType<typeof FormContext>;

  public componentWillUnmount() {
    if (this.props.name != null && this.props.keepOnUnmount === false) {
      const { change } = this.context!;
      const name = this.getNestedSectionName()!;
      change(name, null);
    }
  }

  public render() {
    const {
      borderless,
      children,
      containerClassName,
      columns,
      className,
      vertical,
      name,
      visibility,
      optional,
      values,
      height,
      ...props
    } = this.props;
    const { sectionName, ...rest } = this.context!;

    if (!this.isVisible()) {
      return null;
    }

    const childElement = (
      <Section
        {...props}
        containerClassName={classNames(
          styles.Section,
          {
            [styles.Vertical]: vertical,
            [styles.Borderless]: borderless,
            [styles.Titled]: !!props.title,
            [styles.SingleColumn]: columns === 1,
            [styles.TwoColumns]: columns === 2,
            [styles.ThreeColumns]: columns === 3,
          },
          className,
          'dslGroup',
        )}
        keepExpanded={!this.collapseInitially()}
      >
        {children}
      </Section>
    );

    const extraProps = {
      className: classNames(containerClassName, styles.Group, this.generateQAHelperClass(this.props.title)),
    };

    if (name != null) {
      return (
        <FormSection
          name={flattenName(name)}
          {...extraProps}
        >
          <FormContextProvider value={{ ...rest, sectionName: this.getNestedSectionName(), forceOptional: optional ?? undefined, defaultInline: vertical }}>
            {childElement}
          </FormContextProvider>
        </FormSection>
      );
    } else {
      return (
        <FormContextProvider value={{ ...rest, sectionName, forceOptional: optional ?? undefined, defaultInline: vertical }}>
          {childElement}
        </FormContextProvider>
      );
    }
  }

  private generateQAHelperClass(title: string = 'section'): string {
    return `js__${title.toLocaleLowerCase().split(' ').join('-')}--`;
  }

  private getNestedSectionName = () => {
    const { name } = this.props;
    const { sectionName } = this.context!;

    if (!name) {
      return sectionName;
    }

    const baseName = flattenName(name);

    return sectionName ? `${sectionName}.${baseName}` : baseName;
  }

  private isVisible = (): boolean => {
    const { isGroupVisible, visibility, values } = this.props;

    if (typeof visibility === 'function') {
      return visibility(values as T);
    } else if (typeof visibility === 'string') {
      return isGroupVisible(visibility);
    }

    return true;
  }

  private collapseInitially = (): boolean =>
    this.props.height === 0 || this.props.expandedInitially === false // Special case for collapsed, hacky. Might actually be handled fully at some point
}

// Default to any... Ideal? No. Necessary? Also no. The simplest way to do it? Yes.
export class Group<T = any> extends React.PureComponent<IGroupPublicProps<T>> {
  public render() {
    const { visibility } = this.props;

    if (typeof visibility === 'function') {
      return (
        <FormValueContext.Consumer>
          {
            (values) => (
              <GlobalFormsContext.Consumer>
                {({ isGroupVisible }) => <GroupBare {...this.props} values={values} isGroupVisible={isGroupVisible} />}
              </GlobalFormsContext.Consumer>
            )
          }
        </FormValueContext.Consumer>
      );
    } else {
      return (
        <GlobalFormsContext.Consumer>
          {({ isGroupVisible }) => <GroupBare {...this.props} isGroupVisible={isGroupVisible} />}
        </GlobalFormsContext.Consumer>
      );
    }
  }
}
