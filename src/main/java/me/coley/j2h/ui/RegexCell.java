package me.coley.j2h.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import me.coley.j2h.regex.RegexRule;

/**
 * ListCell for displaying RegexRules.
 *
 * @author Matt
 */
public final class RegexCell extends ListCell<RegexRule> {
	@Override
	protected void updateItem(RegexRule item, boolean empty) {
		super.updateItem(item, empty);
		if(empty || item == null) {
			setGraphic(null);
			setText(null);
		} else {
			GridPane grid = new GridPane();
			Label lblName = new Label(item.getRawName() + "   ");
			//Label lblPattern = new Label(escapeJava(item.getPattern()));
			Label lblPattern = new Label(escape(item.getPattern()));
			lblName.getStyleClass().add("title");
			lblPattern.getStyleClass().add("pattern");
			grid.add(lblName, 0, 0);
			grid.add(lblPattern, 1, 0);
			setGraphic(grid);
			setText(null);
		}
	}

	private static String escape(String text) {
		return text
				.replace("\n", "\\n")
				.replace("\t","\\t");
	}
}