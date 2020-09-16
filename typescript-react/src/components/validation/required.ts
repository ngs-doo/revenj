import { ValidateTrigger } from './interfaces';
import * as operators from './operators';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Requirement validators                                                     //
////////////////////////////////////////////////////////////////////////////////
export const requiredCreator = validatorCreatorFactory<void, any, any, any>({
  getValidatorErrorMessage: 'Required',
  operator: operators.required,
  validate: ValidateTrigger.Always,
});
