package me.coley.c2h.util;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for miscellaneous regex actions.
 *
 * @author Matt Coley
 */
public class Regex {
	private static final Map<String, Pattern> patternCache = new HashMap<>();

	/**
	 * @param cssText
	 * 		Text containing CSS to match.
	 * @param cssRule
	 * 		Rule to extract properties from.
	 *
	 * @return Map of property names to values.
	 */
	public static Map<String, String> getCssProperties(String cssText, String cssRule) {
		Map<String, String> map = new HashMap<>();
		String regex = "((?<TITLE>" + cssRule + ")\\s*\\{(?<BODY>[^}]*))\\}";
		Pattern pattern = patternOf(regex);
		Matcher matcher = pattern.matcher(cssText);
		if (!matcher.find()) {
			return map;
		}
		String body = matcher.group("BODY");

		// Parse the body of the css class and update the regex-rule style map.
		regex = "(?<key>\\S+):\\s*(?<value>.+)(?=;)";
		pattern = patternOf(regex);
		matcher = pattern.matcher(body);
		while (matcher.find()) {
			String key = matcher.group("key");
			String value = matcher.group("value");
			map.put(key, value);
		}
		return map;
	}

	/**
	 * @param text
	 * 		Input text to search in.
	 * @param regex
	 * 		Pattern to search with.
	 *
	 * @return The number of matches in the input text.
	 */
	public static int countMatches(String text, String regex) {
		int count = 0;
		Matcher matcher = patternOf(regex).matcher(text);
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * @param regex
	 * 		Regex to update
	 * @param increment
	 * 		Value to increment with.
	 *
	 * @return Updated regex with new recursive constructs / back-references.
	 */
	public static String incrementRecursiveConstructs(String regex, int increment) {
		Matcher matcher = patternOf("\\([?\\\\]\\d\\)").matcher(regex);
		while (matcher.find()) {
			int value = extractInt(matcher.group(0));
			regex = regex
					.replace("(?" + value + ")", "(?" + (value + increment) + ")")
					.replace("(\\" + value + ")", "(\\" + (value + increment) + ")");
		}
		return regex;
	}

	/**
	 * Only used in {@link #incrementRecursiveConstructs(String, int)} so input is assumed to be {@code (.N.)} where
	 * N is one or more consecutive numeric characters. Surrounding text discarded.
	 *
	 * @param text
	 * 		Input text containing a single integer.
	 *
	 * @return Int extracted.
	 */
	private static int extractInt(String text) {
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) if (c >= '0' && c <= '9') sb.append(c);
		return Integer.parseInt(sb.toString());
	}

	/**
	 * @param pattern
	 * 		Pattern text.
	 *
	 * @return Cached compiled pattern.
	 */
	public static Pattern patternOf(String pattern) {
		return patternCache.computeIfAbsent(pattern, Pattern::compile);
	}

	/**
	 * @param pattern
	 * 		Pattern text.
	 *
	 * @return {@code true} when valid.
	 */
	public static boolean isValid(String pattern) {
		try {
			Pattern.compile(pattern);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
}
