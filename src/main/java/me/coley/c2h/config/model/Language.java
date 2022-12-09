package me.coley.c2h.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of rules that match against a language's feature set and themes to apply distinct
 * styles to each of these rules.
 *
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Language implements Comparable<Language> {
	@XmlElement(name = "rule")
	private final List<Rule> rules = new ArrayList<>();
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
	 * @param name
	 * 		Name of rule to find.
	 *
	 * @return Rule by name, or {@code null} when there is no match.
	 */
	public Rule getRule(String name) {
		return rules.stream()
				.filter(rule -> rule.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * @return Rules for matching against language features.
	 */
	public List<Rule> getRules() {
		return rules;
	}

	/**
	 * @return Language identifier.
	 */
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(Language other) {
		return getName().toLowerCase().compareTo(other.getName().toLowerCase());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Language language = (Language) o;

		if (!Objects.equals(rules, language.rules)) return false;
		return Objects.equals(name, language.name);
	}

	@Override
	public int hashCode() {
		int result = rules.hashCode();
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
