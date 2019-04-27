package me.coley.c2h.config;

import jregex.Matcher;
import jregex.Pattern;
import lombok.Getter;
import lombok.Setter;
import me.coley.c2h.Code2Html;
import me.coley.c2h.config.model.*;
import me.coley.c2h.util.Regex;

import java.util.List;
import java.util.Map;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

/**
 * Utility to make working with the configuration object easier.
 *
 * @author Matt
 */
public class ConfigHelper {
	@Getter
	@Setter
	private Configuration configuration;
	@Getter
	@Setter
	private Language language;
	@Getter
	@Setter
	private Theme theme;

	public ConfigHelper(Configuration configuration, Language language, Theme theme) {
		if(configuration == null)
			throw new IllegalStateException("Configuration cannot be null");
		if(language == null)
			throw new IllegalStateException("Language cannot be null");
		if(theme == null)
			throw new IllegalStateException("Theme cannot be null");
		this.configuration = configuration;
		this.language = language;
		this.theme = theme;
	}

	/**
	 * Append regex pattern.
	 *
	 * @param name
	 * 		Name of the pattern.
	 * @param regex
	 * 		Pattern string.
	 */
	public void addRule(String name, String regex) {
		Rule rule = new Rule();
		rule.setName(name);
		rule.setPattern(regex);
		addRule(rule);
	}

	/**
	 * Append regex pattern.
	 *
	 * @param rule
	 * 		Pattern object.
	 */
	public void addRule(Rule rule) {
		language.addRule(rule);
	}

	/**
	 * @param text
	 * 		Text to convert into HTML.
	 *
	 * @param inline
	 * @return HTML of the text with matched attributes of the {@link #language current language}.
	 */
	public String convert(String text, boolean inline) {
		Matcher matcher = getPattern().matcher(text);
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;
		while(matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			// append text not matched
			if(start > lastEnd) {
				String unmatched = escapeHtml4(text.substring(lastEnd, start));
				sb.append(unmatched);
			}
			// append match
			String matched = escapeHtml4(text.substring(start, end));
			if (inline) {
				String styleRules = getInlineStyleFromGroup(matcher);
				sb.append("<span style=\"" + styleRules + "\">" + matched + "</span>");
			} else {
				String styleClass = getClassFromGroup(matcher);
				sb.append("<span class=\"" + styleClass + "\">" + matched + "</span>");
			}
			lastEnd = end;
		}
		// Append ending text not matched
		sb.append(escapeHtml4(text.substring(lastEnd)));
		// Apply line formatting to each line
		StringBuilder fmt = new StringBuilder();
		if(inline) {
			StringBuilder sbLineStyle = new StringBuilder();
			StringBuilder sbLinePreStyle = new StringBuilder();
			Regex.getCssProperties(Code2Html.BASE_CSS, "pre .line").forEach((key, value) -> {
				sbLineStyle.append(key + ":" + value + ";");
			});
			Regex.getCssProperties(Code2Html.BASE_CSS, "pre .line::before").forEach((key, value) -> {
				sbLinePreStyle.append(key + ":" + value + ";");
			});
			int lineNum = 1;
			for(String line : sb.toString().split("\n"))
				fmt.append("<span style=\"" +sbLineStyle.toString() + "\"><span style=\"" + sbLinePreStyle.toString() + "\">" + (lineNum++) + "</span></span>" + line + "\n");
			// Wrap in pre tags and slap it in an HTML page
			StringBuilder sbPreStyle = new StringBuilder();
			Regex.getCssProperties(Code2Html.BASE_CSS, "pre").forEach((key, value) -> {
				sbPreStyle.append(key + ":" + value + ";");
			});
			return "<pre style=\"" + sbPreStyle.toString() + "\">" + fmt.toString() + "</pre>";
		} else {
			for(String line : sb.toString().split("\n"))
				fmt.append("<span class=\"line\"></span>" + line + "\n");
			// Wrap in pre tags and slap it in an HTML page
			return "<pre>" + fmt.toString() + "</pre>";
		}
	}

	/**
	 * @return Compiled regex pattern from {@link #getRules() all existing rules}.
	 */
	public Pattern getPattern() {
		if (getRules().isEmpty()) {
			return new Pattern("({EMPTY}EMPTY)");
		}
		StringBuilder sb = new StringBuilder();
		for(Rule rule : getRules())
			sb.append("({" + rule.getPatternGroupName() + "}" + rule.getPattern() + ")|");
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
		for(Rule rule : getRules()) {
			sb.append("." + rule.getName() + " {");
			theme.getStylesForRule(rule.getName()).forEach(style -> {
				sb.append("\n\t" + style.getKey() + ": " + style.getValue() + ";");
			});
			sb.append("\n}\n");
		}
		return sb.toString();
	}

	/**
	 * @return List of added rules.
	 */
	public List<Rule> getRules() {
		return language.getRules();
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
		for(Rule rule : getRules())
			if(matcher.group(rule.getPatternGroupName()) != null)
				return rule.getName();
		return null;
	}

	/**
	 * Fetch the CSS style to be used by the matched group.
	 *
	 * @param matcher
	 * 		Matcher that has found a group.
	 *
	 * @return CSS inline style.
	 */
	public String getInlineStyleFromGroup(Matcher matcher) {
		for(Rule rule : getRules())
			if(matcher.group(rule.getPatternGroupName()) != null) {
				StringBuilder sbStyle = new StringBuilder();
				theme.getStylesForRule(rule.getName())
						.forEach(style ->
								sbStyle.append(style.getKey() + ":" + style.getValue() + ";"));
				return sbStyle.toString();
			}
		return "";
	}
}
