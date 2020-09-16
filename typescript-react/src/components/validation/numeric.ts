import {
  Num,
  ValidatorCreator,
} from './interfaces';
import * as operators from './operators';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Order validators                                                           //
////////////////////////////////////////////////////////////////////////////////
interface INumValidatorCreatorFactory {
  (value: Num): ValidatorCreator<Num, Num, any, any>;
}

export const multipleOfFactorCreator: INumValidatorCreatorFactory = (value: Num) =>
  validatorCreatorFactory<Num, Num, any, any>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be a multiple of ${base}.`,
    operator: operators.multipleOfFactor,
  });
