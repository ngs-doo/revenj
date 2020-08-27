import { identity } from '../../util/FunctionalUtils/FunctionalUtils';
import {
  IGetValidatorBaseValue,
  IGetValidatorErrorMessage,
  IParseValue,
  Operator,
  Preprocessor,
  ValidateTrigger,
  ValidatorCreator,
} from './interfaces';

const globalPreProcessors: Array<Preprocessor<any>> = [];

export const withPreprocessor = <T>(preprocessor: Preprocessor<T>) =>
  globalPreProcessors.push(preprocessor);

// we treat undefined, null and '' as an absence of values
export const valueIsAbsent = (value: any) =>
  (value == null) || (typeof value === 'string' && value === '');

////////////////////////////////////////////////////////////////////////////////
// Validator creator                                                          //
////////////////////////////////////////////////////////////////////////////////
// Generic types: B = base type, I = input type, F = form type, P = props type
export interface IValidatorCreatorOptions<B, I, F, P> {
  validate?: ValidateTrigger;
  getValidatorErrorMessage: IGetValidatorErrorMessage<B, I, F, P> | string;
  getValidatorBaseValue?: IGetValidatorBaseValue<B, F, P> | B;
  getValueFromInput?: IParseValue<any, I>;
  getValueFromBase?: IParseValue<any, B>;
  operator: Operator<B, I>;
}
export const validatorCreatorFactory = <B, I, F, P>({
  validate = ValidateTrigger.IfInputIsSet,
  getValidatorErrorMessage,
  getValidatorBaseValue,
  getValueFromInput = (identity as IParseValue<I, I>),
  getValueFromBase = (identity as IParseValue<B, B>),
  operator,
}: IValidatorCreatorOptions<B, I, F, P>): ValidatorCreator<B, I, F, P> =>
  (creatorProps = {}) =>
    (inputValue, allValues, props, name) => {
      const {
        validateOverride,
        getValidatorBaseValueOverride,
        getValidatorErrorMessageOverride,
        getValueFromInputOverride,
        getValueFromBaseOverride,
      } = creatorProps;

      const validateIf = validateOverride || validate;
      const getBaseValue = getValidatorBaseValueOverride || getValidatorBaseValue;
      const parseInputValue: IParseValue<any, I> = getValueFromInputOverride || getValueFromInput;
      const parseBaseValue: IParseValue<any, B> = getValueFromBaseOverride || getValueFromBase;

      const baseValue = typeof getBaseValue === 'function'
        ? (getBaseValue as IGetValidatorBaseValue<B, F, P>)(allValues ?? {} as F, props!, name as string)
        : getBaseValue;

      if (
        ((validateIf === ValidateTrigger.IfInputIsSet) && (valueIsAbsent(inputValue))) ||
        ((validateIf === ValidateTrigger.IfBaseIsSet) && (valueIsAbsent(baseValue))) ||
        ((validateIf === ValidateTrigger.IfInputAndBaseAreSet) && (valueIsAbsent(inputValue) || valueIsAbsent(baseValue)))
      ) {
        return undefined;
      }

      const parsedBaseValue = globalPreProcessors.reduce((val, f) => f(val), parseBaseValue(baseValue));
      const parsedInputValue = globalPreProcessors.reduce((val, f) => f(val), parseInputValue(inputValue));

      const getErrorMessage = getValidatorErrorMessageOverride || getValidatorErrorMessage;
      const message = typeof getErrorMessage === 'function'
        ? getErrorMessage(parsedBaseValue, parsedInputValue, allValues ?? {} as F, props!, name as string)
        : getErrorMessage;

      return operator(parsedBaseValue)(parsedInputValue)
        ? undefined
        : message;
    };
