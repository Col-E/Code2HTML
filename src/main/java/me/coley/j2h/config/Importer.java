package me.coley.j2h.config;

import me.coley.j2h.config.model.Configuration;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Configuration importer.
 *
 * @author Geoff Hayward
 */
public final class Importer {
	private final static String DEFAULT_CONF = "default-config.j2h";

	/**
	 * @param path
	 * 		Path to configuration file.
	 *
	 * @return Configuration instance from file.
	 *
	 * @throws JAXBException
	 * 		Thrown if JAXBContext failed to initialize.<br>
	 * 		This typically means the file is formatted incorrectly.
	 * @throws IOException
	 * 		Thrown if the file could not be read from.
	 */
	public static Configuration importFromFile(String path) throws JAXBException, IOException {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		return (Configuration) um.unmarshal(new StringReader(readFile(path, UTF_8)));
	}

	/**
	 * @param text
	 * 		Raw text of the configuration
	 *
	 * @return Configuration instance from the text.
	 *
	 * @throws JAXBException
	 * 		Thrown if JAXBContext failed to initialize.<br>
	 * 		This typically means the file is formatted incorrectly.
	 */
	public static Configuration importFromText(String text) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		return (Configuration) um.unmarshal(new StringReader(text));
	}

	/**
	 * @return Configuration instance from default file.
	 *
	 * @throws JAXBException
	 * 		Thrown if JAXBContext failed to initialize.<br>
	 * 		This typically means the file is formatted incorrectly.
	 * @throws IOException
	 * 		Thrown if the file could not be read from.
	 */
	public static Configuration importDefault() throws JAXBException, IOException {
		ClassLoader classloader = Importer.class.getClassLoader();
		String uri = classloader.getResource(DEFAULT_CONF).toExternalForm();
		Path path = Paths.get(URI.create(uri));
		return importFromFile(path.toString());
	}

	/**
	 * @param path
	 * 		Path to file.
	 * @param encoding
	 * 		File encoding.
	 *
	 * @return Content of file.
	 *
	 * @throws IOException
	 * 		Thrown if the file could not be read from.
	 */
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
