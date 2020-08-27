import { getLabel, getTopNLabels } from '../helpers';

const mockData = [
  { name: 'Seb', gender: 'M', powerLevel: 200 },
  { name: 'Ana', gender: 'F', powerLevel: 180 },
  { name: 'Lea', gender: 'F', powerLevel: 7 },
  { name: 'Rikard', gender: 'M', powerLevel: 10000 },
  { name: 'Kreso', gender: 'M', powerLevel: 201 },
  { name: 'Marina', gender: 'F', powerLevel: 420 },
  { name: 'Domagoj', gender: 'M', powerLevel: 204 },
  { name: 'Dog', gender: 'M', powerLevel: 0 },
  { name: 'Cat', gender: 'F', powerLevel: 9999 },
];

describe('chart helpers', () => {
  describe('getLabel', () => {
    const thing = mockData[0];
    it('gets the string or placeholder by key', () => {
      expect(getLabel(thing, ['name'])).toBe('Seb');
      expect(getLabel(thing, ['lame'] as any)).toBe('—');
    });

    it('combines values with commas', () => {
      expect(getLabel(thing, ['name', 'gender'])).toBe('Seb, M');
      expect(getLabel(thing, ['name', 'lame'] as any)).toBe('Seb, —');
    });
  });

  describe('getTopNLabels', () => {
    it('gets the labels in descending order by the value of Y', () => {
      expect(getTopNLabels(mockData, ['name'], 'powerLevel', 10)).toStrictEqual([
        'Rikard',
        'Cat',
        'Marina',
        'Domagoj',
        'Kreso',
        'Seb',
        'Ana',
        'Lea',
        'Dog',
      ]);
    });

    it('only returns N labels', () => {
      expect(getTopNLabels(mockData, ['name'], 'powerLevel', 3)).toStrictEqual([
        'Rikard',
        'Cat',
        'Marina',
      ]);
    });

    it('works for composite labels', () => {
      expect(getTopNLabels(mockData, ['name', 'gender'], 'powerLevel', 10)).toStrictEqual([
        'Rikard, M',
        'Cat, F',
        'Marina, F',
        'Domagoj, M',
        'Kreso, M',
        'Seb, M',
        'Ana, F',
        'Lea, F',
        'Dog, M',
      ]);
    });
  });
});
