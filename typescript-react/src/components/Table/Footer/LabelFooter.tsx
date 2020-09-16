import * as React from 'react';

interface ILabelFooterProps {
  label: string;
}

export class LabelFooter extends React.PureComponent<ILabelFooterProps | any> {
  render() {
    const {
      className,
      label,
      style,
    } = this.props;
    return <div style={style} className={className}>{label}</div>;
  }
}
