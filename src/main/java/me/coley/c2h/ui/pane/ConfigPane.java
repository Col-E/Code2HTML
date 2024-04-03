package me.coley.c2h.ui.pane;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import me.coley.c2h.config.Exporter;
import me.coley.c2h.config.Importer;
import me.coley.c2h.config.model.Configuration;
import me.coley.c2h.config.model.Language;
import me.coley.c2h.ui.ConfigUpdateListener;

import java.io.File;
import java.nio.file.Files;

/**
 * Configuration pane for tweaking the {@link Language} values in a {@link Configuration} instance.
 *
 * @author Matt Coley
 */
public class ConfigPane extends BorderPane {
	private final ObjectProperty<Language> targetLanguage = new SimpleObjectProperty<>();
	private final ObjectProperty<Configuration> targetConfiguration = new SimpleObjectProperty<>();
	private ConfigUpdateListener listener;

	/**
	 * @param initialConfiguration
	 * 		Initial config state to pull from.
	 */
	public ConfigPane(Configuration initialConfiguration) {
		if (initialConfiguration == null || initialConfiguration.getLanguages().isEmpty())
			throw new IllegalArgumentException("Must supply non-empty config");

		targetLanguage.addListener((ob, old, cur) -> {
			if (cur == null) {
				setCenter(null);
			} else {
				setCenter(new LanguagePane(cur, () -> listener));
			}

			refreshMenu();
			if (listener != null) listener.onTargetLanguageSet(cur);
		});

		targetConfiguration.addListener((old, ob, cur) -> {
			targetLanguage.setValue(cur.getLanguages().get(0));
			refreshMenu();
			if (listener != null) listener.onConfigChanged(cur);
		});
		targetConfiguration.setValue(initialConfiguration);
	}

	private void refreshMenu() {
		setTop(generateMenu());
	}

	private Node generateMenu() {
		Configuration config = targetConfiguration.getValue();
		Menu menuLanguages = new Menu("Language");
		{
			for (Language language : config.getLanguages()) {
				MenuItem item = new MenuItem(language.getName());
				item.setOnAction(e -> targetLanguage.setValue(language));
				menuLanguages.getItems().add(item);
			}

			MenuItem itemNewLanguage = new MenuItem("Add new");
			itemNewLanguage.setOnAction(e -> {
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("New language");
				dialog.showAndWait().ifPresent(name -> {
					Language language = new Language(name);
					config.addLanguage(language);
					targetLanguage.setValue(language);
				});
			});

			MenuItem itemRenameLanguage = new MenuItem("Rename current");
			itemRenameLanguage.setOnAction(e -> {
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Rename language");
				dialog.showAndWait().ifPresent(name -> {
					Language language = targetLanguage.getValue();
					language.setName(name);
					refreshMenu();
				});
			});
			itemRenameLanguage.disableProperty().bind(targetLanguage.isNull());


			MenuItem itemRemoveLanguage = new MenuItem("Remove current");
			itemRemoveLanguage.setOnAction(e -> {
				config.getLanguages().remove(targetLanguage.get());
				targetLanguage.set(null);
				setCenter(null);
				refreshMenu();
			});
			itemRemoveLanguage.disableProperty().bind(targetLanguage.isNull());

			menuLanguages.getItems().addAll(new SeparatorMenuItem(), itemNewLanguage, itemRenameLanguage, itemRemoveLanguage);
		}

		Menu menuFile = new Menu("File");
		{
			MenuItem itemLoad = new MenuItem("Load config");
			MenuItem itemSave = new MenuItem("Save config");
			itemLoad.setOnAction(e -> {
				FileChooser chooser = new FileChooser();
				chooser.setTitle("Load config");
				File file = chooser.showOpenDialog(getScene().getWindow());
				if (file != null && file.exists()) {
					try {
						Configuration v = Importer.importFromFile(file.toPath());
						targetConfiguration.setValue(v);
					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
					}
				}
			});
			itemSave.setOnAction(e -> {
				FileChooser chooser = new FileChooser();
				chooser.setTitle("Save config");
				chooser.setInitialFileName("config.xml");
				File file = chooser.showSaveDialog(getScene().getWindow());
				if (file != null) {
					try {
						Files.writeString(file.toPath(), Exporter.toXML(config));
					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
					}
				}
			});
			menuFile.getItems().addAll(itemLoad, itemSave);
		}
		return new MenuBar(menuFile, menuLanguages);
	}

	/**
	 * @param listener
	 * 		Listener to receive updates from changes to the configuration.
	 */
	public void setListener(ConfigUpdateListener listener) {
		this.listener = listener;

		// When assigned, pass the config and initial language.
		if (listener != null) {
			listener.onConfigChanged(targetConfiguration.get());
			listener.onTargetLanguageSet(targetLanguage.get());
		}
	}
}
