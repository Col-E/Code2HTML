package me.coley.c2h.config.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Language rule that matches against a feature of a language using regex.
 *
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule {
	@XmlAttribute(required = true)
	private String name;
	@XmlElement(required = true)
	private String pattern;
	@XmlElementWrapper(name = "style")
	@XmlElement(name = "entry")
	private List<StyleProperty> style = new ArrayList<>();
	@XmlElement(name = "rule")
	@XmlElementWrapper(name = "subrules")
	private List<Rule> subrules = new ArrayList<>();

	/**
	 * @return Name as proper regex group title.
	 */
	public String getPatternGroupName() {
		return sterilize(name);
	}

	/**
	 * @return Name literal / rule identifier.
	 *
	 * @see #getPatternGroupName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * 		Name literal / rule identifier.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Regex pattern string.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern
	 * 		Regex pattern string.
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return List of style properties to apply to matched content.
	 */
	public List<StyleProperty> getStyle() {
		return style;
	}

	/**
	 * @param style
	 * 		List of style properties to apply to matched content.
	 */
	public void setStyle(List<StyleProperty> style) {
		this.style = style;
	}

	/**
	 * @param property
	 * 		Style property to remove.
	 */
	public void removeStyle(StyleProperty property) {
		style.remove(property);
	}

	/**
	 * @param property
	 * 		Style property to add.
	 */
	public void addStyle(StyleProperty property) {
		// Remove prior entry if same 'key'
		style.removeIf(p -> p.getKey().equals(property.getKey()));
		style.add(property);
	}

	/**
	 * @return Rules to apply within this rule's matched bounds.
	 */
	public List<Rule> getSubrules() {
		return subrules;
	}

	/**
	 * @param subrules
	 * 		Rules to apply within this rule's matched bounds.
	 */
	public void setSubrules(List<Rule> subrules) {
		this.subrules = subrules;
	}

	@Override
	public String toString() {
		return getName() + ": " + getPattern();
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
