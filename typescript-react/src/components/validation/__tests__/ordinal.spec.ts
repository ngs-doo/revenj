import { BigNumber } from 'bignumber.js';

import { Ord } from '../interfaces';
import {
  gteOrdAlphaCreator,
  gteOrdCreator,
  gtOrdAlphaCreator,
  gtOrdCreator,
  inIntervalCreator,
  inSegmentCreator,
  lteOrdAlphaCreator,
  lteOrdCreator,
  ltOrdAlphaCreator,
  ltOrdCreator,
} from '../ordinal';

describe('validation', () => {
  describe('gtOrdCreator', () => {
    const gt10 = gtOrdCreator(10)();
    const gt100 = gtOrdCreator('100')();
    const gt10Custom = gtOrdCreator(10)({
      getValidatorErrorMessageOverride: (value) => `This quick brown fox can only jump over more than ${value} lazy dogs.`,
    });
    const gt100Custom = gtOrdCreator('100')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be greater than ${value}!`,
    });

    it('should return an error message on value that is less than or equal', () => {
      expect(gt10(9, {})).toBeDefined();
      expect(gt10(9, {})).toMatchSnapshot();
      expect(gt10(10, {})).toBeDefined();
      expect(gt10(10, {})).toMatchSnapshot();
      expect(gt100('99', {})).toBeDefined();
      expect(gt100('99', {})).toMatchSnapshot();
      expect(gt100('100', {})).toBeDefined();
      expect(gt100('100', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is greater', () => {
      expect(gt10(11, {})).toBeUndefined();
      expect(gt100('1000', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not greater than', () => {
      expect(gt10Custom(10, {})).toBeDefined();
      expect(gt10Custom(10, {})).toMatchSnapshot();
      expect(gt10Custom(9, {})).toBeDefined();
      expect(gt10Custom(9, {})).toMatchSnapshot();
      expect(gt100Custom('99', {})).toBeDefined();
      expect(gt100Custom('99', {})).toMatchSnapshot();
      expect(gt100Custom('100', {})).toBeDefined();
      expect(gt100Custom('100', {})).toMatchSnapshot();
    });
  });

  describe('gteOrdCreator', () => {
    const gte10 = gteOrdCreator(10)();
    const gte100 = gteOrdCreator('100')();
    const gte10Custom = gteOrdCreator(10)({
      getValidatorErrorMessageOverride: (value) => `This quick brown fox can only jump over ${value} or more lazy dogs.`,
    });
    const gte100Custom = gtOrdCreator('100')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be greater than or equal to ${value}!`,
    });

    it('should return an error message on value that is less than', () => {
      expect(gte10(9, {})).toBeDefined();
      expect(gte10(9, {})).toMatchSnapshot();
      expect(gte100('99', {})).toBeDefined();
      expect(gte100('99', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is greater than or equal', () => {
      expect(gte10(11, {})).toBeUndefined();
      expect(gte10(10, {})).toBeUndefined();
      expect(gte100('100', {})).toBeUndefined();
      expect(gte100('1000', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not greater than or equal', () => {
      expect(gte10Custom(9, {})).toBeDefined();
      expect(gte10Custom(9, {})).toMatchSnapshot();
      expect(gte100Custom('99', {})).toBeDefined();
      expect(gte100Custom('99', {})).toMatchSnapshot();
    });
  });

  describe('ltOrdCreator', () => {
    const lt10 = ltOrdCreator(10)();
    const lt100 = ltOrdCreator('100')();
    const lt10Custom = ltOrdCreator(10)({
      getValidatorErrorMessageOverride: (value) => `This quick brown fox can only jump under ${value} lazy dogs.`,
    });
    const lt100Custom = ltOrdCreator('100')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be less than ${value}!`,
    });

    it('should return an error message on value that is greater than or equal', () => {
      expect(lt10(11, {})).toBeDefined();
      expect(lt10(11, {})).toMatchSnapshot();
      expect(lt10(10, {})).toBeDefined();
      expect(lt10(10, {})).toMatchSnapshot();
      expect(lt100('100', {})).toBeDefined();
      expect(lt100('100', {})).toMatchSnapshot();
      expect(lt100('1000', {})).toBeDefined();
      expect(lt100('1000', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is less than', () => {
      expect(lt10(9, {})).toBeUndefined();
      expect(lt100('99', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not less than', () => {
      expect(lt10Custom(10, {})).toBeDefined();
      expect(lt10Custom(10, {})).toMatchSnapshot();
      expect(lt10Custom(11, {})).toBeDefined();
      expect(lt10Custom(11, {})).toMatchSnapshot();
      expect(lt100Custom('100', {})).toBeDefined();
      expect(lt100Custom('100', {})).toMatchSnapshot();
      expect(lt100Custom('1000', {})).toBeDefined();
      expect(lt100Custom('1000', {})).toMatchSnapshot();
    });
  });

  describe('lteOrdCreator', () => {
    const lte10 = lteOrdCreator(10)();
    const lte100 = lteOrdCreator('100')();
    const lte10Custom = lteOrdCreator(10)({
      getValidatorErrorMessageOverride: (value) => `This quick brown fox can only jump under ${value} or less lazy dogs.`,
    });
    const lte100Custom = ltOrdCreator('100')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be less than or equal to ${value}!`,
    });

    it('should return an error message on value that is not less than or equal', () => {
      expect(lte10(11, {})).toBeDefined();
      expect(lte10(11, {})).toMatchSnapshot();
      expect(lte100('1000', {})).toBeDefined();
      expect(lte100('1000', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is less than or equal', () => {
      expect(lte10(9, {})).toBeUndefined();
      expect(lte10(10, {})).toBeUndefined();
      expect(lte100('100', {})).toBeUndefined();
      expect(lte100('99', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not less than or equal', () => {
      expect(lte10Custom(11, {})).toBeDefined();
      expect(lte10Custom(11, {})).toMatchSnapshot();
      expect(lte100Custom('1000', {})).toBeDefined();
      expect(lte100Custom('1000', {})).toMatchSnapshot();
    });
  });

  describe('gtOrdAlphaCreator', () => {
    const gtBBB = gtOrdAlphaCreator('BBB')();
    const gtBBBCustom = gtOrdAlphaCreator('BBB')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be greater than ${value}!`,
    });

    it('should return an error message on value that is less than or equal', () => {
      expect(gtBBB('AAA', {})).toBeDefined();
      expect(gtBBB('AAA', {})).toMatchSnapshot();
      expect(gtBBB('BBB', {})).toBeDefined();
      expect(gtBBB('BBB', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is greater', () => {
      expect(gtBBB('CCC', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not greater than', () => {
      expect(gtBBBCustom('BBB', {})).toBeDefined();
      expect(gtBBBCustom('BBB', {})).toMatchSnapshot();
      expect(gtBBBCustom('AAA', {})).toBeDefined();
      expect(gtBBBCustom('AAA', {})).toMatchSnapshot();
    });
  });

  describe('gteOrdAlphaCreator', () => {
    const gteBBB = gteOrdAlphaCreator('BBB')();
    const gteBBBCustom = gtOrdAlphaCreator('BBB')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be greater than or equal to ${value}!`,
    });

    it('should return an error message on value that is less than', () => {
      expect(gteBBB('AAA', {})).toBeDefined();
      expect(gteBBB('AAA', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is greater than or equal', () => {
      expect(gteBBB('BBB', {})).toBeUndefined();
      expect(gteBBB('CCC', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not greater than or equal', () => {
      expect(gteBBBCustom('AAA', {})).toBeDefined();
      expect(gteBBBCustom('AAA', {})).toMatchSnapshot();
    });
  });

  describe('ltOrdAlphaCreator', () => {
    const ltBBB = ltOrdAlphaCreator('BBB')();
    const ltBBBCustom = ltOrdAlphaCreator('BBB')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be less than ${value}!`,
    });

    it('should return an error message on value that is greater than or equal', () => {
      expect(ltBBB('CCC', {})).toBeDefined();
      expect(ltBBB('CCC', {})).toMatchSnapshot();
      expect(ltBBB('BBB', {})).toBeDefined();
      expect(ltBBB('BBB', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is less than', () => {
      expect(ltBBB('AAA', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not less than', () => {
      expect(ltBBBCustom('BBB', {})).toBeDefined();
      expect(ltBBBCustom('BBB', {})).toMatchSnapshot();
      expect(ltBBBCustom('CCC', {})).toBeDefined();
      expect(ltBBBCustom('CCC', {})).toMatchSnapshot();
    });
  });

  describe('lteOrdAlphaCreator', () => {
    const lteBBB = lteOrdAlphaCreator('BBB')();
    const lteBBBCustom = ltOrdAlphaCreator('BBB')({
      getValidatorErrorMessageOverride: (value) => `This makes no sense! Be less than or equal to ${value}!`,
    });

    it('should return an error message on value that is not less than or equal', () => {
      expect(lteBBB('CCC', {})).toBeDefined();
      expect(lteBBB('CCC', {})).toMatchSnapshot();
    });

    it('should return undefiend on value that is less than or equal', () => {
      expect(lteBBB('AAA', {})).toBeUndefined();
      expect(lteBBB('BBB', {})).toBeUndefined();
    });

    it('should return a custom error message on failure when value is not less than or equal', () => {
      expect(lteBBBCustom('CCC', {})).toBeDefined();
      expect(lteBBBCustom('CCC', {})).toMatchSnapshot();
    });
  });

  describe('inInterval', () => {
    const inInterval = inIntervalCreator({ min: 10, max: 20 })();

    it('should return undefined if value is inside the interval', () => {
      expect(inInterval(11, {})).toBeUndefined();
      expect(inInterval(19, {})).toBeUndefined();
      expect(inInterval('11', {})).toBeUndefined();
      expect(inInterval('19', {})).toBeUndefined();
      expect(inInterval(new BigNumber(11), {})).toBeUndefined();
      expect(inInterval(new BigNumber(19), {})).toBeUndefined();
    });

    it('should return undefined if any of the interval values are missing', () => {
      const missingMin = inIntervalCreator({ max: 10 })();
      const missingMax = inIntervalCreator({ min: 10 })();
      const missingBoth = inIntervalCreator({})();

      expect(missingMin(11, {})).toBeUndefined();
      expect(missingMax(9, {})).toBeUndefined();
      expect(missingBoth(1234, {})).toBeUndefined();
    });

    it('should return a validation error message is not inside the interval', () => {
      expect(inInterval(10, {})).toBeDefined();
      expect(inInterval(10, {})).toMatchSnapshot();
      expect(inInterval(20, {})).toBeDefined();
      expect(inInterval(20, {})).toMatchSnapshot();
      expect(inInterval('10', {})).toBeDefined();
      expect(inInterval('10', {})).toMatchSnapshot();
      expect(inInterval('20', {})).toBeDefined();
      expect(inInterval('20', {})).toMatchSnapshot();
      expect(inInterval(new BigNumber(10), {})).toBeDefined();
      expect(inInterval(new BigNumber(10), {})).toMatchSnapshot();
      expect(inInterval(new BigNumber(20), {})).toBeDefined();
      expect(inInterval(new BigNumber(20), {})).toMatchSnapshot();
    });

    it('should throw if range values or input supplied are not treatable as numbers', () => {
      const fail = (value: Ord) => () => {
        const failidator = inIntervalCreator({ min: 'not a number', max: 'somthing' })();
        failidator(value, {});
      };

      expect(fail(15)).toThrowErrorMatchingSnapshot();
      expect(fail('fifteen')).toThrowErrorMatchingSnapshot();
    });
  });

  describe('inSegment', () => {
    const inSegment = inSegmentCreator({ min: 10, max: 20 })();

    it('should return undefined if value is inside the segment', () => {
      expect(inSegment(10, {})).toBeUndefined();
      expect(inSegment(20, {})).toBeUndefined();
      expect(inSegment('10', {})).toBeUndefined();
      expect(inSegment('20', {})).toBeUndefined();
      expect(inSegment(new BigNumber(10), {})).toBeUndefined();
      expect(inSegment(new BigNumber(20), {})).toBeUndefined();
    });

    it('should return undefined if any of the segment values are missing', () => {
      const missingMin = inSegmentCreator({ max: 10 })();
      const missingMax = inSegmentCreator({ min: 10 })();
      const missingBoth = inSegmentCreator({})();

      expect(missingMin(11, {})).toBeUndefined();
      expect(missingMax(9, {})).toBeUndefined();
      expect(missingBoth(1234, {})).toBeUndefined();
    });

    it('should return a validation error message is not inside the segment', () => {
      expect(inSegment(9, {})).toBeDefined();
      expect(inSegment(9, {})).toMatchSnapshot();
      expect(inSegment(21, {})).toBeDefined();
      expect(inSegment(21, {})).toMatchSnapshot();
      expect(inSegment('9', {})).toBeDefined();
      expect(inSegment('9', {})).toMatchSnapshot();
      expect(inSegment('21', {})).toBeDefined();
      expect(inSegment('21', {})).toMatchSnapshot();
      expect(inSegment(new BigNumber(9), {})).toBeDefined();
      expect(inSegment(new BigNumber(9), {})).toMatchSnapshot();
      expect(inSegment(new BigNumber(21), {})).toBeDefined();
      expect(inSegment(new BigNumber(21), {})).toMatchSnapshot();
    });

    it('should throw if range values or input supplied are not treatable as numbers', () => {
      const fail = (value: Ord) => () => {
        const failidator = inSegmentCreator({ min: 'not a number', max: 'somthing' })();
        failidator(value, {});
      };

      expect(fail(15)).toThrowErrorMatchingSnapshot();
      expect(fail('fifteen')).toThrowErrorMatchingSnapshot();
    });

  });
});
