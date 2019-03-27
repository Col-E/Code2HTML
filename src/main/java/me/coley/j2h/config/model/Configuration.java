package me.coley.j2h.config.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoff Hayward
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

	@XmlElement(name = "language")
	private List<Language> languages = new ArrayList<>();

	public List<Language> getLanguages() {
		return new ArrayList<>(languages);
	}

	public void addLanguage(Language language) {
		languages.add(language);
	}

	public Language findLanguageByNames(String name) {
		return languages.stream().filter(l -> l.getName().equalsIgnoreCase(name)).findFirst()
                .orElse(null);
	}
}
