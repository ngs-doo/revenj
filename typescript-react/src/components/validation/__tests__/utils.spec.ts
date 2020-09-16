import { resolveRelativePath } from '../utils';

describe('utils', () => {
  describe('resolveRelativePath', () => {
    it('should not process simple paths', () => {
      expect(resolveRelativePath('path.to.somewhere', 'base')).toBe('path.to.somewhere');
      expect(resolveRelativePath('path[0].best.loan', 'root')).toBe('path[0].best.loan');
    });

    it('should use path syntax to resolve relative paths', () => {
      expect(resolveRelativePath('./minValue', 'person.maxValue')).toBe('person.maxValue.minValue');
      expect(resolveRelativePath('../age', 'person.spouse.name')).toBe('person.spouse.age');
      expect(resolveRelativePath('../../name', 'person.spouse.name')).toBe('person.name');
      expect(resolveRelativePath('../../../name', 'person.spouse.name')).toBe('name');
      expect(resolveRelativePath('.././././../../././name/age/..', 'person.spouse.name')).toBe('name');
    });
  });
});
