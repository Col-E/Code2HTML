package me.coley.c2h.ui.pane;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import me.coley.c2h.ui.InputUpdateListener;

/**
 * Input pane for source text to stylize.
 *
 * @author Matt Coley
 */
public class InputPane extends BorderPane {
	private InputUpdateListener listener;

	public InputPane() {
		TextArea text = new TextArea("// paste your code here");
		text.getStyleClass().add("mono");
		setCenter(text);
		text.textProperty().addListener((o, old, current) -> {
			if (listener != null) {
				listener.onInput(current);
			}
		});
	}

	public void setListener(InputUpdateListener listener) {
		this.listener = listener;
	}
}
