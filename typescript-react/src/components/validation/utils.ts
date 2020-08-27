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
