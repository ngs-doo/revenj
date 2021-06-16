import React from 'react';
import { FormType } from '../components/Form/interfaces';
import { CellType, IActionInfo, IColumnConfig, IRowConfig } from '../components/Table/Table';

interface IReferences {
  edit?: string[];
  view?: string[];
}

export const hasPermissions = (
  userRoles: Set<string>,
  roles: string[],
): boolean =>
  userRoles.size === 0 || roles.length === 0 || roles.some((role) => userRoles.has(role));


export const getTableActions = <T>(
  navigateTo: (formType: FormType, id?: any) => void,
  canNavigateTo: (formType: FormType, item?: T) => boolean,
  userRoles: Set<string>,
  getIdentifier: (item: T) => string,
  references: IReferences,
  externalActions: Array<(row: T) => React.ReactElement<any>> = [],
): IRowConfig<T> => [
  {
    actions: [
      references.view ? {
        className: 'btn btn-xs btn-primary',
        icon: 'fa fa-eye',
        isVisible: (it: any) => canNavigateTo(FormType.View, it)
          && hasPermissions(userRoles, references.view!),
        onClick: (_: any, row: any) => navigateTo(FormType.View, getIdentifier(row)),
        tooltip: 'View',
      } : undefined,
      references.edit ? {
        className: 'btn btn-xs btn-primary',
        icon: 'fa fa-pencil',
        isVisible: (it: any) => canNavigateTo(FormType.Edit, it)
          && hasPermissions(userRoles, references.edit!),
        onClick: (_: any, row: any) => navigateTo(FormType.Edit, getIdentifier(row)),
        tooltip: 'Edit',
      } : undefined,
      ...externalActions.map((handler): IActionInfo<T> => ({
        customComponent: handler,
      })),
    ].filter((it) => it != null) as Array<IActionInfo<T>>,
    cellType: CellType.Actions,
    field: '',
    minWidth: 100,
    style: { display: 'flex' },
    title: 'Actions',
  },
];

const mergeActionsByTooltip = <T>(actionsA: Array<IActionInfo<T>> = [], actionsB: Array<IActionInfo<T>> = []): Array<IActionInfo<T>> => {
  const merged: Array<IActionInfo<T>> = [];
  const actionsForA = actionsA.map((cellConfig) => cellConfig.tooltip);
  const actionsForB = actionsB.map((cellConfig) => cellConfig.tooltip);
  const addedActions = actionsForB.filter((field) => field == null || !actionsForA.includes(field));

  for (const tooltip of actionsForA) {
    const actionA = actionsA.find((action) => action.tooltip === tooltip);
    const actionB = actionsB.find((action) => action.tooltip === tooltip);
    const mergedAction = tooltip != null && actionB != null ? { ...actionA, ...actionB } : actionA;
    merged.push(mergedAction!);
  }

  for (const tooltip of addedActions) {
    merged.push(actionsB.find((action) => action.tooltip === tooltip)!);
  }

  return merged;
};

export const mergeColumnConfigs = <T>(configA: IRowConfig<T>, configB: IRowConfig<T>) => {
  const merged: IRowConfig<T> = [];
  const fieldsForA = configA.map((cellConfig) => cellConfig.field + cellConfig.title);
  const fieldsForB = configB.map((cellConfig) => cellConfig.field + cellConfig.title);
  const addedFields = fieldsForB.filter((field) => field == null || !fieldsForA.includes(field));

  for (const field of fieldsForA) {
    const cellConfigA = configA.find((cellConfig) => (cellConfig.field + cellConfig.title) === field);
    const cellConfigB = configB.find((cellConfig) => (cellConfig.field + cellConfig.title) === field);
    const mergedCellConfig = field != null && cellConfigB != null ? {...cellConfigA, ...cellConfigB} : cellConfigA;
    const actions = cellConfigA && cellConfigA.actions != null || cellConfigB && cellConfigB.actions != null
      ? mergeActionsByTooltip(cellConfigA ? cellConfigA.actions : [], cellConfigB ? cellConfigB.actions : [])
      : undefined;

    if (actions) {
      mergedCellConfig!.actions = actions;
    }

    merged.push(mergedCellConfig!);
  }

  for (const field of addedFields) {
    merged.push(configB.find((cellConfig) => (cellConfig.field + cellConfig.title) === field)! as IColumnConfig<T>);
  }

  return merged;
};
