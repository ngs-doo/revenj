import * as React from 'react';

import { ISubmittable, CreatePresenterContext, ICreatePresenterContext } from '../Form/Context';
import { FormType } from '../Form/interfaces';

interface ICreatePresenter<T> {
  presenterName?: string;
  domainObject: ISubmittable<T>;
  onSubmitSuccess?: (data: T) => void;
  onSubmit?: (data: T, generatedHandler: (original: T) => Promise<T>) => Promise<T>;
}

export class CreatePresenter<T> extends React.PureComponent<ICreatePresenter<T>> {
  public render() {
    const { children } = this.props;

    return (
      <CreatePresenterContext.Provider value={this.getContext()}>
        {children}
      </CreatePresenterContext.Provider>
    );
  }

  private getContext = (): ICreatePresenterContext<T> => ({
    conceptName: this.props.presenterName != null ? this.props.presenterName : this.props.domainObject.domainObjectName,
    formType: FormType.Create,
    onSubmit: this.onSubmit,
  })

  private onSubmit = async (data: T): Promise<T> => {
    const { domainObject, onSubmit, onSubmitSuccess } = this.props;

    const result = onSubmit
      ? await onSubmit(data, domainObject.submit)
      : await domainObject.submit(data);

    if (onSubmitSuccess) {
      onSubmitSuccess(result);
    }

    return result;
  }
}
