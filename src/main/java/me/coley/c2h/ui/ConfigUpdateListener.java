package me.coley.c2h.ui;

import me.coley.c2h.config.model.Configuration;
import me.coley.c2h.config.model.Language;

/**
 * Listener to handle changes to {@link Configuration} / {@link Language} selection.
 *
 * @author Matt Coley
 */
public interface ConfigUpdateListener {
	/**
	 * @param config New config instance.
	 */
	void onConfigChanged(Configuration config);

	/**
	 * @param language Target language from the current config instance.
	 */
	void onTargetLanguageSet(Language language);
}
