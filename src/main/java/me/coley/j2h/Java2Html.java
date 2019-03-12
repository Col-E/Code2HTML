package me.coley.j2h;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;
import jregex.Matcher;
import jregex.Pattern;
import me.coley.j2h.loader.Importer;
import me.coley.j2h.modle.Configuration;
import me.coley.j2h.modle.Language;
import me.coley.j2h.modle.Rule;
import me.coley.j2h.modle.Theme;
import me.coley.j2h.regex.PatternHelper;
import me.coley.j2h.regex.RegexRule;
import me.coley.j2h.ui.RegexCell;
import org.apache.commons.io.IOUtils;
import org.controlsfx.validation.ValidationSupport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;
import static org.controlsfx.validation.Validator.createEmptyValidator;
import static org.controlsfx.validation.Validator.createPredicateValidator;

/**
 * Text to styled HTML powered by Regex.
 *
 * @author Matt
 */
public class Java2Html extends Application {
	private final PatternHelper helper = new PatternHelper();
	// Base values
	private static String css = "";
	private static String js = "";
	// Controls
	private final WebView browser = new WebView();
	private final TextArea txtInput = new TextArea();
	private final TextArea txtHTML = new TextArea();
	private final TextArea txtCSS = new TextArea();
	private final TextArea txtJS = new TextArea();
	//
	private boolean canUpdate = true;

	public static void main(String[] args) {
		try {
			css = IOUtils.toString(Java2Html.class.getResourceAsStream("/code.css"), UTF_8);
			js = IOUtils.toString(Java2Html.class.getResourceAsStream("/code.js"), UTF_8);
		} catch(Exception e) {}
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		try {
			// Setup initial regular expressions
			initRegex();
		} catch (JAXBException | IOException e) {
			// In time configuration loading issues need to be displayed
			// in the UI as a helpful error message.
			e.printStackTrace();
		}
		// Inputs
		txtInput.setText("class Example { \n\t// put source code here\n}");
		txtInput.setFont(Font.font("monospace"));
		txtHTML.setFont(Font.font("monospace"));
		txtCSS.setFont(Font.font("monospace"));
		txtJS.setFont(Font.font("monospace"));
		txtHTML.setEditable(false);
		txtCSS.setText(helper.getPatternCSS() + css);
		txtJS.setText(js);
		txtInput.textProperty().addListener((ob, o, n) -> {
			// Update HTML
			update();
		});
		txtCSS.textProperty().addListener((ob, o, n) -> {
			// Update RegexRule style map
			// - Assume 'pre' tag still exists. Hacky but should be fine
			int indx = txtCSS.getText().indexOf("pre {");
			String old = o.substring(0, indx);
			String cur = n.substring(0, indx);
			// Only update if the section for custom elements is updated.
			if(!old.equalsIgnoreCase(cur)) {
				// Iterate over css tags (by matching via regex)
				for(RegexRule rule : helper.getRules()) {
					String r = "(\\.({TITLE}" + rule.getRawName() + ")\\s*\\{({BODY}[^}]*))\\}";
					Pattern pattern = new Pattern(r);
					Matcher matcher = pattern.matcher(cur);
					if(!matcher.find())
						return;
					String body = matcher.group("BODY");
					// Parse the body of the css class and update the regex-rule style map.
					r = "({key}\\S+):\\s*({value}.+)";
					pattern = new Pattern(r);
					matcher = pattern.matcher(body);
					while(matcher.find()) {
						String key = matcher.group("key");
						String value = matcher.group("value");
						rule.getStyle().put(key, value);
					}
				}
			}
			// Update HTML
			update();
		});
		txtJS.textProperty().addListener((ob, o, n) -> {
			// Update HTML
			update();
		});
		// Config
		ListView<RegexRule> view = new ListView<>();
		view.getItems().addAll(helper.getRules());
		view.setCellFactory(v -> new RegexCell());
		view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		BorderPane config = new BorderPane();
		config.setCenter(view);
		GridPane configInputs = new GridPane();
		Button btnAdd = new Button("New Rule");
		Button btnRemove = new Button("Remove Selected");
		Button btnUp = new Button("Move Up");
		Button btnDown = new Button("Move Down");
		btnRemove.setDisable(true);
		btnUp.setDisable(true);
		btnDown.setDisable(true);
		configInputs.add(btnAdd, 0, 0);
		configInputs.add(btnRemove, 1, 0);
		configInputs.add(btnUp, 2, 0);
		configInputs.add(btnDown, 3, 0);
		config.setBottom(configInputs);
		view.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> {
			btnRemove.setDisable(n == null);
			// Moving
			int i = view.getSelectionModel().getSelectedIndex();
			btnUp.setDisable(n == null || i == 0);
			btnDown.setDisable(n == null || i == view.getItems().size() - 1);
		});
		view.getItems().addListener((ListChangeListener<RegexRule>) c -> {
			helper.getRules().clear();
			for(RegexRule rule : view.getItems())
				helper.addRule(rule);
			txtCSS.setText(helper.getPatternCSS() + css);
			update();
		});
		btnAdd.setOnAction(e -> {
			// Inputs
			TextField txtName = new TextField();
			txtName.setPromptText("Name");
			TextField txtRegex = new TextField();
			txtRegex.setPromptText("Regex pattern");
			ValidationSupport valiation = new ValidationSupport();
			Platform.runLater(() -> {
				valiation.registerValidator(txtName, createEmptyValidator("Name cannot be empty"));
				valiation.registerValidator(txtRegex, createPredicateValidator(s -> {
					try {
						new Pattern(txtRegex.getText());
						return true;
					} catch(Exception ex) {
						return false;
					}
				}, "Regex pattern must compile with JRegex"));
			});
			// Setup grid
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));
			grid.add(new Label("Name:"), 0, 0);
			grid.add(txtName, 1, 0);
			grid.add(new Label("Regex:"), 0, 1);
			grid.add(txtRegex, 1, 1);
			Platform.runLater(() -> txtName.requestFocus());
			// Create dialog
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("New Regex Rule");
			valiation.invalidProperty().addListener((ob, o, n) -> {
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(n);
			});
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(grid);
			dialog.setResultConverter(dialogButton -> {
				if(dialogButton == ButtonType.OK) {
					return new Pair<>(txtName.getText(), txtRegex.getText());
				}
				return null;
			});
			Optional<Pair<String, String>> result = dialog.showAndWait();
			if(result.isPresent()) {
				Pair<String, String> pair = result.get();
				view.getItems().add(new RegexRule(pair.getKey(), pair.getValue()));
			}
		});
		btnRemove.setOnAction(e -> view.getItems().remove(view.getSelectionModel()
				.getSelectedIndex()));
		btnUp.setOnAction(e -> {
			int i = view.getSelectionModel().getSelectedIndex();
			canUpdate = false;
			Collections.swap(helper.getRules(), i, i - 1);
			Collections.swap(view.getItems(), i, i - 1);
			view.getSelectionModel().select(i - 1);
			canUpdate = true;
		});
		btnDown.setOnAction(e -> {
			int i = view.getSelectionModel().getSelectedIndex();
			canUpdate = false;
			Collections.swap(helper.getRules(), i, i + 1);
			Collections.swap(view.getItems(), i, i + 1);
			view.getSelectionModel().select(i + 1);
			canUpdate = true;
		});
		// Tabs
		TabPane tabs = new TabPane();
		Tab tabHTML = new Tab("HTML", txtHTML);
		Tab tabCSS = new Tab("CSS", txtCSS);
		Tab tabJS = new Tab("JS", txtJS);
		Tab tabConfig = new Tab("Configuration", config);
		tabHTML.getStyleClass().add("tab-small");
		tabCSS.getStyleClass().add("tab-small");
		tabJS.getStyleClass().add("tab-small");
		tabConfig.getStyleClass().add("tab-large");
		tabs.getTabs().add(tabHTML);
		tabs.getTabs().add(tabCSS);
		tabs.getTabs().add(tabJS);
		tabs.getTabs().add(tabConfig);
		// Layout
		SplitPane pane = new SplitPane(txtInput, tabs);
		SplitPane vert = new SplitPane(pane, browser);
		vert.setOrientation(Orientation.VERTICAL);
		Scene scene = new Scene(vert, 900, 800);
		scene.getStylesheets().add("gui.css");
		stage.setScene(scene);
		stage.setTitle("Java2Html");
		stage.show();
		Platform.runLater(() -> update());
	}

	/**
	 * Add regex rules for matching code.
	 */
	private void initRegex() throws JAXBException, IOException {
		Configuration configuration = Importer.importDefaultConfiguration();
		Language java = configuration.findLanguageByNames("Java");
		Theme theme = java.getThemes().get(0);
		for (Rule rule : java.getRules()) {
			RegexRule regexRule = new RegexRule(rule.getName(), rule.getPattern());
			regexRule.addStyle(theme.getStylesForTargetByName(rule.getName()));
			helper.addRule(regexRule);
		}
	}

	private void update() {
		if(!canUpdate) {
			return;
		}
		String text = txtInput.getText().replace("\t", "    ");
		Pattern pattern = helper.getPattern();
		Matcher matcher = pattern.matcher(text);
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;
		while(matcher.find()) {
			String styleClass = helper.getClassFromGroup(matcher);
			int start = matcher.start();
			int end = matcher.end();
			// append text not matched
			if(start > lastEnd) {
				String unmatched = escapeHtml4(text.substring(lastEnd, start));
				sb.append(unmatched);
			}
			// append match
			String matched = escapeHtml4(text.substring(start, end));
			sb.append("<span class=\"" + styleClass + "\">" + matched + "</span>");
			lastEnd = end;
		}
		// Append ending text not matched
		sb.append(escapeHtml4(text.substring(lastEnd)));
		// Apply line formatting to each line
		StringBuilder fmt = new StringBuilder();
		for(String line : sb.toString().split("\n"))
			fmt.append("<span class=\"line\"></span>" + line + "\n");
		// Wrap in pre tags and slap it in an HTML page
		String html = "<pre>" + fmt.toString() + "</pre>";
		String style = txtCSS.getText();
		StringBuilder sbWeb = new StringBuilder();
		sbWeb.append("<html><head><style>");
		sbWeb.append(style);
		sbWeb.append("</style></head><body><h2 style=\"font-family: sans-serif;\">Preview</h2>");
		sbWeb.append(html);
		sbWeb.append("</body></html>");
		browser.getEngine().loadContent(sbWeb.toString());
		txtHTML.setText(html);
	}
}
