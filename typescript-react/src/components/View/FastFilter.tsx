import * as React from 'react';

import { Input as InputField } from '../Input/Input';

import styles from './Grid.module.css';

interface IFastFilter {
  query?: string;
  onQueryChange(query?: string): void;
}

export class FastFilter extends React.PureComponent<IFastFilter> {
  public render() {
    return (
      <section className={styles.Search}>
        <InputField
          containerClassName={styles.InputContainer}
          input={{
            name: 'fast-search',
            // TODO: remove this as any after redux-form typings get fixed
            // any is here since onChange can receive either a value or a change event
            // until we move to newer react (and better typings for redux-form with that) we are stuck with as any
            onChange: this.onChange as any,
            value: this.props.query || '',
          } as any}
          id='fast-search'
          type='search'
          // TODO: @bigd -> when inputs get hit with a refact-o-hammer this 'as any's can be dropped
          meta={{} as any}
          hideLabel
          {...{ placeholder: 'Search...', 'data-qa-element-id': 'fast-search' }}
        />
      </section>
    );
  }

  private onChange = (event: React.SyntheticEvent<HTMLInputElement>) =>
    this.props.onQueryChange(event.currentTarget.value)
}
