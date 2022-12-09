package me.coley.c2h.ui.pane;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import me.coley.c2h.config.model.Configuration;
import me.coley.c2h.config.model.Language;
import me.coley.c2h.ui.ConfigUpdateListener;

/**
 * Configuration pane for tweaking the {@link Language} values in a {@link Configuration} instance.
 *
 * @author Matt Coley
 */
public class ConfigPane extends BorderPane {
	private final Configuration configuration;
	private final Language targetLanguage;
	private ConfigUpdateListener listener;

	/**
	 * @param initialConfiguration
	 * 		Initial config state to pull from.
	 */
	public ConfigPane(Configuration initialConfiguration) {
		if (initialConfiguration == null || initialConfiguration.getLanguages().isEmpty()) {
			throw new IllegalArgumentException("Must supply non-empty config");
		}
		Language initialLanguage = initialConfiguration.getLanguages().get(0);
		configuration = initialConfiguration;
		targetLanguage = initialLanguage;


		setCenter(new Label("TODO"));
		// TODO:
		//  - config
		//    - load/export configuration
		//  - languages
		//    - cycle between langs in config
		//    - add/remove language
		//  - language
		//    - change name
		//    - add/remove rule
		//  - rule
		//    - change name
		//    - change pattern
		//    - change style
	}

	/**
	 * @param listener
	 * 		Listener to receive updates from changes to the configuration.
	 */
	public void setListener(ConfigUpdateListener listener) {
		this.listener = listener;
		// When assigned, pass the config and initial language.
		if (listener != null) {
			listener.onConfigChanged(configuration);
			listener.onTargetLanguageSet(targetLanguage);
		}
	}
}
