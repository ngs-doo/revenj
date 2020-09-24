import { Validator } from 'revenj';

export const lessThan = (amount: number) => Validator.lteCreator(amount)();
export const isPositive = Validator.isPositive;
