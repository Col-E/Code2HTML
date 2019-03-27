package me.coley.j2h.config.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;

/**
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleProperty {
	/**
	 * The target rule this style applies to.
	 */
	@Getter
	@Setter
	@XmlAttribute
	private String targetRule;
	/**
	 * The CSS property name.
	 */
	@Getter
	@Setter
	@XmlAttribute
	private String key;
	/**
	 * The CSS property value.
	 */
	@Getter
	@Setter
	@XmlAttribute
	private String value;
}
