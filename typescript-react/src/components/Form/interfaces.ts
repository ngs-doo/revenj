export enum FormType {
  View = 'View',
  Create = 'Create',
  Edit = 'Edit',
}

export interface IFormError {
  message: string | JSX.Element;
  code?: string | number;
}

export enum ErrorType {
  Error,
  Warning,
  QAPink,
}
