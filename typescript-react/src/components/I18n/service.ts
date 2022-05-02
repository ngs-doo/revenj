import type { II18nContext } from './I18n';

/**
 * Performs localization (by rules configured in the application on the top level) on strings that are marked as internationalised.
 * The marker used is to being the string with `i18n:`
 * @param localize Configured localization function, must be provided from context
 * @param defaultValue Value that is used when localization is attempted (and string is marked), but no result is found
 * @param paths Paths for the localization method, first one that returns the result will be used
 */

export const localizeText = (
  localize: II18nContext['localize'],
  defaultValue: string,
  paths: string[],
): string => {
  for (const path of paths) {
    const value = localize(path);
    if (value != null) {
      return value;
    }
  }
  return defaultValue;
}
