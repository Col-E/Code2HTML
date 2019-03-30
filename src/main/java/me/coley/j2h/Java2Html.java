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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import jregex.Matcher;
import jregex.Pattern;
import me.coley.j2h.config.*;
import me.coley.j2h.config.model.*;
import me.coley.j2h.ui.RuleCell;
import org.apache.commons.io.IOUtils;
import org.controlsfx.validation.ValidationSupport;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.controlsfx.validation.Validator.createEmptyValidator;
import static org.controlsfx.validation.Validator.createPredicateValidator;

/**
 * Text to styled HTML powered by Regex.
 *
 * @author Matt
 */
public class Java2Html extends Application {
	// Base values
	private static String css = "";
	private static String js = "";
	// Controls
	private final WebView browser = new WebView();
	private final TextArea txtInput = new TextArea();
	private final TextArea txtHTML = new TextArea();
	private final TextArea txtCSS = new TextArea();
	private final TextArea txtJS = new TextArea();
	// Misc layout & stuff
	private final Menu mnLang = new Menu("Language");
	private final Menu mnTheme = new Menu("Theme");
	private final BorderPane patternsPane = new BorderPane();
	private Stage stage;
	private boolean previewLock;
	// Config
	private ConfigHelper helper;


	public static void main(String[] args) {
		try {
			css = IOUtils.toString(Java2Html.class.getResourceAsStream("/code.css"), UTF_8);
			js = IOUtils.toString(Java2Html.class.getResourceAsStream("/code.js"), UTF_8);
		} catch(Exception e) {
			System.err.println("Failed to load default resources: css/js");
			e.printStackTrace();
			System.exit(-1);
		}
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		try {
			// Setup
			Configuration configuration = Importer.importDefault();
			Language language = configuration.getLanguages().get(0);
			Theme theme = language.getThemes().get(0);
			helper = new ConfigHelper(configuration, language, theme);
		} catch(Exception e) {
			e.printStackTrace();
			fatal("Invalid default configuration",
					"The default configuration file failed to be read.",
					"Please ensure the default configuration file is properly formatted.");
		}
		// Inputs
		txtInput.setText("class Example { \n\t// put source code here\n}");
		txtInput.setFont(Font.font("monospace"));
		txtHTML.setFont(Font.font("monospace"));
		txtCSS.setFont(Font.font("monospace"));
		txtJS.setFont(Font.font("monospace"));
		txtHTML.setEditable(false);
		updateCSS();
		txtJS.setText(js);
		txtInput.textProperty().addListener((ob, o, n) -> {
			// Update HTML
			updateHTML();
		});
		txtCSS.textProperty().addListener((ob, o, n) -> {
			// Update style map
			// - Assume 'pre' tag still exists. Hacky but should be fine
			int index = txtCSS.getText().indexOf("pre {");
			String old = o.substring(0, index);
			String cur = n.substring(0, index);
			// Only update if the section for custom elements is updated.
			if(!old.equalsIgnoreCase(cur)) {
				// Iterate over css tags (by matching via regex)
				for(Rule rule : helper.getRules()) {
					String r = "(\\.({TITLE}" + rule.getName() + ")\\s*\\{({BODY}[^}]*))\\}";
					Pattern pattern = new Pattern(r);
					Matcher matcher = pattern.matcher(cur);
					if(!matcher.find())
						return;
					String body = matcher.group("BODY");
					// Parse the body of the css class and update the regex-rule style map.
					r = "({key}\\S+):\\s*({value}.+)(?=;)";
					pattern = new Pattern(r);
					matcher = pattern.matcher(body);
					while(matcher.find()) {
						String key = matcher.group("key");
						String value = matcher.group("value");
						helper.getTheme().update(rule.getName(), key, value);
					}
				}
			}
			// Update HTML
			updateHTML();
		});
		txtJS.textProperty().addListener((ob, o, n) -> {
			// Update HTML
			updateHTML();
		});
		// Config
		updateRulesPane();
		// Tabs
		TabPane tabs = new TabPane();
		Tab tabHTML = new Tab("HTML", txtHTML);
		Tab tabCSS = new Tab("CSS", txtCSS);
		Tab tabJS = new Tab("JS", txtJS);
		Tab tabPatterns = new Tab("Patterns", patternsPane);
		tabHTML.getStyleClass().add("tab-small");
		tabCSS.getStyleClass().add("tab-small");
		tabJS.getStyleClass().add("tab-small");
		tabPatterns.getStyleClass().add("tab-large");
		tabs.getTabs().add(tabHTML);
		tabs.getTabs().add(tabCSS);
		tabs.getTabs().add(tabJS);
		tabs.getTabs().add(tabPatterns);
		// Menubar
		Menu mnFile = new Menu("File");
		MenuItem miConfigLoad = new MenuItem("Load config...");
		MenuItem miConfigSave = new MenuItem("Save config...");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Configurations",
				"*.j2h", "*.xml");
		FileChooser fcSave = new FileChooser();
		fcSave.setInitialDirectory(new File(System.getProperty("user.dir")));
		fcSave.setTitle("Save configuration");
		fcSave.getExtensionFilters().add(extFilter);
		FileChooser fcLoad = new FileChooser();
		fcLoad.setInitialDirectory(new File(System.getProperty("user.dir")));
		fcLoad.setTitle("Select configuration");
		fcLoad.getExtensionFilters().add(extFilter);
		miConfigLoad.setOnAction(e -> {
			File file = fcLoad.showOpenDialog(stage);
			if(file != null) {
				try {
					Configuration configuration = Importer.importFromFile(file.getAbsolutePath());
					setConfiguration(configuration);
				} catch(IOException ex) {
					ex.printStackTrace();
					error("Configuration write failure",
							"The configuration could not be read from the selected file.",
							"Ensure the file location is valid (permissions, disk space)");
				} catch(JAXBException ex) {
					ex.printStackTrace();
					error("Configuration load failure",
							"The configuration could not be parsed.",
							"The configuration instance could not be read from XML");

				}
			}
		});
		miConfigSave.setOnAction(e -> {
			File file = fcSave.showSaveDialog(stage);
			if(file != null) {
				try {
					String text = Exporter.toString(helper.getConfiguration());
					Files.write(Paths.get(file.toURI()), text.getBytes(UTF_8));
				} catch(IOException ex) {
					ex.printStackTrace();
					error("Configuration write failure",
							"The configuration could not be written to the selected file.",
							"Ensure the file location is valid (permissions, disk space)");
				} catch(JAXBException ex) {
					ex.printStackTrace();
					error("Configuration write failure",
							"The configuration could not be exported.",
							"The configuration instance could not be translated into XML");
				}
			}
		});
		mnFile.getItems().addAll(miConfigLoad);
		mnFile.getItems().addAll(miConfigSave);
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(mnFile, mnLang, mnTheme);
		// Layout
		SplitPane pane = new SplitPane(txtInput, tabs);
		SplitPane vert = new SplitPane(pane, browser);
		vert.setOrientation(Orientation.VERTICAL);
		BorderPane wrap = new BorderPane();
		wrap.setTop(menuBar);
		wrap.setCenter(vert);
		Scene scene = new Scene(wrap, 900, 800);
		scene.getStylesheets().add("gui.css");
		stage.setScene(scene);
		updateTitle();
		updateLanguageMenu();
		updateThemeMenu();
		stage.show();
		Platform.runLater(() -> updateHTML());
	}

	/**
	 * Update the ui that need to be refreshed for the new configuration.
	 *
	 * @param configuration
	 * 		New configuration to load.
	 */
	private void setConfiguration(Configuration configuration) {
		if(configuration.getLanguages().isEmpty()) {
			error("Configuration load error",
					"The configuration has no languages.",
					"Use a config that has a specified language");
			return;
		}
		Language language = configuration.getLanguages().get(0);
		if(language.getThemes().isEmpty()) {
			error("Configuration load error",
					"The configuration has no themes.",
					"Use a config that has a specified theme");
			return;
		}
		Theme theme = language.getThemes().get(0);
		helper = new ConfigHelper(configuration, language, theme);
		updateCSS();
		updateRulesPane();
		updateLanguageMenu();
		updateThemeMenu();
		updateHTML();
	}

	/**
	 * Update the language menu to show the currently supported languages.
	 */
	private void updateLanguageMenu() {
		mnLang.getItems().clear();
		for(Language language : helper.getConfiguration().getLanguages()) {
			// Skip language if it has no theme
			if(language.getThemes().isEmpty())
				continue;
			// Create menu-item that sets the current language
			String name = language.getName();
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			MenuItem miLang = new MenuItem(name);
			mnLang.getItems().add(miLang);
			// Disable if the language is the currently active one
			if(language == helper.getLanguage()) {
				miLang.setDisable(true);
			} else {
				miLang.setOnAction(e -> {
					helper.setLanguage(language);
					helper.setTheme(language.getThemes().get(0));
					updateRulesPane();
					updateThemeMenu();
					updateHTML();
					updateTitle();
				});
			}
		}
	}

	/**
	 * Update te theme menu to show the currently supported themes for the language.
	 */
	private void updateThemeMenu() {
		mnTheme.getItems().clear();
		for (Theme theme : helper.getLanguage().getThemes()) {
			// Create menu-item that sets the current theme
			String name = theme.getName();
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			MenuItem miTheme = new MenuItem(name);
			mnTheme.getItems().add(miTheme);
			// Disable if the theme is the currently active one
			if(theme == helper.getTheme()) {
				miTheme.setDisable(true);
			} else {
				miTheme.setOnAction(e -> {
					helper.setTheme(theme);
					updateRulesPane();
					updateCSS();
					updateHTML();
				});
			}
		}
		mnTheme.getItems().add(new SeparatorMenuItem());
		MenuItem mnNew = new MenuItem("New Theme...");
		mnNew.setOnAction(e -> {
			TextField txtName = new TextField();
			txtName.setPromptText("Name");
			ValidationSupport valiation = new ValidationSupport();
			Platform.runLater(() -> {
				valiation.registerValidator(txtName, createEmptyValidator("Name cannot be empty"));
			});
			// Create dialog
			Dialog<String> dialog = new Dialog<>();
			dialog.setTitle("New Theme");
			valiation.invalidProperty().addListener((ob, o, n) -> {
				dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(n);
			});
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(txtName);
			dialog.setResultConverter(dialogButton -> {
				if(dialogButton == ButtonType.OK) {
					return txtName.getText();
				}
				return null;
			});
			Optional<String> result = dialog.showAndWait();
			if(result.isPresent()) {
				Theme theme = new Theme();
				theme.setName(result.get());
				helper.getLanguage().addTheme(theme);
				helper.setTheme(theme);
				updateThemeMenu();
			}
		});
		mnTheme.getItems().add(mnNew);
	}

	/**
	 * Reset the config pane. Shows the rules of the currently active language.
	 */
	private void updateRulesPane() {
		ListView<Rule> view = new ListView<>();
		view.getItems().addAll(helper.getRules());
		view.setCellFactory(v -> new RuleCell());
		view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		GridPane gridRuleBtns = new GridPane();
		Button btnAdd = new Button("New Rule");
		Button btnRemove = new Button("Remove Selected");
		Button btnUp = new Button("Move Up");
		Button btnDown = new Button("Move Down");
		btnRemove.setDisable(true);
		btnUp.setDisable(true);
		btnDown.setDisable(true);
		gridRuleBtns.add(btnAdd, 0, 0);
		gridRuleBtns.add(btnRemove, 1, 0);
		gridRuleBtns.add(btnUp, 2, 0);
		gridRuleBtns.add(btnDown, 3, 0);
		view.getSelectionModel().selectedItemProperty().addListener((ob, o, n) -> {
			btnRemove.setDisable(n == null);
			// Moving
			int i = view.getSelectionModel().getSelectedIndex();
			btnUp.setDisable(n == null || i == 0);
			btnDown.setDisable(n == null || i == view.getItems().size() - 1);
		});
		view.getItems().addListener((ListChangeListener<Rule>) c -> {
			helper.getRules().clear();
			for(Rule rule : view.getItems())
				helper.addRule(rule);
			updateCSS();
			updateHTML();
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
				Rule rule = new Rule();
				rule.setName(pair.getKey());
				rule.setPattern(pair.getValue());
				view.getItems().add(rule);
			}
		});
		btnRemove.setOnAction(e -> view.getItems().remove(view.getSelectionModel()
				.getSelectedIndex()));
		btnUp.setOnAction(e -> {
			int i = view.getSelectionModel().getSelectedIndex();
			previewLock = true;
			Collections.swap(helper.getRules(), i, i - 1);
			Collections.swap(view.getItems(), i, i - 1);
			view.getSelectionModel().select(i - 1);
			previewLock = false;
		});
		btnDown.setOnAction(e -> {
			int i = view.getSelectionModel().getSelectedIndex();
			previewLock = false;
			Collections.swap(helper.getRules(), i, i + 1);
			Collections.swap(view.getItems(), i, i + 1);
			view.getSelectionModel().select(i + 1);
			previewLock = true;
		});
		patternsPane.setCenter(view);
		patternsPane.setBottom(gridRuleBtns);
	}

	/**
	 * Update title to match current language.
	 */
	private void updateTitle() {
		String name = helper.getLanguage().getName();
		name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
		stage.setTitle(name + "2Html");
	}

	/**
	 * Update CSS text.
	 */
	private void updateCSS() {
		txtCSS.setText(helper.getPatternCSS() + css);
	}

	/**
	 * Generate and update HTML output / preview.
	 */
	private void updateHTML() {
		if(previewLock) {
			return;
		}
		String text = txtInput.getText().replace("\t", "    ");
		String html = helper.convert(text);
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

	/**
	 * Show error dialog and then exit.
	 *
	 * @param title
	 * 		Dialog title.
	 * @param header
	 * 		Message header.
	 * @param content
	 * 		Message content.
	 */
	private static void fatal(String title, String header, String content) {
		error(title, header, content);
		System.exit(-1);
	}

	/**
	 * Show error dialog.
	 *
	 * @param title
	 * 		Dialog title.
	 * @param header
	 * 		Message header.
	 * @param content
	 * 		Message content.
	 */
	private static void error(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

}
