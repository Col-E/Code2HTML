package me.coley.j2h;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

// https://mvnrepository.com/artifact/org.apache.commons/commons-text
import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic java to HTML converter using regex
 *
 * @author Matt
 */
public class Java2Html extends Application {
	//@formatter:off
	private static final String[] KEYWORDS = new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch",
			"char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
			"float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
			"package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while" };
	//@formatter:on
	private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String CONST_HEX_PATTERN = "(0[xX][0-9a-fA-F]+)+";
	private static final String CONST_VAL_PATTERN = "(\\b([\\d._]*[\\d])\\b)+|(true|false|null)";
	private static final String CONST_PATTERN = CONST_HEX_PATTERN + "|" + CONST_VAL_PATTERN;
	private static final String COMMENT_SINGLE_PATTERN = "//[^\n]*";
	private static final String COMMENT_MULTI_SINGLE_PATTERN = "/[*](.|\\R)+?\\*/";
	private static final String COMMENT_MULTI_JAVADOC_PATTERN = "/[*]{2}(.|\\R)+?\\*/";
	private static final String ANNOTATION_PATTERN = "\\B(@[\\w]+)\\b";
	//@formatter:off
	private static final Pattern PATTERN = Pattern.compile(
			 "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
			"|(?<STRING>" + STRING_PATTERN + ")" +
			"|(?<COMMENTDOC>" + COMMENT_MULTI_JAVADOC_PATTERN + ")" +
			"|(?<COMMENTMULTI>" + COMMENT_MULTI_SINGLE_PATTERN + ")" +
			"|(?<COMMENTLINE>" + COMMENT_SINGLE_PATTERN + ")" +
			"|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")" +
			"|(?<CONSTPATTERN>" + CONST_PATTERN + ")");
	//@formatter:on
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		TextArea input = new TextArea("class Example { \n" + "\t// put source code here\n" + "}");
		TextArea output = new TextArea();
		WebView browser = new WebView();
		input.textProperty().addListener((observable, oldValue, newValue) -> {
			update(output, newValue, browser);
		});
		SplitPane pane = new SplitPane(input, output);
		SplitPane vert = new SplitPane(pane, browser);
		vert.setOrientation(Orientation.VERTICAL);
		primaryStage.setScene(new Scene(vert, 900, 800));
		primaryStage.setTitle("Java2Html");
		primaryStage.show();
		Platform.runLater(() -> {
			browser.getEngine().setUserStyleSheetLocation(getClass().getResource("/code.css").toString());
			update(output, input.getText(), browser);
		});
	}

	/**
	 * Set the value of the TextArea <i>(output)</i> to the HTML-decorated version of the input <i>(text)</i>.
	 *
	 * @param output
	 * 		TextArea that will host the output.
	 * @param text
	 * 		Input text <i>(Java code)</i>
	 * @param browser
	 * 		Browser to preview HTML output.
	 */
	private static void update(TextArea output, String text, WebView browser) {
		text = text.replace("\t", "    ");
		Matcher matcher = PATTERN.matcher(text);
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;
		while(matcher.find()) {
			//@formatter:off
			String styleClass =
					  matcher.group("STRING")       != null ? "string"
					: matcher.group("KEYWORD")      != null ? "keyword"
					: matcher.group("COMMENTDOC")   != null ? "comment-javadoc"
					: matcher.group("COMMENTMULTI") != null ? "comment-multi"
					: matcher.group("COMMENTLINE")  != null ? "comment-line"
					: matcher.group("CONSTPATTERN") != null ? "const"
					: matcher.group("ANNOTATION")   != null ? "annotation" : null;
			//@formatter:on
			int start = matcher.start();
			int end = matcher.end();
			// append text not matched
			if(start > lastEnd) {
				sb.append(StringEscapeUtils.escapeHtml4(text.substring(lastEnd, start)));
			}
			// append match
			sb.append("<span class=\"" + styleClass + "\">" + StringEscapeUtils.escapeHtml4(text.substring(start, end)) + "</span>");
			lastEnd = end;
		}
		sb.append(StringEscapeUtils.escapeHtml4(text.substring(lastEnd)));
		StringBuilder fmt = new StringBuilder();
		for(String line : sb.toString().split("\n")) {
			fmt.append("<span class=\"line\"></span>" + line + "\n");
		}
		String html = "<pre>" + fmt.toString() + "</pre>";
		browser.getEngine().loadContent("<html><body><h2 style=\"font-family: sans-serif;\">Preview</h2>" + html + "</body></html>");
		output.setText(html);
	}
}
