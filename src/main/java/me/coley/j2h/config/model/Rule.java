package me.coley.j2h.config.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

/**
 * Language rule that matches against a feature of a language using regex.
 *
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule {
	/**
	 * The rule identifier.
	 */
	@Getter
	@Setter
	@XmlAttribute
	private String name;
	/**
	 * The regex pattern.
	 */
	@Getter
	@Setter
	@XmlElement
	private String pattern;
}
