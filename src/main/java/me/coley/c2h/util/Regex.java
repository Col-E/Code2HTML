package me.coley.c2h.util;

import jregex.Matcher;
import jregex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for miscellaneous regex actions.
 *
 * @author Matt
 */
public class Regex {
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
		String regex = "(({TITLE}" + cssRule + ")\\s*\\{({BODY}[^}]*))\\}";
		Pattern pattern = new Pattern(regex);
		Matcher matcher = pattern.matcher(cssText);
		if(!matcher.find())
			return map;
		String body = matcher.group("BODY");
		// Parse the body of the css class and update the regex-rule style map.
		regex = "({key}\\S+):\\s*({value}.+)(?=;)";
		pattern = new Pattern(regex);
		matcher = pattern.matcher(body);
		while(matcher.find()) {
			String key = matcher.group("key");
			String value = matcher.group("value");
			map.put(key, value);
		}
		return map;
	}
}
