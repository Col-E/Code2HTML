package me.coley.c2h.config;

import me.coley.c2h.config.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Configuration importer.
 *
 * @author Geoff Hayward
 */
public final class Importer {
	private static final Logger logger = LoggerFactory.getLogger(Importer.class);
	private final static String DEFAULT_CONF = "default-config.xml";

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
	public static Configuration importFromFile(Path path) throws JAXBException, IOException {
		logger.debug("Importing configuration from path: {}", path);
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Unmarshaller um = context.createUnmarshaller();
		return (Configuration) um.unmarshal(new StringReader(Files.readString(path)));
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
		logger.debug("Importing configuration from text");
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
		logger.debug("Importing configuration from default location");
		ClassLoader classloader = Importer.class.getClassLoader();
		URL resourceUrl = classloader.getResource(DEFAULT_CONF);
		if (resourceUrl == null) {
			logger.error("Could not load default config from path: {}", DEFAULT_CONF);
			throw new IOException("Resource not found: " + DEFAULT_CONF);
		}
		String uriPath = resourceUrl.toExternalForm();
		URI uri = URI.create(uriPath);
		switch (uri.getScheme()) {
			case "file" -> {
				Path path = Paths.get(uri);
				return importFromFile(path);
			}
			case "jar" -> {
				try (InputStream in = classloader.getResourceAsStream(DEFAULT_CONF)) {
					if (in == null) {
						throw new IOException("Resource not found: " + DEFAULT_CONF);
					}
					byte[] data = in.readAllBytes();
					return importFromText(new String(data, UTF_8));
				}
			}
		}
		throw new IOException("Default configuration location unknown");
	}
}
