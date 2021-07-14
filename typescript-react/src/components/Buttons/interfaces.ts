import { ButtonProps } from "react-bootstrap/esm/Button";

export interface ISimpleButton {
  className?: string;
  disabled?: boolean;
  label: string;
  url?: string;
  isExternalUrl?: boolean;
  tooltip?: string;
  onClick?: () => void;
}

export interface IComponentButtonProps extends ButtonProps { 
  props?: {
    item?: any;
  };
  label?: string;
  className?: string;
  iconClassName?: string;
}
export interface IComponentButton extends ButtonProps {
  Component: React.ComponentType<IComponentButtonProps>;
  values?: any;
  label?: string;
  className?: string;
  iconClassName?: string;
}

export type ButtonItem = ISimpleButton | IComponentButton;

export const isComponentButton = (it: any): it is IComponentButton =>
  it != null && (it as IComponentButton).Component != null;
