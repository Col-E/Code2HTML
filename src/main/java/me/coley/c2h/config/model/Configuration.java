package me.coley.c2h.config.model;

import lombok.Getter;

import javax.xml.bind.annotation.*;
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
	@Getter
	@XmlElement(name = "language")
	private List<Language> languages = new ArrayList<>();

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
