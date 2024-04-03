package me.coley.c2h.util;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;
import me.coley.c2h.config.model.Language;
import me.coley.c2h.config.model.Rule;
import me.coley.c2h.ui.pane.OutputPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

/**
 * Tool that takes in some text, breaks it down based on matched regions from a {@link Language#getRules()}
 * and any {@link Rule#getSubrules()}, then takes the broken down regions and builds HTML text from them.
 *
 * @author Matt Coley
 */
public class LanguageMatcher {
	private static final Logger logger = LoggerFactory.getLogger(LanguageMatcher.class);
	private static final Map<List<Rule>, Pattern> patternCache = new HashMap<>();
	private final Language language;

	/**
	 * @param language
	 * 		Language to match against.
	 */
	public LanguageMatcher(Language language) {
		if (language == null) {
			throw new IllegalStateException("Language cannot be null");
		}
		this.language = language;
	}

	/**
	 * @param text
	 * 		Text to convert into HTML.
	 * @param inline
	 * 		Flag to enable inline CSS generation.
	 *
	 * @return HTML of the text with matched attributes of the {@link #language current language}.
	 */
	public String convert(String text, boolean inline) {
		long start = System.currentTimeMillis();
		logger.trace("Running language to HTML conversion for {}", language.getName());
		Region root = new Region(text);
		logger.trace("Splitting input text into blocks");
		root.split(language.getRules());
		logger.trace("Collecting blocks input HTML text");
		String html = root.toHtml(inline);
		logger.trace("Completed conversion in {}ms", (System.currentTimeMillis() - start));
		return html;
	}

	/**
	 * @return CSS for language.
	 */
	public String createPatternCSS() {
		return "/* =========================== */\n" +
				"/*    Custom element types     */\n" +
				"/* =========================== */\n" +
				createPatternCSS("", language.getRules());
	}

	/**
	 * @param parent
	 * 		Parent class name / context.
	 * @param rules
	 * 		Rules to create CSS classes for.
	 *
	 * @return CSS for given rule list.
	 */
	private static String createPatternCSS(String parent, List<Rule> rules) {
		StringBuilder sb = new StringBuilder();
		for (Rule rule : rules) {
			String cssName = "." + rule.getName();
			sb.append(parent).append(cssName).append(" {");
			rule.getStyle().forEach(style ->
					sb.append("\n\t").append(style.getKey()).append(": ")
							.append(style.getValue()).append(";"));
			sb.append("\n}\n");
			// Add sub-rules
			if (!rule.getSubrules().isEmpty()) sb.append(createPatternCSS(cssName + " ", rule.getSubrules()));
		}
		return sb.toString();
	}

	private static class Region {
		private final List<Region> children = new ArrayList<>();
		private final String text;
		private final Rule rule;
		private final int start;
		private final int end;

		/**
		 * Constructor for root region.
		 *
		 * @param text
		 * 		Complete text.
		 */
		private Region(String text) {
			this.text = text;
			rule = null;
			start = 0;
			end = text.length();
		}

		/**
		 * Constructor for matching a rule.
		 *
		 * @param text
		 * 		Complete text.
		 * @param rule
		 * 		Rule associated with region.
		 * @param start
		 * 		Start offset in complete text.
		 * @param end
		 * 		End offset in complete text.
		 */
		private Region(String text, Rule rule, int start, int end) {
			this.text = text;
			this.rule = rule;
			this.start = start;
			this.end = end;
		}

		/**
		 * Splits this node into subregions based on the given rules.
		 *
		 * @param rules
		 * 		Rules to match and split by.
		 */
		public void split(List<Rule> rules) {
			// Match within give region
			String localText = text.substring(start, end);
			Matcher matcher = getCombinedPattern(rules).matcher(localText);
			while (matcher.find()) {
				// Create region from found match
				int localStart = matcher.start();
				int localEnd = matcher.end();
				Rule matchedRule = getRuleFromMatcher(rules, matcher);
				logger.trace("Block range found [{} - {}] for rule '{}'", localStart, localEnd, matchedRule.getName());
				Region localChild = new Region(text, matchedRule, start + localStart, start + localEnd);

				// Break the new region into smaller ones if the rule associated with the match has sub-rules.
				List<Rule> subrules = matchedRule.getSubrules();
				if (!subrules.isEmpty()) localChild.split(subrules);

				// Add child (splitting technically handled in HTML output logic)
				children.add(localChild);
			}
		}

		/**
		 * @param inline
		 *        {@code true} to emit CSS inline with created {@code <span>} elements.
		 *
		 * @return Styled HTML by spans matching the {@link Language#getRules() language rules} break-down.
		 */
		public String toHtml(boolean inline) {
			StringBuilder sb = new StringBuilder();
			int lastEnd = start;
			if (rule == null) {
				logger.trace("Writing root region to HTML");

				// Add children
				for (Region child : children) {
					int childStart = child.start;

					// Append text not matched at start
					if (childStart > lastEnd) {
						String unmatched = escapeHtml4(text.substring(lastEnd, childStart));
						sb.append(unmatched);
					}

					// Append child content
					sb.append(child.toHtml(inline));
					lastEnd = child.end;
				}
				// Append remaining unmatched text, then end preformatted block
				sb.append(escapeHtml4(text.substring(lastEnd)));

				// Wrap in root <pre>
				StringBuilder fmt = new StringBuilder();
				if (inline) {
					StringBuilder sbLineStyle = new StringBuilder();
					StringBuilder sbLinePreStyle = new StringBuilder();
					Regex.getCssProperties(OutputPane.BASE_CSS, "pre .line").forEach((key, value) ->
							sbLineStyle.append(key).append(":").append(value).append(";"));
					Regex.getCssProperties(OutputPane.BASE_CSS, "pre .line::before").forEach((key, value) ->
							sbLinePreStyle.append(key).append(":").append(value).append(";"));
					int lineNum = 1;
					for (String line : sb.toString().split("\n")) {
						fmt.append("<span style=\"").append(sbLineStyle)
								.append("\"><span style=\"").append(sbLinePreStyle).append("\">").append(lineNum++)
								.append("</span></span>").append(line).append("\n");
					}

					// Wrap in pre tags and slap it in an HTML page
					StringBuilder sbPreStyle = new StringBuilder();
					Regex.getCssProperties(OutputPane.BASE_CSS, "pre").forEach((key, value) ->
							sbPreStyle.append(key).append(":").append(value).append(";"));
					return "<pre style=\"" + sbPreStyle + "\">" + fmt + "</pre>";
				} else {
					fmt.append("<pre>");
					for (String line : sb.toString().split("\n")) {
						fmt.append("<span class=\"line\"></span>").append(line).append("\n");
					}
					return fmt.append("</pre>").toString();
				}
			} else {
				logger.trace("Writing region[{} - {}] to HTML", start, end);
				if (inline) {
					String styleRules = getInlineStyleFromRule(rule);
					sb.append("<span style=\"").append(styleRules).append("\">");
				} else {
					String styleClass = rule.getName();
					sb.append("<span class=\"").append(styleClass).append("\">");
				}

				// Add children
				for (Region child : children) {
					int childStart = child.start;

					// Append text not matched at start
					if (childStart > lastEnd) {
						String unmatched = escapeHtml4(text.substring(lastEnd, childStart));
						sb.append(unmatched);
					}

					// Append child content
					sb.append(child.toHtml(inline));
					lastEnd = child.end;
				}
				// Append remaining unmatched text, then end span block
				sb.append(escapeHtml4(text.substring(lastEnd, end)));
				sb.append("</span>");
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			if (rule == null) return "ROOT";
			return rule.getName() + " {\n" +
					text.substring(start, end) +
					"\n}";
		}

		/**
		 * @param rules
		 * 		Rules to search for.
		 * @param matcher
		 * 		Matcher to pull rule name from using matched group's name.
		 *
		 * @return Rule with matching name as matched group.
		 */
		private static Rule getRuleFromMatcher(Collection<Rule> rules, Matcher matcher) {
			return rules.stream()
					.filter(rule -> matcher.group(rule.getPatternGroupName()) != null)
					.findFirst()
					.orElse(null);
		}

		/**
		 * @param rule
		 * 		Rule to pull style from.
		 *
		 * @return Inline CSS for the rule.
		 */
		private static String getInlineStyleFromRule(Rule rule) {
			if (rule != null) {
				StringBuilder sbStyle = new StringBuilder();
				rule.getStyle().forEach(style ->
						sbStyle.append(style.getKey()).append(":").append(style.getValue()).append(";"));
				return sbStyle.toString();
			}
			return "";
		}

		/**
		 * @param rules
		 * 		Rules to match against.
		 *
		 * @return Compiled regex pattern from the given rules.
		 */
		private static Pattern getCombinedPattern(List<Rule> rules) {
			// Cache results. Very likely to encounter rule-collection again which makes it wise to save
			// the result instead of re-computing every time.
			return patternCache.computeIfAbsent(rules, Region::createCombinedPattern);
		}

		/**
		 * @param rules
		 * 		Rules to match against.
		 *
		 * @return Compiled regex pattern from the given rules.
		 */
		private static Pattern createCombinedPattern(List<Rule> rules) {
			// Dummy pattern
			if (rules.isEmpty()) return Regex.patternOf("(?<EMPTY>EMPTY)");

			// (?N) and (\N) in regex when wrapped in a group increment to ((?N+1)) and ((\N+1))
			// Since these are all bundled in named group our start offset is going to be 1.
			// Any group that isn't non-matching '(?:foo)' should also increment the offset.
			int relativeGroup = 1;
			StringBuilder sb = new StringBuilder();
			logger.trace("Creating pattern for {} rules", rules.size());
			for (Rule rule : rules) {
				logger.trace(" - Appending: " + rule.getName());

				// Update (?N) and (\N) values since we are merging patterns into a single combined one
				// - ?N - Subroutine matches expression defined in Nth capture group
				// - \N - Back-reference matches same text matched by Nth capture group
				String pattern = Regex.incrementRecursiveConstructs(rule.getPattern(), relativeGroup);
				logger.trace("  - Incremented recursions: " + rule.getName());

				// Append to builder
				sb.append("(?<").append(rule.getPatternGroupName()).append(">").append(pattern).append(")|");

				// Increment count
				int localGroups = Regex.countMatches(pattern, "\\((?!\\?:)[^)(]*+(?:[^)(])*+\\)");
				logger.trace("  - Counted matches: " + rule.getName());
				relativeGroup += 1 + localGroups;
			}
			return Regex.patternOf(sb.substring(0, sb.length() - 1));
		}
	}
}
