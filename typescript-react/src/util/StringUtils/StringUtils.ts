export const trim = (string: string) =>
  string.replace(/^\s+|\s+$/g, '');

export const removeSpaces = (string: string) =>
  string.trim().split(' ').join('');

export const fromBool = (bool: boolean) =>
  bool ? 'Yes' : 'No';

export const percentage = (number: number, precision: number = 2) =>
  `${parseFloat(`${number}`).toFixed(precision)}%`;

export const capitalize = (word: string) =>
  word.replace(/^./, (letter) => letter.toLocaleUpperCase());

const isLetter = (text: string) => text.toLocaleLowerCase() !== text.toLocaleUpperCase(); // Hacky, but kinda works!
const isUpper = (text: string) => isLetter(text) && text.toLocaleUpperCase() === text;
const isLower = (text: string) => isLetter(text) && text.toLocaleLowerCase() === text;

export const camelToWord = (term: string): string => {
  const words: string[] = [];
  if (term.length == null || term.length === 0) {
    return term;
  }

  Array.from(term).forEach((letter, index) => {
    const isUpperCase = isUpper(letter);
    const nextIsLower = index < term.length - 1 && isLower(term[index + 1]);
    const previousIsLower = index  === 0 || isLower(term[index - 1]);
    if (index === 0 || (isUpperCase && (previousIsLower || nextIsLower))) {
      words.push(capitalize(letter));
    } else {
      words[words.length - 1] += letter;
    }
  });

  return words.filter((it) => it.length > 0).join(' ');
};

export const leftPad = (str: string, len: number, padding: string = '0') =>
  str.length < len
    ? `${padding.repeat(len - str.length)}${str}`
    : str;

export const includes = (haystack: string, needle: string) =>
  haystack.indexOf(needle) !== -1;

export const findAll = (regex: RegExp, str: string): string[] => {
  if (!regex.global) {
    throw new Error('Cannot run findAll on non-global regular expressions');
  }

  const matches = [];
  while (true) {
    const match = regex.exec(str);
    if (match === null) {
      break;
    }

    matches.push(...match);
  }

  return matches;
};

export const safeToString = <T extends { toString: () => string }>(value?: T, fallback: string = '-') =>
  value != null
    ? String(value)
    : fallback;

export const normalizeString = (text?: string): string | undefined =>
  text != null
    ? text.trim()
    : text;

export const isNullOrEmpty = (str?: string) =>
  str == null || str.trim() === '';

export const safeIsNullOrEmpty = (value?: any) =>
  safeToString(value, '').trim() === '';
