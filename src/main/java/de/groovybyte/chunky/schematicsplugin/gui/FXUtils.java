package de.groovybyte.chunky.schematicsplugin.gui;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FXUtils {

	public static void onMounted(Node node, Runnable callback) {
		final ReadOnlyObjectProperty<Scene> sceneProperty = node.sceneProperty();
		sceneProperty.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				callback.run();
				sceneProperty.removeListener(this);
			}
		});
	}

	public static void addSpace(VBox pane, double spacing, double padding) {
		pane.setSpacing(spacing);
		pane.setPadding(new Insets(padding));
	}

	public static void addSpace(GridPane pane, double horizontalGap, double verticalGap, double padding) {
		pane.setHgap(horizontalGap);
		pane.setVgap(verticalGap);
		pane.setPadding(new Insets(padding));
	}

	public static void setTextAndTooltip(TextField textField, String text) {
		if(text != null) {
			textField.setText(text);
			if(textField.getTooltip() == null) {
				textField.setTooltip(new Tooltip(text));
			} else {
				textField.getTooltip().setText(text);
			}
		} else {
			textField.setText("");
			textField.setTooltip(null);
		}
	}

	public static void addLabeledTextFieldRow(GridPane gridPane, String label, TextField textField, int rowIndex) {
		gridPane.add(new Label(label), 0, rowIndex);
		gridPane.add(textField, 1, rowIndex);
	}

	public static TextField newOutputTextField() {
		TextField textField = new TextField();
		textField.setEditable(false);
		textField.getStyleClass().add("outputField");
		return textField;
	}
}
