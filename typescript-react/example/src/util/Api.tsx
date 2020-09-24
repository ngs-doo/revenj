import classNames from 'classnames';
import React from 'react';
import { SubmissionError } from 'redux-form';
import { IExportButton, Serialized } from 'revenj';

import { notifyError} from './notify';

async function onSubmit<T>(nameWithModule: string, domainObject: Serialized<T>): Promise<Serialized<T>> {
  const response = await fetch(`/submit/${nameWithModule}`, {
    body: JSON.stringify(domainObject),
    headers: new Headers({ 'Content-Type': 'application/json' }),
    method: 'post',
  });

  if (response.status >= 200 && response.status < 300) {
    try {
      return await response.json();
    } catch (error) {
      notifyError('Failed to parse response!');
      throw error;
    }
  } else {
    if (response.headers.get('content-type')?.startsWith('application/json')) {
      throw new SubmissionError(await response.json());
    } else {
      throw await response.text();
    }
  }
}

export const api = {
  onSubmit,
};

export const ExportButton: React.FC<IExportButton> = ({ onDownload, className, disabled}) => (
  <button
    className={classNames(className, 'btn btn-primary')}
    disabled={disabled}
    onClick={React.useCallback(() => onDownload(), [onDownload])}
  >
    Export
  </button>
);

export async function onExport<T>(data: T, domainObjectName: string, templateName: string): Promise<void> {
  return new Promise((resolve, reject) => {
    try {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', `/export/${templateName}/${domainObjectName}`);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.responseType = 'blob';
      xhr.onload = function() {
        if (this.status === 200) {
          const fileName = `${templateName}.xlsx`;
          const blob = new Blob(
            [this.response], // tslint:disable-line
            { type: this.response.type ?? 'application/vdn.ms-excel' },
          );
          const downloadUrl = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = downloadUrl;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          a.parentElement?.removeChild(a);
          resolve();
        } else {
          reject(this.responseText);
        }
      };
      xhr.send(JSON.stringify(data));
    } catch (error) {
      notifyError(error);
      reject(error);
    }
  });
}
