package me.coley.j2h.config;

import me.coley.j2h.config.model.Configuration;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Configuration exporter.
 *
 * @author Geoff Hayward
 * @author Matt
 */
public class Exporter {
	public static String toString(Configuration configuration) throws JAXBException {
		// Create marshaller
		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// Export config to outputstream and return the toString value.
		OutputStream output = new OutputStream() {
			private final StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
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
