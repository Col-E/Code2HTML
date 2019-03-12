package me.coley.j2h.modle;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Language {

    @XmlElement(name = "rule")
    private List<Rule> rules = new ArrayList<>();

    @XmlElementWrapper(name = "themes")
    @XmlElement(name = "theme")
    private List<Theme> themes = new ArrayList<>();

    @XmlAttribute
    private String name;

    public Language(String name){
        this.name = name;
    }

    public Language(){
        /* Just for JAXB */
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule){
        this.rules.add(rule);
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void addTheme(Theme theme){
        this.themes.add(theme);
    }

    public String getName(){
        return name;
    }
}
