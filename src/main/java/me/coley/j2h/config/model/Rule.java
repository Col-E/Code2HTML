package me.coley.j2h.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule {

	@XmlAttribute
	private String name;
	@XmlElement
	private String pattern;

	public String getName() {
		return name;
	}

	public String getPattern() {
		return pattern;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
