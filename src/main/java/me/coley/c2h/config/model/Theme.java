package me.coley.c2h.config.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection of styles that will be applied to a language's rule-set.
 *
 * @author Geoff Hayward
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Theme {
	/**
	 * Name of the theme.
	 */
	@XmlAttribute
	private String name;
	/**
	 * Styles for the rules of the theme.
	 */
	@XmlElement(name = "style")
	private List<StyleProperty> styles = new ArrayList<>();


	/**
	 * @param rule
	 * 		Target rule name.
	 *
	 * @return List of style properties that are associated with the given rule.
	 */
	public List<StyleProperty> getStylesForRule(String rule) {
		return styles.stream()
				.filter(sr -> sr.getTargetRule().equals(rule))
				.collect(Collectors.toList());
	}

	/**
	 * Update a style property belonging to the given rule.
	 *
	 * @param rule
	 * 		Target rule name.
	 * @param key
	 * 		Style property key.
	 * @param value
	 * 		Style property value to set.
	 */
	public void update(String rule, String key, String value) {
		Collection<StyleProperty> matched = styles.stream()
				.filter(sr -> sr.getTargetRule().equals(rule) &&
						sr.getKey().equals(key))
				.collect(Collectors.toList());
		if(matched.isEmpty()) {
			StyleProperty property = new StyleProperty();
			property.setTargetRule(rule);
			property.setKey(key);
			property.setValue(value);
			styles.add(property);
		} else {
			matched.forEach(property -> property.setValue(value));
		}
	}
}
