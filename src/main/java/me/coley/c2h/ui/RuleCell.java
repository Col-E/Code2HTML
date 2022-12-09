package me.coley.c2h.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import me.coley.c2h.config.model.Rule;

/**
 * ListCell for displaying language rules.
 *
 * @author Matt Coley
 */
public final class RuleCell extends ListCell<Rule> {
	@Override
	protected void updateItem(Rule item, boolean empty) {
		// TODO: Re-add this to the config menu
		//  - likely as a tree-cell since rules can be hierarchies now

		super.updateItem(item, empty);
		if (empty || item == null) {
			setGraphic(null);
			setText(null);
		} else {
			GridPane grid = new GridPane();
			Label lblName = new Label(item.getName() + "   ");
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
		return text.replace("\n", "\\n").replace("\t", "\\t");
	}
}