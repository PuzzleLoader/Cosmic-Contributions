package dev.crmodders.puzzle.core.localization;

import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.settings.Preferences;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static dev.crmodders.puzzle.core.PuzzleRegistries.LANGUAGES;

public class LanguageManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("Language Manager");
	public static TranslationEntry UNDEFINED = new TranslationEntry();
	private static Language selectedLanguage;

	public static boolean hasLanguageInstalled(@NotNull TranslationLocale locale) {
		return LANGUAGES.contains(locale.toIdentifier());
	}

	public static void selectLanguage(@NotNull TranslationLocale locale) {
		Language newLanguage = LANGUAGES.get(locale.toIdentifier());
		if (newLanguage != null) {
			selectedLanguage = newLanguage;
		} else {
            LOGGER.error("Language not found {}", locale);
		}
	}

	private static Lang lastLang;
	public static Language getLanguage() {
		if(Lang.currentLang != lastLang) {
			selectLanguage(TranslationLocale.fromLanguageTag(Preferences.chosenLang.getValue()));
			lastLang = Lang.currentLang;
		}
		return selectedLanguage;
	}

	public static TranslationEntry translate(TranslationKey key) {
		Language current = getLanguage();
		if(current == null) {
			if(key == null) {
				return UNDEFINED;
			}
			return new TranslationEntry(key.getIdentifier());
		} else {
			return current.entry(key);
		}
	}

	public static String string(TranslationKey key) {
		return translate(key).string().string();
	}

	public static List<TranslationString> strings(TranslationKey key) {
		return translate(key).strings();
	}

	public static String format(TranslationKey key, Object... args) {
		return translate(key).string().format(args);
	}
}
