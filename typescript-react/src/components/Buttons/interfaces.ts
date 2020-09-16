export interface ISimpleButton {
  className?: string;
  disabled?: boolean;
  label: string;
  url?: string;
  isExternalUrl?: boolean;
  tooltip?: string;
  onClick?: () => void;
}

export interface IComponentButton {
  Component: React.ComponentType<{}>;
}

export type ButtonItem = ISimpleButton | IComponentButton;

export const isComponentButton = (it: any): it is IComponentButton =>
  it != null && (it as IComponentButton).Component != null;
