package me.coley.j2h.regex;

import jregex.Matcher;
import jregex.Pattern;

import java.util.*;

/**
 * Merge rules into patterns instances.
 *
 * @author Matt
 */
public class PatternHelper {
	private final List<RegexRule> rules = new ArrayList<>();

	/**
	 * Append regex pattern.
	 *
	 * @param name
	 * 		Name of the pattern.
	 * @param regex
	 * 		Pattern string.
	 */
	public void addRule(String name, String regex) {
		rules.add(new RegexRule(name, regex));
	}

	/**
	 * Append regex pattern.
	 *
	 * @param rule
	 * 		Pattern object.
	 */
	public void addRule(RegexRule rule) {
		rules.add(rule);
	}

	/**
	 * Append regex patterns.
	 *
	 * @param ruleArray
	 * 		Patterns.
	 */
	public void addRules(RegexRule... ruleArray) {
		addRules(Arrays.asList(ruleArray));
	}

	/**
	 * Append regex patterns.
	 *
	 * @param ruleCollection
	 * 		Patterns.
	 */
	public void addRules(Collection<RegexRule> ruleCollection) {
		rules.addAll(ruleCollection);
	}

	/**
	 * @return Compiled regex pattern from {@link #getRules() all existing rules}.
	 */
	public Pattern getPattern() {
		StringBuilder sb = new StringBuilder();
		for(RegexRule rule : rules)
			sb.append("({" + rule.getGroupName() + "}" + rule.getPattern() + ")|");
		return new Pattern(sb.substring(0, sb.length() - 1));
	}

	/**
	 * @return CSS for patterns.
	 */
	public String getPatternCSS() {
		StringBuilder sb = new StringBuilder();
		sb.append("/* =========================== */\n");
		sb.append("/*    Custom element types     */\n");
		sb.append("/* =========================== */\n");
		for(RegexRule rule : rules) {
			sb.append("." + rule.getRawName() + " {");
			for(Map.Entry<String, String> kv : rule.getStyle().entrySet())
				sb.append("\n\t" + kv.getKey() + ": " + kv.getValue() + ";");
			sb.append("\n}\n");
		}
		return sb.toString();
	}

	/**
	 * @return List of added rules.
	 */
	public List<RegexRule> getRules() {
		return rules;
	}

	/**
	 * Fetch the CSS class name to use based on the matched group.
	 *
	 * @param matcher
	 * 		Matcher that has found a group.
	 *
	 * @return CSS class name <i>(Raw name of regex rule)</i>
	 */
	public String getClassFromGroup(Matcher matcher) {
		for(RegexRule rule : rules)
			if(matcher.group(rule.getGroupName()) != null)
				return rule.getRawName();
		return null;
	}
}
