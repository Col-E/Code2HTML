import me.coley.j2h.regex.PatternHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Tests {
	private final PatternHelper gen = new PatternHelper();

	@Test
	public void testSingleRule() {
		gen.addRule("WORD", "\\w+");
		assertEquals("({WORD}\\w+)", getPat());
	}

	@Test
	public void testTwoRules() {
		gen.addRule("COLOR", "(red)|(green)|(blue)");
		gen.addRule("NUMBR", "(one)|(two)|(three)");
		assertEquals("({COLOR}(red)|(green)|(blue))|({NUMBR}(one)|(two)|(three))", getPat());
	}

	private String getPat() {
		return gen.getPattern().toString();
	}
}
