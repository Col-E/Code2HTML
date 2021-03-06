package me.coley.c2h.config.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of rules that match against a language's feature set and themes to apply distinct
 * styles to each of these rules.
 *
 * @author Geoff Hayward
 */
@EqualsAndHashCode
@XmlAccessorType(XmlAccessType.FIELD)
public class Language implements Comparable<Language> {
	/**
	 * Rules for matching against language features.
	 */
	@Getter
	@XmlElement(name = "rule")
	private List<Rule> rules = new ArrayList<>();
	/**
	 * Theme to apply to language. Determines which set of CSS styles are applied to matches.
	 */
	@Getter
	@XmlElementWrapper(name = "themes")
	@XmlElement(name = "theme")
	private List<Theme> themes = new ArrayList<>();
	/**
	 * Language identifier.
	 */
	@Getter
	@XmlAttribute
	private String name;

	public Language(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	public Language() {
		// For JAXB instantiation
	}

	/**
	 * Add a rule to the language.
	 *
	 * @param rule
	 * 		Rule to add.
	 */
	public void addRule(Rule rule) {
		this.rules.add(rule);
	}

	/**
	 * Add a theme to the language.
	 *
	 * @param theme
	 * 		Theme to add.
	 */
	public void addTheme(Theme theme) {
		this.themes.add(theme);
	}

	/**
	 * @param name
	 * 		Theme identifier.
	 *
	 * @return Theme matching the given name.
	 */
	public Theme findTheme(String name) {
		return themes.stream()
				.filter(t -> t.getName().equalsIgnoreCase(name))
				.findFirst().orElse(null);
	}

	@Override
	public int compareTo(Language other) {
		return getName().toLowerCase().compareTo(other.getName().toLowerCase());
	}
}
