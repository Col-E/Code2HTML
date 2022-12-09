package me.coley.c2h.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for managing multiple languages.
 *
 * @author Geoff Hayward
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
	@XmlElement(name = "language")
	private final List<Language> languages = new ArrayList<>();

	/**
	 * @return Languages supported in the config instance.
	 */
	public List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Add a language to the config.
	 *
	 * @param language
	 * 		Language to add to the config.
	 */
	public void addLanguage(Language language) {
		languages.add(language);
	}

	/**
	 * @param name
	 * 		Language identifier.
	 *
	 * @return Language matching the given name.
	 */
	public Language findLanguage(String name) {
		return languages.stream()
				.filter(l -> l.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}
}
