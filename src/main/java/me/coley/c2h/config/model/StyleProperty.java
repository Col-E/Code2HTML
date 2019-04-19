package me.coley.c2h.config.model;

import lombok.*;

import javax.xml.bind.annotation.*;

/**
 * CSS properties to apply to some language's rule.
 *
 * @author Geoff Hayward
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleProperty {
	/**
	 * The target rule this style applies to.
	 */
	@XmlAttribute
	private String targetRule;
	/**
	 * The CSS property name.
	 */
	@XmlAttribute
	private String key;
	/**
	 * The CSS property value.
	 */
	@XmlAttribute
	private String value;
}
