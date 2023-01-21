package de.groovybyte.chunky.schematicsplugin.gui.utils;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import se.llbit.fxutil.Dialogs;

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

	public static void setPadding(Pane pane, double padding) {
		pane.setPadding(new Insets(padding));
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

	public static Button newButton(
		String label,
		Image icon,
		EventHandler<ActionEvent> onAction
	) {
		Button button = new Button(label);
		if(icon != null) button.setGraphic(new ImageView(icon));
		button.setOnAction(onAction);
		return button;
	}

	public static ConfirmChoice confirm(
		Window window, String title, String contextText
	) {
		Alert alert = Dialogs.createAlert(Alert.AlertType.CONFIRMATION);
		alert.initOwner(window);
		alert.setTitle(title);
		alert.setContentText(contextText);
		if(alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent())
			return ConfirmChoice.CONFIRM;
		return ConfirmChoice.DENY;
	}

	public enum ConfirmChoice {
		CONFIRM, DENY;

		public void onConfirm(Runnable callback) {
			if(this == CONFIRM)
				callback.run();
		}
	}
}
