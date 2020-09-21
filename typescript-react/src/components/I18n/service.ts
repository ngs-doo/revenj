import type { II18nContext } from './I18n';

/**
 * Performs localization (by rules configured in the application on the top level) on strings that are marked as internationalised.
 * The marker used is to being the string with `i18n:`
 * @param localize Configured localization function, must be provided from context
 * @param text Either regular text, or a marked identified noted with an `i18n:` prefix
 * @param defaultValue Optionally, a fallback value that is used when localization is attempted (and string is marked), but no result is found
 */
export const localizeTextIfMarked = (
  localize: II18nContext['localize'],
  text: string,
  defaultValue?: string,
): string => {
  if (!text) {
    return text;
  }

  const shouldBeTranslated = text.toLocaleLowerCase().startsWith('i18n:');
  if (shouldBeTranslated) {
    const path = text.split(':').slice(1).join(':');
    return localize(path, defaultValue);
  }

  return text;
}
