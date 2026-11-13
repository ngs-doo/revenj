import * as React from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ControlLabel from 'react-bootstrap/FormLabel';

import { I18nContext } from '../I18n/I18n';
import { localizeText } from '../I18n/service';

export interface ICustomizableProps {
  className?: string;
  defaultValue: string;
  message?: string;
  paths: string[];
}

export const CustomizableText = ({ defaultValue, className, paths }: ICustomizableProps) => {
  const { customizeLabel, hasPermissions, localize } = React.useContext(I18nContext);
  const [isEdit, setIsEdit] = React.useState(false);
  // draft is only used while editing; the displayed value is always derived from the current
  // localization (SMD) state, so labels keep updating after SMD loads/changes post-mount
  const [draft, setDraft] = React.useState('');

  const value = isEdit ? draft : localizeText(localize, defaultValue, paths);

  const onEdit = async () => {
    if (hasPermissions) {
      try {
        await customizeLabel(paths[0], value);
      } catch (error) {
        console.error(`Could not set ${paths[0]} to ${value}`);
        throw error;
      } finally {
        setIsEdit(false);
      }
    }
  }

  const onClick = async (e: React.MouseEvent) => {
    if (hasPermissions && (e.ctrlKey || e.metaKey)) {
      setDraft(value);
      setIsEdit(true);
    }
  }

  return (
    hasPermissions && isEdit ? (
      <FormControl
        autoFocus
        onBlur={onEdit}
        onChange={(e) => setDraft(e.target.value)}
        type='text'
        value={value}
      />
    ) : (
      <span
        onClick={onClick}
        className={className}
      >
        {value}
      </span>
    )
  );
};

export const CustomizableLabel = ({ defaultValue, className, paths }: ICustomizableProps) => {
  return (
    <ControlLabel>
      <CustomizableText
        defaultValue={defaultValue}
        className={className}
        paths={paths}
      />
    </ControlLabel>
  );
}
