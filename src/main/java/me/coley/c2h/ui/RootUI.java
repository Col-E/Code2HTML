package me.coley.c2h.ui;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import me.coley.c2h.config.Importer;
import me.coley.c2h.config.model.Configuration;
import me.coley.c2h.ui.pane.ConfigPane;
import me.coley.c2h.ui.pane.InputPane;
import me.coley.c2h.ui.pane.OutputPane;

/**
 * Root UI layout.
 *
 * @author Matt Coley
 */
public class RootUI extends Application {
	private static final int WIDTH = 1080;
	private static final int HEIGHT = 720;
	private final InputPane inputPane;
	private final OutputPane outputPane;
	private final ConfigPane configPane;

	public RootUI() {
		Configuration configuration;
		try {
			configuration = Importer.importDefault();
		} catch (Exception ex) {
			throw new IllegalStateException("Cannot read default config", ex);
		}
		inputPane = new InputPane();
		outputPane = new OutputPane();
		configPane = new ConfigPane(configuration);
		// Hook up listeners
		inputPane.setListener(outputPane);
		configPane.setListener(outputPane);
	}

	@Override
	public void start(Stage primaryStage) {
		SplitPane wrapper = new SplitPane();
		wrapper.setOrientation(Orientation.VERTICAL);
		DetachableTabPane top = new DetachableTabPane();
		DetachableTabPane center = new DetachableTabPane();
		top.addTab("Input", inputPane).setClosable(false);
		top.addTab("Config", configPane).setClosable(false);
		center.addTab("Output", outputPane).setClosable(false);
		wrapper.getItems().addAll(top, center);
		Scene scene = new Scene(wrapper);
		scene.getStylesheets().add("gui.css");
		primaryStage.setWidth(WIDTH);
		primaryStage.setHeight(HEIGHT);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Code2HTML");
		primaryStage.show();
	}
}
