package me.coley.j2h.config.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Theme {
	/**
	 * Name of the theme.
	 */
	@Getter
	@Setter
	@XmlAttribute
	private String name;
	/**
	 * Styles for the rules of the theme.
	 */
	@Getter
	@Setter
	@XmlElement(name = "style")
	private List<StyleProperty> styles = new ArrayList<>();

	/**
	 * @param name
	 * 		Target rule name.
	 *
	 * @return List of matching style properties.
	 */
	public List<StyleProperty> findStylesByTargetRule(String name) {
		return styles.stream().filter(sr -> sr.getTargetRule().equalsIgnoreCase(name)).collect
				(Collectors.toList());
	}
}
