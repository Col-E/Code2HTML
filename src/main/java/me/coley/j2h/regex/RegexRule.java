package me.coley.j2h.regex;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for grouping a regex pattern with an identifier.
 *
 * @author Matt
 */
public class RegexRule {
	private final String pattern;
	private final String group, raw;
	private final Map<String, String> style = new HashMap<>();

	public RegexRule(String name, String pattern) {
		this.raw = name;
		this.group = sterilize(name);
		this.pattern = pattern;
	}

	/**
	 * @return Style properties.
	 */
	public Map<String, String> getStyle() {
		return style;
	}

	/**
	 * @return Name as proper regex group title.
	 */
	public String getGroupName() {
		return group;
	}

	/**
	 * @return Name as original specification.
	 */
	public String getRawName() {
		return raw;
	}

	public String getPattern() {
		return pattern;
	}

	/**
	 * @param name
	 * 		Original name.
	 *
	 * @return Name stripped of invalid identifier characters. Allows the name to be used as a
	 * regex group name.
	 */
	private static String sterilize(String name) {
		return name.replaceAll("[\\W\\d]+", "").toUpperCase();
	}
}
