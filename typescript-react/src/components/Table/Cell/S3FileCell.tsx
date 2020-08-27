import * as React from 'react';

import { ApiContext } from '../../Api/ApiContext';
import { get } from '../../../util/FunctionalUtils/FunctionalUtils';
import { ICellProps } from '../interfaces';
import { LinkCell } from './LinkCell';

// FIXME: @bigd -> this as any is here because table typings are borked
const formatter: any = (s3?: S3) => s3?.name;

export class S3FileCell extends React.PureComponent<ICellProps<S3, string>> {
  public static contextType = ApiContext;
  public context: React.ContextType<typeof ApiContext>;

  public render() {
    return (
      <LinkCell<S3>
        {...this.props}
        constructUrl={this.getUrl}
        formatter={formatter}
        download
        isExternal
      />
    );
  }

  private getUrl = (model: any, row: any): string => {
    const s3: S3 | undefined = get(model, row.field!);
    return s3 ? this.context!.getS3DownloadUrl(s3!) : '#';
  }
}
