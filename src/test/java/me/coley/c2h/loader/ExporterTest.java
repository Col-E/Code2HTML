package me.coley.c2h.loader;

import me.coley.c2h.config.Exporter;
import me.coley.c2h.config.Importer;
import me.coley.c2h.config.model.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class ExporterTest
{
    private Configuration configuration;

    @BeforeEach
    public void setup()
    {
        try
        {
            // Its bad practice to require disk resources in tests, but until somebody gets around
            // to mocking a model configuration, its good enough.
            // If it fails, good to catch it here anyways.
            configuration = Importer.importDefault();
        } catch (JAXBException e)
        {
            fail("setup: JAXBException" + e.getMessage());
        } catch (IOException e)
        {
            fail("setup: IOException" + e.getMessage());
        }
    }

    @Test
    public void testExport()
    {
        try
        {
            String confStr = Exporter.toXML(configuration);
            assertNotNull(confStr);
            Configuration copy = Importer.importFromText(confStr);
            assertArrayEquals(configuration.getLanguages().toArray(), copy.getLanguages().toArray());
        } catch (JAXBException e)
        {
            fail("testExport: JAXBException" + e.getMessage());
        }
    }
}