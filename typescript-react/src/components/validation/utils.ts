/* eslint-disable prettier/prettier */
import { get } from '../../util/FunctionalUtils/FunctionalUtils';

export const resolveRelativePath = <F>(relativePath: string, relativeTo: string): keyof F => {
  if (!relativePath.includes('/')) {
    return relativePath as keyof F;
  }

  const fullPath = `${relativeTo.split('.').join('/')}/${relativePath}`;

  const parts = fullPath.split('/');
  const elements: string[] = [];

  parts.forEach((part) => {
    if (part === '..') {
      elements.pop();
    } else if (part === '.') {
      return;
    } else {
      elements.push(part);
    }
  });

  return elements.join('.') as keyof F;
};


export const getBaseValue = <F>(right: any, otherRight: any) =>
  (formValues: F, _: any, fieldName: string) => get(formValues, resolveRelativePath(right.path, fieldName) as keyof F) || get(formValues, resolveRelativePath(otherRight.path, fieldName) as keyof F);

export const valueIsAbsent = (value: any) =>
  (value == null) || (typeof value === 'string' && value === '');
