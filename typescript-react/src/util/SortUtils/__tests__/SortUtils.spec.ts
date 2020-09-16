import { sortBy } from '../SortUtils';

describe('SortUtils', () => {
  describe('sortBy', () => {
    it('should sort in ascending order by a given property', () => {
      const items = [
        {
          age: 52,
          name: 'Steven',
        },
        {
          age: 70,
          name: 'Anne',
        },
        {
          age: 15,
          name: 'Joe',
        },
      ];

      expect(sortBy((it) => it.age, items).map((it) => it.name)).toEqual(['Joe', 'Steven', 'Anne']);
      expect(sortBy((it) => it.name, items).map((it) => it.name)).toEqual(['Anne', 'Joe', 'Steven']);
    });
  });
});
