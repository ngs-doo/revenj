import { Eq, ValidatorCreator } from './interfaces';
import * as operators from './operators';
import { validatorCreatorFactory } from './validatorCreatorFactory';

////////////////////////////////////////////////////////////////////////////////
// Equality validators                                                        //
////////////////////////////////////////////////////////////////////////////////
interface IEqValidatorCreatorFactory {
  <T extends Eq>(value: T): ValidatorCreator<T, T, void, void>;
}

export const isEqualCreator: IEqValidatorCreatorFactory = <T>(value: T) =>
  validatorCreatorFactory<T, T, void, void>({
    getValidatorBaseValue: () => value,
    getValidatorErrorMessage: (base) => `Value must be equal to ${base}.`,
    operator: operators.eq,
  });
