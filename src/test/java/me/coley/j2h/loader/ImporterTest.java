package me.coley.j2h.loader;

import me.coley.j2h.config.Importer;
import me.coley.j2h.config.model.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class ImporterTest {

    // Test not intended as a Unit test - hits the disk.
    public static void main(String[] args) throws JAXBException, IOException {
        Configuration configuration = Importer.importDefaultConfiguration();
        for(Language lang : configuration.getLanguages()){
            System.out.println("Language: " + lang.getName());
            System.out.println("Rules:");
            for(Rule searchRule : lang.getRules()){
                System.out.println("- " + searchRule.getName() + ": " + searchRule.getPattern());
            }
            for(Theme theme : lang.getThemes()){
                System.out.println("Theme: " + theme.getName());
                for(StyleRule sr : theme.getStyles())
                    System.out.println("- " + sr.getKey() + ": " + sr.getValue());
            }
        }
    }

}