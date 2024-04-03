package me.coley.c2h.ui.pane;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import me.coley.c2h.config.model.Configuration;
import me.coley.c2h.config.model.Language;
import me.coley.c2h.ui.ConfigUpdateListener;
import me.coley.c2h.ui.InputUpdateListener;
import me.coley.c2h.ui.WebScrollHelper;
import me.coley.c2h.util.LanguageMatcher;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Display pane showing:
 * <ul>
 *     <li>HTML preview</li>
 *     <li>HTML source to copy</li>
 *     <ul>
 *         <li>Option to toggle between full HTML page and local {@code <pre>} block</li>
 *         <li>Option to toggle usage of CSS classes vs inline CSS</li>
 *     </ul>
 *     <li>CSS to copy &amp; quickly modify</li>
 * </ul>
 *
 * @author Matt Coley
 */
public class OutputPane extends BorderPane implements ConfigUpdateListener, InputUpdateListener {
	public static String BASE_CSS = "";
	public static String BASE_JS = "";
	private final WebView htmlView = new WebView();
	private final TextArea htmlText = new TextArea();
	private final TextArea cssText = new TextArea();
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
		// Must be daemon so app can shut down without lingering threads
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});
	private Future<?> lastBuildRequest;
	// Config
	private final BooleanProperty inlineCss = new SimpleBooleanProperty();
	private final BooleanProperty fullHtml = new SimpleBooleanProperty();
	private final BooleanProperty includeJs = new SimpleBooleanProperty();
	private Configuration config;
	private Language targetLanguage;
	// Input
	private String lastSource;
	private volatile boolean isHtmlGenerationInProgress;
	private volatile boolean isCssGenerationInProgress;

	static {
		try (InputStream is = OutputPane.class.getResourceAsStream("/code.css")) {
			if (is != null) BASE_CSS = new String(is.readAllBytes());
		} catch (Exception ex) {
			throw new IllegalStateException("Missing 'code.css' resource, cannot infer default CSS");
		}

		try (InputStream is = OutputPane.class.getResourceAsStream("/code.js")) {
			if (is != null) BASE_JS = new String(is.readAllBytes());
		} catch (Exception ex) {
			throw new IllegalStateException("Missing 'code.js' resource, cannot infer default JS");
		}
	}

	public OutputPane() {
		// Outputs
		htmlText.setEditable(false);
		htmlText.getStyleClass().add("mono");
		cssText.getStyleClass().add("mono");
		htmlText.setWrapText(true);
		cssText.setWrapText(true);
		// Options
		CheckBox btnFullHtml = new CheckBox("Include full HTML");
		CheckBox btnIncludeJs = new CheckBox("Include JS");
		CheckBox btnInline = new CheckBox("Use inline CSS");
		btnIncludeJs.disableProperty().bind(btnFullHtml.selectedProperty().not());
		cssText.disableProperty().bind(btnInline.selectedProperty());
		fullHtml.bind(btnFullHtml.selectedProperty());
		includeJs.bind(btnIncludeJs.selectedProperty());
		inlineCss.bind(btnInline.selectedProperty());
		// Layout
		HBox options = new HBox(btnFullHtml, /*btnIncludeJs,*/ btnInline);
		options.setSpacing(15);
		BorderPane textOutputWrapper = new BorderPane();
		options.getStyleClass().add("options-pane");
		SplitPane textOutputs = new SplitPane(htmlText, cssText);
		textOutputWrapper.setTop(options);
		textOutputWrapper.setCenter(textOutputs);
		textOutputs.setOrientation(Orientation.VERTICAL);
		SplitPane split = new SplitPane(htmlView, textOutputWrapper);
		setCenter(split);
		heightProperty().addListener((ob, old, current) ->
				// Odd, but the HTML view height doesn't sync, but the width does.
				// So we need to manually apply resizing here for height changes.
				htmlView.resize(htmlView.getWidth(), current.doubleValue()));
		htmlView.setOnScroll(e -> {
			// Allow [control] + [mouse-wheel] to change web-view zoom level.
			if (e.isControlDown()) {
				double zoom = htmlView.getZoom();
				if (zoom < 1 / 5.0 || zoom > 5) return;

				double speed = 1.125;
				if (e.getDeltaY() > 0) htmlView.setZoom(zoom * speed);
				else htmlView.setZoom(zoom / speed);
			}
		});
		// Property listeners
		fullHtml.addListener((ob, old, current) -> {
			if (!isHtmlGenerationInProgress && config != null) updateHtml();
		});
		includeJs.addListener((ob, old, current) -> {
			if (!isHtmlGenerationInProgress && config != null) updateHtml();
		});
		inlineCss.addListener((ob, old, current) -> {
			if (!isHtmlGenerationInProgress && config != null) updateHtml();
		});
		// Allow user changes to CSS text to update HTML output.
		cssText.textProperty().addListener((ob, old, current) -> {
			if (!isCssGenerationInProgress && config != null) updateHtml();
		});
	}

	@Override
	public void onConfigChanged(Configuration config) {
		this.config = config;
	}

	@Override
	public void onTargetLanguageSet(Language language) {
		targetLanguage = language;
		updateOutput();
	}

	@Override
	public void onInput(String source) {
		lastSource = source;
		updateOutput();
	}

	private void updateOutput() {
		updateCss();
		updateHtml();
	}

	private void updateCss() {
		synchronized (cssText) {
			isCssGenerationInProgress = true;
			String css = createCss();
			cssText.setText(css);
			isCssGenerationInProgress = false;
		}
	}

	private void updateHtml() {
		if (lastBuildRequest == null || lastBuildRequest.isDone()) {
			int scroll = WebScrollHelper.getVScrollValue(htmlView);
			lastBuildRequest = executorService.submit(() -> {
				HtmlGenResults html = createHtml();
				Platform.runLater(() -> {
					isHtmlGenerationInProgress = true;
					htmlText.setText(html.forText);
					htmlView.getEngine().loadContent(html.forView);
					htmlView.getEngine().documentProperty().addListener((ob, old, current) -> {
						if (includeJs.get()) htmlView.getEngine().executeScript("setup()");
						// When the document is ready, restore prior scroll position
						WebScrollHelper.scrollTo(htmlView, 0, scroll);
					});
					isHtmlGenerationInProgress = false;
				});
			});
		} else {
			lastBuildRequest.cancel(true);
		}
	}

	private String createCss() {
		// Get input values
		Language language = targetLanguage;
		try {
			if (language == null) language = config.getLanguages().get(0);
		} catch (Exception ex) {
			return "/* No input language set! */";
		}

		return new LanguageMatcher(language).createPatternCSS();
	}

	private HtmlGenResults createHtml() {
		// Validate source set
		if (lastSource == null) {
			return new HtmlGenResults("<html>" +
					"<h1>Error: Missing inputs</h1>" +
					"<p>Source not set!</p>" +
					"</html>");
		}

		// Get input values
		Language language = targetLanguage;
		try {
			if (language == null) language = config.getLanguages().get(0);
		} catch (Exception ex) {
			return new HtmlGenResults("<html>" +
					"<h1>Error: Missing inputs</h1>" +
					"<p>Language not set!</p>" +
					"</html>");
		}

		// Convert
		try {
			// Because the scroll-bar can disappear from the web-view (which can look confusing)
			// we're going to insert padding so that it is clear that when this occurs the UI isn't
			// busted. The bar just is gone.
			//
			// Additionally, a horizontal overflow in the webview's contents (<pre>) will make the
			// last line not visible. So for our preview we're going to just disable it.
			String viewFixes = "body { padding: 7px; }\n" +
					"pre { overflow: hidden !IMPORTANT; }";
			String css = BASE_CSS + "\n" + cssText.getText();

			// Get <pre> block
			String codeBlock = new LanguageMatcher(language)
					.convert(lastSource, inlineCss.get());

			// Wrap in HTML body
			StringBuilder sbWeb = new StringBuilder();

			sbWeb.append("<html>\n<head>\n");
			if (!inlineCss.get()) sbWeb.append("\n<style>\n").append(css).append("\n</style>\n");
			if (includeJs.get()) sbWeb.append("\n<script>\n").append(BASE_JS).append("\n</script>\n");
			sbWeb.append("\n<style>\n").append(viewFixes).append("\n</style>\n");
			sbWeb.append("\n</head>\n<body>\n");
			sbWeb.append(codeBlock);
			sbWeb.append("\n</body>\n</html>\n");

			if (fullHtml.get()) {
				return new HtmlGenResults(sbWeb.toString());
			} else {
				return new HtmlGenResults(sbWeb.toString(), codeBlock);
			}
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			return new HtmlGenResults("<html>" +
					"<h1>Error: Conversion</h1>" +
					"<p>Exception occurred converting source:</p>" +
					"<pre>" +
					sw +
					"</pre>" +
					"</html>");
		}
	}

	/**
	 * Wrapper, since we want results to be different for the displayed content and the text for the user to copy.
	 */
	static class HtmlGenResults {
		private final String forView;
		private final String forText;

		public HtmlGenResults(String single) {
			this(single, single);
		}

		public HtmlGenResults(String forView, String forText) {
			this.forView = forView;
			this.forText = forText;
		}
	}
}
