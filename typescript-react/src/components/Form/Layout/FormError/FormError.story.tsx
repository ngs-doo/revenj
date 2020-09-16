import { storiesOf } from '@storybook/react';
import * as React from 'react';

enum ErrorType {
  Error,
  Warning,
  QAPink,
}

import { FormError } from './FormError';
const sampleErrorMessage = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.';

storiesOf('FormError element', module)
  .add('Default view', () => {
    return (
      <FormError message={sampleErrorMessage} type={ErrorType.Error} />
    );
  });
