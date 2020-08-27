import * as React from 'react';
import FormControl from 'react-bootstrap/FormControl';
import { Provider } from 'react-redux';
import {
  act,
  create,
  ReactTestRenderer,
} from 'react-test-renderer';
import {
  combineReducers,
  createStore,
} from 'redux';
import {
  change,
  getFormValues,
  reducer,
  reduxForm,
  Field,
} from 'redux-form';

import { omit } from '../../../util/FunctionalUtils/FunctionalUtils'
import {
  formField,
} from '../formField';

jest.unmock('redux-form');

const mockStore = createStore(combineReducers({
  form: reducer,
}));

class TestFieldComponent extends React.PureComponent<any> {
  public render() {
    const { containerClassName, input, type } = this.props;
    const childProps = omit(this.props, 'input', 'hideLabel', 'meta');

    return (
      <div className={containerClassName}>
        <FormControl
          {...input}
          type={type}
          {...childProps}
          noValidate
        />
      </div>
    );
  }
}

const DecoratedTestField = formField<any>()(TestFieldComponent);

class TestFormComponent extends React.Component {
  public render() {
    return (
      <form>
        <Field
          label='Test Label'
          name='field'
          id='Test Field'
          component={DecoratedTestField}
        />
      </form>
    );
  }
}

const TestForm = reduxForm({ form: 'Test' })(TestFormComponent);

const renderTestForm = () => {
  return (
    <Provider store={mockStore as any}>
      <TestForm />
    </Provider>
  );
};

describe('formField HOC', () => {

  it('should render without crashing', () => {
    let component: ReactTestRenderer | null = null;
    act(() => {
      component = create(renderTestForm());
    });
    // NOTE: this should be .toTree instead of .toJson but because of a bug in react 16.x the tree is actually a graph
    // NOTE: and having FCs with hooks with children as FCs with hooks creates cycles from which the .toTree never finishes
    expect(component!.toJSON()).toMatchSnapshot();
  });

  it('should render the correct masked value', () => {
    let component: ReactTestRenderer | null = null;

    act(() => {
      component = create(renderTestForm());
    });

    const selector = getFormValues('Test');

    const input = component!.root.find((instance) => instance.type === 'input');
    expect(input.props.value).toEqual('');
    expect(selector(mockStore.getState())).toEqual(undefined);

    const EXPECTED_INPUT_VALUE = 'test input value';

    act(() => {
      mockStore.dispatch(change('Test', 'field', EXPECTED_INPUT_VALUE));
    });

    expect(selector(mockStore.getState())).toEqual({ field: EXPECTED_INPUT_VALUE });
    expect(input.props.value).toEqual(EXPECTED_INPUT_VALUE);
  });
});
