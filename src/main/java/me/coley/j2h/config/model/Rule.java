package me.coley.j2h.config.model;

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
}
