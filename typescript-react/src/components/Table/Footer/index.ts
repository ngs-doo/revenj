import { IColumnConfig } from '../Table';
import { AggregateFooter } from './AggregateFooter';
import { LabelFooter } from './LabelFooter';

export enum FooterType {
  Aggregate = 'Aggregate',
  Label = 'Label',
}

export const getFooterComponent = (config: IColumnConfig<any>) => {
  const footerType = (() => {
    if (config.footerType != null) {
      return config.footerType;
    }

    if (typeof config.aggregate === 'function') {
      return FooterType.Aggregate;
    }

    if (typeof config.aggregate === 'string') {
      return FooterType.Label;
    }

    return null;
  })();

  switch (footerType) {
    case FooterType.Aggregate: return AggregateFooter;
    case FooterType.Label: return LabelFooter;
    default: return null;
  }
};

export {
  AggregateFooter,
  LabelFooter,
};
