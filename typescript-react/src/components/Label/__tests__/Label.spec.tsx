import * as React from 'react';
import { act, fireEvent, render, screen } from '@testing-library/react';

import { I18nContext, II18nContext } from '../../I18n/I18n';
import { CustomizableText } from '../Label';

const PATH = 'jeFiltersForm.accountIDs';

const baseI18n: II18nContext = {
  customizeLabel: jest.fn().mockResolvedValue({}),
  hasPermissions: true,
  localize: () => undefined,
};

const renderWithI18n = (localize: II18nContext['localize'], customizeLabel: jest.Mock = baseI18n.customizeLabel) =>
  render(
    <I18nContext.Provider value={{ ...baseI18n, localize, customizeLabel }}>
      <CustomizableText defaultValue='Account ID:' paths={[PATH]} />
    </I18nContext.Provider>,
  );

describe('CustomizableText', () => {
  it('renders the default value when no custom label is set', () => {
    renderWithI18n(() => undefined);

    expect(screen.getByText('Account ID:')).toBeDefined();
  });

  it('renders the custom label from localization', () => {
    renderWithI18n((path) => (path === PATH ? 'FULL EDIT:' : undefined));

    expect(screen.getByText('FULL EDIT:')).toBeDefined();
  });

  it('updates the displayed label when localization changes after mount (SMD loaded late)', () => {
    const { rerender } = render(
      <I18nContext.Provider value={baseI18n}>
        <CustomizableText defaultValue='Account ID:' paths={[PATH]} />
      </I18nContext.Provider>,
    );
    expect(screen.getByText('Account ID:')).toBeDefined();

    // SMD arrives later and carries a custom label for this path
    rerender(
      <I18nContext.Provider value={{ ...baseI18n, localize: (path) => (path === PATH ? 'FULL EDIT:' : undefined) }}>
        <CustomizableText defaultValue='Account ID:' paths={[PATH]} />
      </I18nContext.Provider>,
    );

    expect(screen.getByText('FULL EDIT:')).toBeDefined();
  });

  it('submits the edited value on blur and returns to display mode', async () => {
    const customizeLabel = jest.fn().mockResolvedValue({});
    renderWithI18n(() => undefined, customizeLabel);

    fireEvent.click(screen.getByText('Account ID:'), { ctrlKey: true });

    const input = document.querySelector('input') as HTMLInputElement;
    expect(input.value).toBe('Account ID:');
    fireEvent.change(input, { target: { value: 'CHANGED:' } });

    await act(async () => {
      fireEvent.blur(input);
    });

    expect(customizeLabel).toHaveBeenCalledWith(PATH, 'CHANGED:');
    // no custom label in localization yet -> back to the default display value
    expect(screen.getByText('Account ID:')).toBeDefined();
  });
});
