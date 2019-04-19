package me.coley.c2h.loader;

import me.coley.c2h.config.Importer;
import me.coley.c2h.config.model.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ImporterTest {
	private Configuration configuration;

	@Before
	public void setup() {
		try {
			// Its bad practice to require disk resources in tests, but until somebody gets around
			// to mocking a model configuration, its good enough.
			// If it fails, good to catch it here anyways.
			configuration = Importer.importDefault();
		} catch(JAXBException e) {
			fail("setup: JAXBException" + e.getMessage());
		} catch(IOException e) {
			fail("setup: IOException" + e.getMessage());
		}
	}

	@Test
	public void testNonEmpty() {
		for(Language lang : configuration.getLanguages()) {
			// No nulls allows in language models
			assertNotNull(lang.getName());
			assertNotNull(lang.getRules());
			assertNotNull(lang.getThemes());
			// Some content exists
			assertTrue(!lang.getRules().isEmpty());
			assertTrue(!lang.getThemes().isEmpty());
		}
	}
}