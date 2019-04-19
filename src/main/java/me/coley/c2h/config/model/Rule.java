package me.coley.c2h.config.model;

import lombok.*;

import javax.xml.bind.annotation.*;

/**
 * Language rule that matches against a feature of a language using regex.
 *
 * @author Geoff Hayward
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule {
	/**
	 * The rule identifier.
	 */
	@XmlAttribute
	private String name;
	/**
	 * The regex pattern.
	 */
	@XmlElement
	private String pattern;

	/**
	 * @return Name as proper regex group title.
	 */
	public String getPatternGroupName() {
		return sterilize(name);
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
