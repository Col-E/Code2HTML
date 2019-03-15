package me.coley.j2h.loader;

import me.coley.j2h.modle.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Importer {

    public static Configuration importConfiguration(String path) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Configuration.class);
        Unmarshaller um = context.createUnmarshaller();
        return (Configuration) um.unmarshal(new StringReader(readFile(path, StandardCharsets.UTF_8)));
    }

    public static Configuration importDefaultConfiguration() throws JAXBException, IOException {
        return importConfiguration(Importer.class.getClassLoader().getResource("default-config.j2h").getPath());
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
