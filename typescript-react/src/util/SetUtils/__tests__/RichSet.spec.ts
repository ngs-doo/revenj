import {
  areEqual,
  difference,
  intersection,
  isSubset,
  isSuperset,
  union,
} from '../SetUtils';

describe('SetUtils', () => {

  describe('union', () => {
    it('should support union with a set, which produces a Set with members that are in either', () => {
      const xs = new Set([1, 2, 3]);
      const ys = new Set([2, 3, 4]);
      const us = union(xs, ys);

      expect(us).toBeInstanceOf(Set);
      expect(xs.size).toBe(3);
      expect(ys.size).toBe(3);
      expect(us.size).toBe(4);
      expect(Array.from(us.values())).toEqual([1, 2, 3, 4]);
    });
  });

  describe('intersection', () => {
    it('should support intersection with a set, which produces a Set with only members that are in both', () => {
      const xs = new Set([1, 2, 3]);
      const ys = new Set([2, 3, 4]);
      const is = intersection(xs, ys);

      expect(is).toBeInstanceOf(Set);
      expect(xs.size).toBe(3);
      expect(ys.size).toBe(3);
      expect(is.size).toBe(2);
      expect(Array.from(is.values())).toEqual([2, 3]);
    });
  });

  describe('difference', () => {
    it('should support difference with a set, which only returns member that are in this set, but not in the other', () => {
      const xs = new Set([1, 2, 3]);
      const ys = new Set([2, 3, 4]);
      const ds = difference(xs, ys);

      expect(ds).toBeInstanceOf(Set);
      expect(xs.size).toBe(3);
      expect(ys.size).toBe(3);
      expect(ds.size).toBe(1);
      expect(Array.from(ds.values())).toEqual([1]);
    });
  });

  describe('isSuperset', () => {
    it('should check whether the first set contains all elements of the second', () => {
      const xs = new Set([1, 2, 3]);

      expect(isSuperset(xs, xs)).toBe(true);
      expect(isSuperset(xs, new Set())).toBe(true);
      expect(isSuperset(xs, new Set([1, 2]))).toBe(true);
      expect(isSuperset(xs, new Set([1, 2, 3, 4]))).toBe(false);
      expect(isSuperset(xs, new Set([1, 5]))).toBe(false);
    });
  });

  describe('isSubset', () => {
    it('should check whether the second set contains all elements of the first', () => {
      const xs = new Set([1, 2, 3]);

      expect(isSubset(xs, xs)).toBe(true);
      expect(isSubset(xs, new Set())).toBe(false);
      expect(isSubset(new Set(), xs)).toBe(true);
      expect(isSubset(xs, new Set([1, 2]))).toBe(false);
      expect(isSubset(xs, new Set([1, 2, 3, 4]))).toBe(true);
      expect(isSubset(xs, new Set([1, 5]))).toBe(false);
    });
  });

  describe('areEqual', () => {
    it('should check whether the first set and second set have the same members', () => {
      const xs = new Set([1, 2, 3]);

      expect(areEqual(xs, xs)).toBe(true);
      expect(areEqual(xs, new Set([3, 2, 1]))).toBe(true);
      expect(areEqual(xs, new Set())).toBe(false);
      expect(areEqual(xs, new Set([1, 2]))).toBe(false);
      expect(areEqual(xs, new Set([1, 2, 3, 4]))).toBe(false);
      expect(areEqual(xs, new Set([1, 5]))).toBe(false);
    });
  });
});
