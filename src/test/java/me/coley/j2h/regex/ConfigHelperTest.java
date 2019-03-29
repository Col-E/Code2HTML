package me.coley.j2h.regex;

import me.coley.j2h.config.ConfigHelper;
import me.coley.j2h.config.Importer;
import me.coley.j2h.config.model.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigHelperTest {
	private ConfigHelper helper;

	@Before
	public void setup() throws JAXBException {
		Configuration configuration = Importer.importFromText("<configuration><language " +
				"name=\"test\"><themes><theme " +
				"name=\"test\"></theme></themes></language></configuration>");
		Language language = configuration.getLanguages().get(0);
		Theme theme = language.getThemes().get(0);
		helper = new ConfigHelper(configuration, language, theme);
	}

	@Test
	public void testSingleRule() {
		helper.addRule("WORD", "\\w+");
		assertEquals("({WORD}\\w+)", getPat());
	}

	@Test
	public void testTwoRules() {
		helper.addRule("COLOR", "(red)|(green)|(blue)");
		helper.addRule("NUMBR", "(one)|(two)|(three)");
		assertEquals("({COLOR}(red)|(green)|(blue))|({NUMBR}(one)|(two)|(three))", getPat());
	}

	@Test
	public void testSingleStyle() {
		helper.addRule("WORD", "\\w+");
		helper.getTheme().update("WORD", "color", "red");
		//
		String expected = ".WORD {\n\tcolor: red;\n}";
		assertTrue(helper.getPatternCSS().contains(expected));
	}

	@Test
	public void testTwoStyles() {
		helper.addRule("WORD", "\\w+");
		helper.addRule("NUMBR", "\\d+");
		helper.getTheme().update("WORD", "color", "red");
		helper.getTheme().update("NUMBR", "color", "blue");
		//
		String expected = ".WORD {\n\tcolor: red;\n}\n.NUMBR {\n\tcolor: blue;\n}";
		assertTrue(helper.getPatternCSS().contains(expected));
	}

	private String getPat() {
		return helper.getPattern().toString();
	}
}