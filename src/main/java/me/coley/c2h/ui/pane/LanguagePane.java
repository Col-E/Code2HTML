package me.coley.c2h.ui.pane;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import me.coley.c2h.config.model.Language;
import me.coley.c2h.config.model.Rule;
import me.coley.c2h.config.model.StyleProperty;
import me.coley.c2h.ui.ConfigUpdateListener;
import me.coley.c2h.util.Regex;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Configuration pane for modifying contents of a {@link Language}.
 *
 * @author Matt Coley
 */
class LanguagePane extends BorderPane {
	private final Supplier<ConfigUpdateListener> listenerProxy;
	private final Language language;

	/**
	 * @param language
	 * 		Target language.
	 * @param listenerProxy
	 * 		Proxy to listener to call when changes are made.
	 */
	public LanguagePane(Language language, Supplier<ConfigUpdateListener> listenerProxy) {
		this.language = language;
		this.listenerProxy = listenerProxy;

		// TODO:
		//  - rule
		//    - add rule
		//    - remove rule
		//    - change name

		VBox box = new VBox();
		box.setFillWidth(true);
		box.setPadding(new Insets(5));
		ScrollPane scrollPane = new ScrollPane(box);
		for (Rule rule : language.getRules()) {
			visit(pane -> box.getChildren().add(pane), rule);
		}

		// Hack
		box.prefWidthProperty().bind(scrollPane.widthProperty().subtract(15));

		// Label title = new Label(language.getName());
		// title.getStyleClass().add("h1");
		// setTop(title);
		setCenter(scrollPane);
	}

	private void notifyChange() {
		listenerProxy.get().onTargetLanguageSet(language);
	}

	private void visit(Consumer<RulePane> placer, Rule rule) {
		RulePane pane = new RulePane(rule);

		placer.accept(pane);

		List<Rule> subrules = rule.getSubrules();
		if (!subrules.isEmpty()) {
			VBox box = new VBox();
			box.setFillWidth(true);
			box.setPadding(new Insets(10, 10, 10, 60));
			pane.setBottom(box);

			for (Rule subrule : subrules) {
				visit(subpane -> box.getChildren().add(subpane), subrule);
			}
		}
	}

	private class RulePane extends BorderPane {
		public RulePane(Rule rule) {
			// Title
			Label title = new Label(rule.getName());
			title.getStyleClass().add("h2");
			setTop(title);

			// Regex
			TextField patternInput = new TextField(rule.getPattern());
			patternInput.setMaxWidth(Integer.MAX_VALUE);
			patternInput.getStyleClass().add("input");
			patternInput.textProperty().addListener((ob, old, cur) -> {
				if (Regex.isValid(cur)) {
					rule.setPattern(cur);
					notifyChange();
				}
			});

			// Style
			StyleGrid styleGrid = new StyleGrid(rule);

			// Layout
			Label styleLabel = new Label("Styles: ");
			GridPane grid = new GridPane(10, 10);
			GridPane.setValignment(styleGrid, VPos.TOP);
			ColumnConstraints alwaysGrow = new ColumnConstraints();
			ColumnConstraints neverGrow = new ColumnConstraints();
			alwaysGrow.setHgrow(Priority.ALWAYS);
			neverGrow.setHgrow(Priority.NEVER);
			grid.getColumnConstraints().addAll(neverGrow, alwaysGrow);
			grid.add(title, 0, 0, 2, 1);
			grid.addRow(1, new Label("Pattern: "), patternInput);
			grid.addRow(2, styleLabel, styleGrid);
			setCenter(grid);
		}
	}

	private class StyleGrid extends GridPane {
		private StyleGrid(Rule rule) {
			super(5, 5);

			ColumnConstraints alwaysGrow = new ColumnConstraints();
			ColumnConstraints neverGrow = new ColumnConstraints();
			alwaysGrow.setHgrow(Priority.ALWAYS);
			neverGrow.setHgrow(Priority.NEVER);
			getColumnConstraints().addAll(
					alwaysGrow,
					alwaysGrow,
					neverGrow);


			Button addStyle = new Button("Add property");
			addStyle.setOnAction(e -> {
				StyleProperty style = new StyleProperty("key", "value");
				rule.getStyle().add(style);
				addStyle(2, rule, style);
			});

			addRow(0, new Label("Property"), new Label("Value"));
			addRow(1, addStyle);

			for (StyleProperty style : rule.getStyle()) {
				int row = getRowCount();
				addStyle(row, rule, style);
			}
		}

		private void addStyle(int row, Rule rule, StyleProperty style) {
			Button delete = new Button("Delete");
			delete.setOnAction(e -> {
				rule.getStyle().remove(style);
				getChildren().removeIf(node -> GridPane.getRowIndex(node) == row);
				notifyChange();
			});
			TextField keyField = new TextField(style.getKey());
			TextField valueField = new TextField(style.getValue());
			keyField.setMaxWidth(Integer.MAX_VALUE);
			valueField.setMaxWidth(Integer.MAX_VALUE);
			keyField.getStyleClass().add("input");
			valueField.getStyleClass().add("input");
			keyField.textProperty().addListener((ob, old, cur) -> {
				style.setKey(cur);
				notifyChange();
			});
			valueField.textProperty().addListener((ob, old, cur) -> {
				style.setValue(cur);
				notifyChange();
			});
			addRow(row, keyField, valueField, delete);
		}
	}
}
