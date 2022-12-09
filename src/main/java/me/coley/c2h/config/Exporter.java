package me.coley.c2h.config;

import me.coley.c2h.config.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.OutputStream;

/**
 * Configuration exporter.
 *
 * @author Geoff Hayward
 * @author Matt Coley
 */
public class Exporter {
	private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

	/**
	 * @param configuration
	 * 		Instance to export.
	 *
	 * @return XML representing the configuration.
	 *
	 * @throws JAXBException
	 * 		When the conversion fails.
	 */
	public static String toXML(Configuration configuration) throws JAXBException {
		logger.debug("Exporting Configuration to XML: {}", configuration);
		// Create marshaller
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// Export config to OutputStream and return the toString() value.
		OutputStream output = new OutputStream() {
			private final StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) {
				this.string.append((char) b);
			}

			@Override
			public String toString() {
				return this.string.toString();
			}
		};
		m.marshal(configuration, output);
		return output.toString();
	}
}
