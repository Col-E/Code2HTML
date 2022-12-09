package me.coley.c2h.ui;

/**
 * Listener to handle text inputs from {@link me.coley.c2h.ui.pane.InputPane}
 *
 * @author Matt Coley
 */
public interface InputUpdateListener {
	/**
	 * @param source
	 * 		Input text.
	 */
	void onInput(String source);
}
