import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { Tooltip } from './Tooltip';
const sampleMessage = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.';

storiesOf('Tooltip element', module)
  .add('Default view', () => {
    return (
      <Tooltip message={sampleMessage} />
    );
  }).add('Warrning', () => {
    return (
      <Tooltip message={sampleMessage} type={'warning'}/>
    );
  }).add('Error', () => {
    return (
      <Tooltip message={sampleMessage} type={'error'} />
    );
  });
