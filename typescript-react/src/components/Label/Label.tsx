import * as React from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ControlLabel from 'react-bootstrap/FormLabel';

import { I18nContext } from '../I18n/I18n';
import { localizeText } from '../I18n/service';

interface ICustomizableProps {
  className?: string;
  defaultValue: string;
  message?: string;
  paths: string[];
}

export const CustomizableText = ({ defaultValue, className, paths }: ICustomizableProps) => {
  const { customizeLabel, hasPermissions, localize } = React.useContext(I18nContext);
  const [isEdit, setIsEdit] = React.useState(false);
  const [value, setValue] = React.useState(localizeText(localize, defaultValue, paths));

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
    if (hasPermissions && e.ctrlKey) {
      setIsEdit(true);
    }
  }

  return (
    hasPermissions && isEdit ? (
      <FormControl
        autoFocus
        onBlur={onEdit}
        onChange={(e) => setValue(e.target.value)}
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
