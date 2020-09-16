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

import styles from './FormSection.module.css';

interface IGroupPublicProps<T> {
  expandedInitially?: boolean;
  name?: DeepKeyOf<T>;
  vertical?: boolean;
  borderless?: boolean;
  title?: string;
  visibility?: string | ((values: Partial<T>) => boolean);
  height?: number;
  className?: string;
  containerClassName?: string;
}

interface IGroupInjectedProps<T> {
  values?: Partial<T>;
  isGroupVisible: (marker: string) => boolean;
}

export interface IGroup<T> extends IGroupPublicProps<T>, IGroupInjectedProps<T> {}

export class GroupBare<T = any> extends React.PureComponent<IGroup<T>> {
  public static contextType = FormContext;
  public context: React.ContextType<typeof FormContext>;

  public render() {
    const { borderless, children, containerClassName, className, vertical, name, visibility, values, height, ...props } = this.props;
    const { sectionName, ...rest } = this.context!;

    if (!this.isVisible()) {
      return null;
    }

    const childElement = (
      <Section
        {...props}
        containerClassName={classNames(styles.Section, { [styles.Vertical]: vertical, [styles.Borderless]: borderless, [styles.Titled]: !!props.title }, className, 'dslGroup')}
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
          name={Array.isArray(name) ? name.join('.') : name as string}
          {...extraProps}
        >
          <FormContextProvider value={{ ...rest, sectionName: this.getNestedSectionName() }}>
            {childElement}
          </FormContextProvider>
        </FormSection>
      );
    } else {
      return (
        <React.Fragment>
          {childElement}
        </React.Fragment>
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

    const baseName = Array.isArray(name) ? name.join('.') : String(name);

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
