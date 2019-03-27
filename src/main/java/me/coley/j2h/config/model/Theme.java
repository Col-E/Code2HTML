package me.coley.j2h.config.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
public class Theme {

    @XmlAttribute
    private String name;

    @XmlElement(name = "style")
    private List<StyleRule> styles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StyleRule> getStyles() {
        return styles;
    }

    public void setStyles(List<StyleRule> styles) {
        this.styles = styles;
    }

    public List<StyleRule> getStylesForTargetByName(String name) {
        return styles.stream()
                .filter(sr -> sr.getTargetRule().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }
}
