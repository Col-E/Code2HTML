package me.coley.c2h.config.model;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * CSS properties to apply to some language's rule.
 *
 * @author Geoff Hayward
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleProperty {
	@XmlAttribute(required = true)
	private String key;
	@XmlAttribute
	private String value;
	@XmlElementWrapper(name = "style")
	@XmlElement(name = "entry")
	private List<StyleProperty> style;

	public StyleProperty() {}

	/**
	 * @param key
	 * 		CSS property key.
	 * @param value
	 * 		CSS property value.
	 */
	public StyleProperty(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return CSS property key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 * 		CSS property key.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return CSS property value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 * 		CSS property value.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StyleProperty property = (StyleProperty) o;

		if (!Objects.equals(key, property.key)) return false;
		return Objects.equals(value, property.value);
	}

	@Override
	public int hashCode() {
		int result = (key != null ? key.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "StyleProperty{" +
				"key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
