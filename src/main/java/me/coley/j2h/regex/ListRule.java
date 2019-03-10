package me.coley.j2h.regex;

import java.util.Collection;

/**
 * Regex rule to match a collection of words.
 *
 * @author Matt
 */
public class ListRule extends RegexRule {
	public ListRule(String name, String[] items) {
		super(name, build(items));
	}

	public ListRule(String name, Collection<String> items) {
		super(name, build(items.toArray(new String[0])));
	}

	/**
	 * @param items
	 * 		Array of items to match.
	 *
	 * @return Build regex matching all items in the array.
	 */
	private static String build(String[] items) {
		return "\\b(" + String.join("|", items) + ")\\b";
	}
}
