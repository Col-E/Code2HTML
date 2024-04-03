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
	private static final String INITIAL_TEXT = "// paste your code here";
	private InputUpdateListener listener;
	private String lastText = INITIAL_TEXT;

	public InputPane() {
		TextArea text = new TextArea(INITIAL_TEXT);
		text.getStyleClass().add("mono");
		text.textProperty().addListener((o, old, current) -> {
			lastText = current;
			if (listener != null) {
				listener.onInput(current);
			}
		});
		setCenter(text);
	}

	public void setListener(InputUpdateListener listener) {
		this.listener = listener;

		if (listener != null)
			listener.onInput(lastText);
	}
}
