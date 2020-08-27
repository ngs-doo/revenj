import { BigNumber } from 'bignumber.js';

import {
  compare,
  compareAlpha,
  eq,
  gt,
  gte,
  gteAlpha,
  gtAlpha,
  lt,
  lte,
  lteAlpha,
  ltAlpha,
  matches,
  multipleOfFactor,
  required,
} from '../operators';

describe('validation', () => {
  describe('operators', () => {
    describe('required', () => {
      it('should check whether the value is present, regardles of type', () => {
        expect(required()(10)).toBe(true);
        expect(required()(0)).toBe(true);
        expect(required()('a non-empty string')).toBe(true);
        expect(required()([1, 2, 3])).toBe(true);
        expect(required()(true)).toBe(true);
        expect(required()(false)).toBe(true);

        expect(required()(null)).toBe(false);
        expect(required()(undefined)).toBe(false);
        expect(required()('')).toBe(false);
        expect(required()([])).toBe(false);
      });
    });

    describe('ordinals comparator', () => {
      it('should compare numbers', () => {
        expect(compare(11, 10)).toBe(1);
        expect(compare(10, 10)).toBe(0);
        expect(compare(10, 11)).toBe(-1);
      });

      it('should compare number strings', () => {
        expect(compare('11', '10')).toBe(1);
        expect(compare('10', '10')).toBe(0);
        expect(compare('10', '11')).toBe(-1);
      });

      it('should compare BigNumbers', () => {
        expect(compare(new BigNumber(11), new BigNumber(10))).toBe(1);
        expect(compare(new BigNumber(10), new BigNumber(10))).toBe(0);
        expect(compare(new BigNumber(10), new BigNumber(11))).toBe(-1);
      });

      it('should throw an error if operator arguments can\'t be converted to BigNumbers', () => {
        const fail = () => {
          compare('a booboo', 'another booboo');
        };
        expect(fail).toThrowErrorMatchingSnapshot();
      });
    });

    describe('alpha comparator', () => {
      it('should compare strings', () => {
        expect(compareAlpha('booboo', 'abbabb')).toBe(1);
        expect(compareAlpha('something', 'something')).toBe(0);
        expect(compareAlpha('dark', 'side')).toBe(-1);
      });
    });

    describe('eq', () => {
      it('should check whether two values are deeply equal', () => {
        expect(eq(3)(3)).toBe(true);
        expect(eq([1, 2, 3])([1, 2, 3])).toBe(true);
        expect(eq(new BigNumber(21))(new BigNumber(21))).toBe(true);

        expect(eq('wat')('foo')).toBe(false);
        expect(eq([1, 2, 3])([1, 2])).toBe(false);
        expect(eq(new BigNumber(21))(new BigNumber(-10))).toBe(false);
      });
    });

    describe('gt', () => {
      it('should check whether value is greater than the minimum value supplied', () => {
        expect(gt(4)(5)).toBe(true);
        expect(gt('20180724')('20180813')).toBe(true);

        expect(gt(4)(4)).toBe(false);
        expect(gt(4)(2)).toBe(false);
        expect(gt('20180724')('20180724')).toBe(false);
        expect(gt('20180724')('20130202')).toBe(false);
      });
    });

    describe('gte', () => {
      it('should check whether value is greater than or equal to the minimum value supplied', () => {
        expect(gte(4)(5)).toBe(true);
        expect(gte(4)(4)).toBe(true);
        expect(gte('20180724')('20180813')).toBe(true);
        expect(gte('20180724')('20180724')).toBe(true);

        expect(gte(4)(2)).toBe(false);
        expect(gte('20180724')('20130202')).toBe(false);
      });
    });

    describe('lt', () => {
      it('should check whether value is less than the maximum value supplied', () => {
        expect(lt(4)(2)).toBe(true);
        expect(lt('20180724')('20130202')).toBe(true);

        expect(lt(4)(4)).toBe(false);
        expect(lt(4)(5)).toBe(false);
        expect(lt('20180724')('20180724')).toBe(false);
        expect(lt('20180724')('20180813')).toBe(false);
      });
    });

    describe('lte', () => {
      it('should check whether value is less than the maximum value supplied', () => {
        expect(lte(4)(2)).toBe(true);
        expect(lte(4)(4)).toBe(true);
        expect(lte('20180724')('20130202')).toBe(true);
        expect(lte('20180724')('20180724')).toBe(true);

        expect(lte(4)(5)).toBe(false);
        expect(lte('20180724')('20180813')).toBe(false);
      });
    });

    describe('gtAlpha', () => {
      it('should check whether value is greater than the minimum value supplied', () => {
        expect(gtAlpha('2018-07-24')('2018-08-13')).toBe(true);

        expect(gtAlpha('2018-07-24')('2018-07-24')).toBe(false);
        expect(gtAlpha('2018-07-24')('2013-02-02')).toBe(false);
      });
    });

    describe('gteAlpha', () => {
      it('should check whether value is greater than or equal to the minimum value supplied', () => {
        expect(gteAlpha('2018-07-24')('2018-08-13')).toBe(true);
        expect(gteAlpha('2018-07-24')('2018-07-24')).toBe(true);

        expect(gteAlpha('2018-07-24')('2013-02-02')).toBe(false);
      });
    });

    describe('ltAlpha', () => {
      it('should check whether value is less than the maximum value supplied', () => {
        expect(ltAlpha('2018-07-24')('2013-02-02')).toBe(true);

        expect(ltAlpha('2018-07-24')('2018-07-24')).toBe(false);
        expect(ltAlpha('2018-07-24')('2018-08-13')).toBe(false);
      });
    });

    describe('lteAlpha', () => {
      it('should check whether value is less than the maximum value supplied', () => {
        expect(lteAlpha('2018-07-24')('2013-02-02')).toBe(true);
        expect(lteAlpha('2018-07-24')('2018-07-24')).toBe(true);

        expect(lteAlpha('2018-07-24')('2018-08-13')).toBe(false);
      });
    });

    describe('multipleOfFactor', () => {
      it('should check whether the input value is a multiple of the base value', () => {
        expect(multipleOfFactor(22)(44)).toBe(true);
        expect(multipleOfFactor(new BigNumber(22))(new BigNumber(44))).toBe(true);

        expect(multipleOfFactor(22)(43)).toBe(false);
        expect(multipleOfFactor(new BigNumber(27))(new BigNumber(44))).toBe(false);
      });
    });

    describe('matches', () => {
      it('should check whether the input value string mathcesh the regex', () => {
        const regexp = /^potato$/;
        expect(matches(regexp)('potato')).toBe(true);
        expect(matches(regexp)('tomato')).toBe(false);
      });
    });
  });
});
