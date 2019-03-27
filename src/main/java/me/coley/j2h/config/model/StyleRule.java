package me.coley.j2h.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class StyleRule {

    @XmlAttribute
    private  String targetRule;
    @XmlAttribute
    private  String key;
    @XmlAttribute
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTargetRule(String targetRule){
        this.targetRule = targetRule;
    }

    public String getTargetRule() {
        return targetRule;
    }
}
