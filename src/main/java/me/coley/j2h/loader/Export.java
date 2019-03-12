package me.coley.j2h.loader;

import me.coley.j2h.modle.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;

public class Export {

    // Current demo code for reference ready for when the UI has export button.
    public static void main(String[] args) throws JAXBException, IOException {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(Configuration.class);
        Marshaller m = context.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Write to System.out
        m.marshal(Importer.importDefaultConfiguration(), System.out);
    }
}
