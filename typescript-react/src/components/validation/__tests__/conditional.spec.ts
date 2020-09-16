import { BigNumber } from 'bignumber.js';

import {
  checkedOneOfCreator,
  gteFieldCreator,
  gtFieldCreator,
  lteFieldCreator,
  ltFieldCreator,
  requiredIfCheckedCreator,
  requiredIfSetCreator,
  requireAtLeastOneOfCreator,
  uniqueAmongstCreator,
} from '../conditional';

interface IFormData {
  maxAmount?: BigNumber;
  minAmount?: BigNumber;
}

const GT_MIN = new BigNumber(8);
const EQ_MIN = new BigNumber(7);
const LT_MIN = new BigNumber(6);
const MAX_AMOUNT = new BigNumber(22);

const mockFormDataComplete: IFormData = {
  maxAmount: MAX_AMOUNT,
  minAmount: EQ_MIN,
};

const mockFormDataIncomplete: IFormData = {
  maxAmount: MAX_AMOUNT,
  minAmount: undefined,
};

const mockFormDataEmpty: IFormData = {
  maxAmount: undefined,
  minAmount: undefined,
};

const mockFormForCheckbox = {
  useBla: true,
  useBle: false,
};

interface ICheckboxForm {
  foo: boolean;
  bar: boolean;
  baz: boolean;
}

const mockCheckboxFormDeselected: ICheckboxForm = {
  bar: false,
  baz: false,
  foo: false,
};

const mockCheckboxFormSomeSelected: ICheckboxForm = {
  bar: true,
  baz: false,
  foo: false,
};

const mockCheckboxFormAllSelected: ICheckboxForm = {
  bar: true,
  baz: true,
  foo: true,
};

interface IUniqueForm {
  star1: string;
  star2: string;
  star3: string;
}
const mockUniqueForm: IUniqueForm = {
  star1: 'Deneb',
  star2: 'Altair',
  star3: 'Aldebaran',
};

const mockNonuniqueForm: IUniqueForm = {
  star1: 'Deneb',
  star2: 'Deneb',
  star3: 'Aldebaran',
};

describe('validation', () => {
  describe('gtFieldCreator', () => {
    const greaterThanMin = gtFieldCreator<IFormData>(
      { label: 'A pancake', path: 'pancake' },
      { label: 'Minimal amount of sonic screwdrivers', path: 'minAmount' },
    )();

    it('should return undefined if input value is greater than the dependant field', () => {
      expect(greaterThanMin(GT_MIN, mockFormDataComplete)).toBeUndefined();
    });

    it('should return an error message if the input value is not greater than the dependant field', () => {
      expect(greaterThanMin(EQ_MIN, mockFormDataComplete)).toBeDefined();
      expect(greaterThanMin(EQ_MIN, mockFormDataComplete)).toMatchSnapshot();
      expect(greaterThanMin(LT_MIN, mockFormDataComplete)).toBeDefined();
      expect(greaterThanMin(LT_MIN, mockFormDataComplete)).toMatchSnapshot();
    });

    it('should return undefined message if base value can\'t be extracted from form data', () => {
      expect(greaterThanMin(GT_MIN, mockFormDataIncomplete)).toBeUndefined();
    });
  });

  describe('gteFieldCreator', () => {
    const greaterThanOrEqualToMin = gteFieldCreator<IFormData>(
      { label: 'A pancake', path: 'pancake' },
      { label: 'Minimal amount of sonic screwdrivers', path: 'minAmount' },
    )();

    it('should return undefined if input value is greater than or equal to the dependant field', () => {
      expect(greaterThanOrEqualToMin(GT_MIN, mockFormDataComplete)).toBeUndefined();
      expect(greaterThanOrEqualToMin(EQ_MIN, mockFormDataComplete)).toBeUndefined();
    });

    it('should return an error message if the input value is not greater than or equal to the dependant field', () => {
      expect(greaterThanOrEqualToMin(LT_MIN, mockFormDataComplete)).toBeDefined();
      expect(greaterThanOrEqualToMin(LT_MIN, mockFormDataComplete)).toMatchSnapshot();
    });

    it('should return undefiend message if base value can\'t be extracted from form data', () => {
      expect(greaterThanOrEqualToMin(GT_MIN, mockFormDataIncomplete)).toBeUndefined();
    });
  });

  describe('ltFieldCreator', () => {
    const lessThanMin = ltFieldCreator<IFormData>(
      { label: 'A pancake', path: 'pancake' },
      { label: 'Minimal amount of sonic screwdrivers', path: 'minAmount' },
    )();

    it('should return undefined if input value is less than the dependant field', () => {
      expect(lessThanMin(LT_MIN, mockFormDataComplete)).toBeUndefined();
    });

    it('should return an error message if the input value is not less than the dependant field', () => {
      expect(lessThanMin(GT_MIN, mockFormDataComplete)).toBeDefined();
      expect(lessThanMin(GT_MIN, mockFormDataComplete)).toMatchSnapshot();
      expect(lessThanMin(EQ_MIN, mockFormDataComplete)).toBeDefined();
      expect(lessThanMin(EQ_MIN, mockFormDataComplete)).toMatchSnapshot();
    });

    it('should return undefined message if base value can\'t be extracted from form data', () => {
      expect(lessThanMin(GT_MIN, mockFormDataIncomplete)).toBeUndefined();
    });
  });

  describe('lteFieldCreator', () => {
    const lessThanOrEqualToMin = lteFieldCreator<IFormData>(
      { label: 'A pancake', path: 'pancake' },
      { label: 'Minimal amount of sonic screwdrivers', path: 'minAmount' },
    )();

    it('should return undefined if input value is less than or equal to the dependant field', () => {
      expect(lessThanOrEqualToMin(EQ_MIN, mockFormDataComplete)).toBeUndefined();
      expect(lessThanOrEqualToMin(LT_MIN, mockFormDataComplete)).toBeUndefined();
    });

    it('should return an error message if the input value is not less than or equal to the dependant field', () => {
      expect(lessThanOrEqualToMin(GT_MIN, mockFormDataComplete)).toBeDefined();
      expect(lessThanOrEqualToMin(GT_MIN, mockFormDataComplete)).toMatchSnapshot();
    });

    it('should return undefined message if base value can\'t be extracted from form data', () => {
      expect(lessThanOrEqualToMin(GT_MIN, mockFormDataIncomplete)).toBeUndefined();
    });
  });

  describe('requiredIfSetCreator', () => {
    const requiredIfMinIsSet = requiredIfSetCreator<IFormData>(
      { label: 'Mminimum amount of sonic screwdrivers', path: 'minAmount' },
    )();
    const requiredIfMaxIsSet = requiredIfSetCreator<IFormData>(
      { label: 'Maximum amount of sonic screwdrivers', path: 'maxAmount' },
    )();

    it('should return undefined if dependant field is not set, no matter the input value', () => {
      expect(requiredIfMinIsSet('a value', mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMinIsSet(undefined, mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMinIsSet(null, mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMinIsSet('', mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMinIsSet([], mockFormDataIncomplete)).toBeUndefined();
    });

    it('should return undefined if input value is present and dependant field is set', () => {
      expect(requiredIfMaxIsSet('a value', mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMaxIsSet(22, mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMaxIsSet([1, 2, 3], mockFormDataIncomplete)).toBeUndefined();
      expect(requiredIfMaxIsSet(false, mockFormDataIncomplete)).toBeUndefined();
    });

    it('should return an error message if the input value is not set but dependant field is set', () => {
      expect(requiredIfMaxIsSet(mockFormDataIncomplete.minAmount, mockFormDataIncomplete)).toBeDefined();
      expect(requiredIfMaxIsSet(mockFormDataIncomplete.minAmount, mockFormDataIncomplete)).toMatchSnapshot();
    });
  });

  describe('requiredIfCheckedCreator', () => {
    const requiredButNotChecked = requiredIfCheckedCreator(
      { label: 'Minimum amount of sonic screwdrivers', path: 'useBle' },
    )();
    const requiredButChecked = requiredIfCheckedCreator(
      { label: 'Maximum amount of sonic screwdrivers', path: 'useBla' },
    )();

    it('should return undefined if dependant field is not set, no matter the input value', () => {
      expect(requiredButNotChecked(false, mockFormForCheckbox)).toBeUndefined();
      expect(requiredButNotChecked(undefined, mockFormForCheckbox)).toBeUndefined();
      expect(requiredButNotChecked(null, mockFormForCheckbox)).toBeUndefined();
      expect(requiredButNotChecked('whatever', mockFormForCheckbox)).toBeUndefined();
    });

    it('should return undefined if dependent value is present but set to null, or if input value is present', () => {
      expect(requiredButNotChecked(null, mockFormForCheckbox)).toBeUndefined();
      expect(requiredButChecked('something', mockFormForCheckbox)).toBeUndefined();
      expect(requiredButChecked(0, mockFormForCheckbox)).toBeUndefined();
    });

    it('should return an error message if the input value is not set but dependant field is set', () => {
      expect(requiredButChecked(undefined, mockFormForCheckbox)).toBeDefined();
      expect(requiredButChecked(null, mockFormForCheckbox)).toMatchSnapshot();
    });
  });

  describe('requireAtLeastOneOfCreator', () => {
    const requireAtLeastOneOf = requireAtLeastOneOfCreator<IFormData>([
      { label: 'Minimum amount of sonic screwdrivers', path: 'minAmount' },
      { label: 'Maximum amount of sonic screwdrivers', path: 'maxAmount' },
    ])();

    const requireAtLeastOneOfSingle = requireAtLeastOneOfCreator<IFormData>([
      { label: 'Minimum amount of sonic screwdrivers', path: 'minAmount' },
    ])();

    const requireAtLeastOneOfNone = requireAtLeastOneOfCreator<IFormData>([
    ])();

    it('should return undefined if any of dependant fields is not set', () => {
      expect(requireAtLeastOneOf(undefined, mockFormDataComplete)).toBeUndefined();
      expect(requireAtLeastOneOf(undefined, mockFormDataIncomplete)).toBeUndefined();
      expect(requireAtLeastOneOfSingle(undefined, mockFormDataComplete)).toBeUndefined();
      expect(requireAtLeastOneOfNone(undefined, mockFormDataComplete)).toBeUndefined();
    });

    it('should return an error message if none of dependant fields are set', () => {
      expect(requireAtLeastOneOf(undefined, mockFormDataEmpty)).toBeDefined();
      expect(requireAtLeastOneOf(undefined, mockFormDataEmpty)).toMatchSnapshot();
    });
  });

  describe('checkedOneOfCreator', () => {
    const checkedOneOf = checkedOneOfCreator<ICheckboxForm>(
      (formData) => [formData.bar, formData.baz, formData.foo],
    )();

    it('should return undefined if one of the selected fields is checked', () => {
      expect(checkedOneOf(undefined, mockCheckboxFormSomeSelected)).toBeUndefined();
      expect(checkedOneOf(undefined, mockCheckboxFormAllSelected)).toBeUndefined();
    });

    it('should return undefined message if all of the selected fields aren\'t checked', () => {
      expect(checkedOneOf(undefined, mockCheckboxFormDeselected)).toBeDefined();
      expect(checkedOneOf(undefined, mockCheckboxFormDeselected)).toMatchSnapshot();
    });
  });

  describe('uniqueAmongstCreator', () => {
    const uniqueAmongst = uniqueAmongstCreator<IUniqueForm, string>(
      (formData) => [formData.star1, formData.star2, formData.star3],
    )();

    it('should return an error message if all of the selected fields aren\'t checked', () => {
      expect(uniqueAmongst('Betelgeuse', mockUniqueForm)).toBeUndefined();
      expect(uniqueAmongst(undefined as any, mockUniqueForm)).toBeUndefined();
    });

    it('should return an error if input value is not unique amongst selected form fields', () => {
      expect(uniqueAmongst('Deneb', mockNonuniqueForm)).toBeDefined();
      expect(uniqueAmongst('Deneb', mockNonuniqueForm)).toMatchSnapshot();
    });
  });
});
