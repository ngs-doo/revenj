import {
  IFormFieldSpecification,
  ValidateTrigger,
} from './interfaces';
import { matches } from './operators';
import { resolveRelativePath } from './utils';
import {
  validatorCreatorFactory,
  valueIsAbsent,
} from './validatorCreatorFactory';
import { get } from '../../util/FunctionalUtils/FunctionalUtils';
import { isNullOrEmpty } from '../../util/StringUtils/StringUtils';

////////////////////////////////////////////////////////////////////////////////
// Text validators                                                            //
////////////////////////////////////////////////////////////////////////////////

export const matchesRegexCreator = (regex: RegExp, message: string = 'Value must match the defined pattern') => validatorCreatorFactory<RegExp, string, any, any>({
  getValidatorBaseValue: () => regex,
  getValidatorErrorMessage: message,
  operator: matches,
  validate: ValidateTrigger.IfInputIsSet,
});

export const matchesRegexFieldCreator = <F>(field: IFormFieldSpecification) =>
  validatorCreatorFactory<RegExp | undefined, string, F, void>({
    getValidatorBaseValue: (formValues: F, _: any, fieldName: string) => {
      const regexp = get(formValues, resolveRelativePath(field.path, fieldName) as keyof F) as unknown as string;
      if (isNullOrEmpty(regexp)) {
        return;
      }

      try {
        return new RegExp(`^${regexp}$`);
      } catch (_) {
        return;
      }
    },
    getValidatorErrorMessage: () => `Does not match ${field.label}`,
    operator: (base) => (input) => {
      if (valueIsAbsent(base) || valueIsAbsent(input)) {
        return true;
      } else {
        return matches(base!)(input);
      }
    },
    validate: ValidateTrigger.IfInputAndBaseAreSet,
  });

// Taken from RFC spec directly
const urlRegex = /^(?:(?:(?:https?):)?\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:[/?#]\S*)?$/i;
export const isValidURLCreator = matchesRegexCreator(urlRegex, 'Value must be valid URL');

// Taken from loose RFC spec directly
const emailRegex = /^[^\.\s@:](?:[^\s@:]*[^\s@:\.])?@[^\.\s@]+(?:\.[^\.\s@]+)*$/;
export const isValidEmailCreator = matchesRegexCreator(emailRegex, 'Value must be valid mail');
