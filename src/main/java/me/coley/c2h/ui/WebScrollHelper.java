package me.coley.c2h.ui;

import javafx.scene.web.WebView;

/**
 * Utilities for {@link WebView} scrolling.
 *
 * @author <a href="https://stackoverflow.com/a/31268321">miho</a>
 */
public class WebScrollHelper {
	/**
	 * Scrolls to the specified position.
	 *
	 * @param view
	 * 		web view that shall be scrolled
	 * @param x
	 * 		horizontal scroll value
	 * @param y
	 * 		vertical scroll value
	 */
	public static void scrollTo(WebView view, int x, int y) {
		view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
	}

	/**
	 * Returns the vertical scroll value, i.e. thumb position.
	 * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue().
	 *
	 * @param view
	 *
	 * @return vertical scroll value
	 */
	public static int getVScrollValue(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollTop");
	}

	/**
	 * Returns the horizontal scroll value, i.e. thumb position.
	 * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue()}.
	 *
	 * @param view
	 *
	 * @return horizontal scroll value
	 */
	public static int getHScrollValue(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollLeft");
	}

	/**
	 * Returns the maximum vertical scroll value.
	 * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
	 *
	 * @param view
	 *
	 * @return vertical scroll max
	 */
	public static int getVScrollMax(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollWidth");
	}

	/**
	 * Returns the maximum horizontal scroll value.
	 * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
	 *
	 * @param view
	 *
	 * @return horizontal scroll max
	 */
	public static int getHScrollMax(WebView view) {
		return (Integer) view.getEngine().executeScript("document.body.scrollHeight");
	}
}
