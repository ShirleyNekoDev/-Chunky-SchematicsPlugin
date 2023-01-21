package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.gui.utils.ChunkyFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import se.llbit.chunky.world.Icon;

import java.util.List;
import java.util.Optional;

public class ChunkyFXInjector {

	public synchronized static void toggleToSchematicGUI(
		Scene fxScene, SchematicFXController schematicFXController
	) {
		if(!isInSchematicGUI()) {
			toggleBiomeColorsFeature(fxScene, false);
			toggleChunkReloadingButtons(fxScene, false, schematicFXController);
		}
	}

	private static boolean isInSchematicGUI() {
		return reloadSchematicButton != null && reloadSchematicButton.getParent() != null;
	}

	public synchronized static void toggleToChunkGUI(Scene fxScene) {
		if(!isInChunkGUI()) {
			toggleBiomeColorsFeature(fxScene, true);
			toggleChunkReloadingButtons(fxScene, true, null);
		}
	}

	private static boolean isInChunkGUI() {
		return reloadChunksButton == null || reloadChunksButton.getParent() != null;
	}

	private static CheckBox biomeColorsCheckbox = null;
	private static Label biomeColorWarningLabel = null;

	private static void toggleBiomeColorsFeature(Scene fxScene, boolean enabled) {
		if(biomeColorsCheckbox == null) {
			biomeColorsCheckbox = ChunkyFXUtils.findToolPaneCheckbox(fxScene, "biomeColors").orElse(null);
		}
		if(biomeColorsCheckbox != null) {
			if(enabled) {
				biomeColorsCheckbox.setDisable(false);
				biomeColorsCheckbox.getStyleClass().remove("disabled");
			} else {
				biomeColorsCheckbox.setDisable(true);
				biomeColorsCheckbox.getStyleClass().add("disabled");
			}
			Node parent = biomeColorsCheckbox.getParent();
			if(parent instanceof Pane) {
				List<Node> children = ((Pane) parent).getChildren();
				if(enabled) {
					if(children.get(0).equals(biomeColorWarningLabel)) {
						children.remove(biomeColorWarningLabel);
					}
				} else {
					if(!children.get(0).equals(biomeColorWarningLabel)) {
						if(biomeColorWarningLabel == null) {
							biomeColorWarningLabel = new Label("Schematics do not support biome colors.");
							biomeColorWarningLabel.setStyle(
								"-fx-text-fill: darkred; -fx-border-color: darkred; -fx-font-weight: bold;");
							biomeColorWarningLabel.setPadding(new Insets(6.0));
							biomeColorWarningLabel.getStyleClass().add("warningLabel");
						}
						children.add(0, biomeColorWarningLabel);
					}
				}
			}
		}
	}

	private static Button loadSelectedChunksButton = null;
	private static Button reloadChunksButton = null;
	private static TitledPane sceneYclipPane = null;

	private static Button reloadSchematicButton = null;
	private static Button exitSchematicGUIButton = null;
	private static Label schematicSceneWarningLabel = null;

	private static void toggleChunkReloadingButtons(
		Scene fxScene, boolean loadChunks, SchematicFXController schematicFXController
	) {
		if(loadChunks) {
			if(sceneYclipPane != null) {
				sceneYclipPane.setDisable(false);
			}

			if(schematicSceneWarningLabel != null && schematicSceneWarningLabel.getParent() != null) {
				((Pane) schematicSceneWarningLabel.getParent()).getChildren()
					.remove(schematicSceneWarningLabel);
			}
			Optional.ofNullable(reloadSchematicButton)
				.map(Node::getParent)
				.filter(parent -> parent instanceof HBox)
				.map(parent -> (HBox) parent)
				.ifPresent(hBox -> {
					hBox.getChildren().clear();
					if(loadSelectedChunksButton != null) {
						hBox.getChildren().add(loadSelectedChunksButton);
					}
					if(reloadChunksButton != null) {
						hBox.getChildren().add(reloadChunksButton);
					}
				});
		} else {
			loadSelectedChunksButton = ChunkyFXUtils.findToolPaneButton(fxScene, "loadSelectedChunks")
				.orElse(null);
			reloadChunksButton = ChunkyFXUtils.findToolPaneButton(fxScene, "reloadChunks").orElse(null);

			sceneYclipPane = ChunkyFXUtils.findToolPane(fxScene)
				.lookupAll("TitledPane")
				.stream()
				.filter(node -> node instanceof TitledPane)
				.map(node -> (TitledPane) node)
				.filter(titledPane -> titledPane.getText().equals("Scene Y clip"))
				.findFirst()
				.orElse(null);
			if(sceneYclipPane != null) {
				sceneYclipPane.setDisable(true);
				sceneYclipPane.setExpanded(false);
			}

			if(reloadSchematicButton == null) {
				reloadSchematicButton = new Button("Reload Schematic", new ImageView(Icon.reload.fxImage()));
				reloadSchematicButton.setOnAction(event -> schematicFXController.reloadSchematic());
			}
			if(exitSchematicGUIButton == null) {
				exitSchematicGUIButton = new Button("Exit Schematic Scene",
					new ImageView(Icon.clear.fxImage())
				);
				exitSchematicGUIButton.setOnAction(event -> schematicFXController.closeSchematic());
			}
			if(schematicSceneWarningLabel == null) {
				schematicSceneWarningLabel = new Label("Schematic Scene - see SchematicPlugin tab");
				schematicSceneWarningLabel.setStyle("-fx-border-color: lightgrey; -fx-font-weight: bold;");
				schematicSceneWarningLabel.setPadding(new Insets(6.0));
				schematicSceneWarningLabel.getStyleClass().add("warningLabel");
			}

			Optional.ofNullable(reloadChunksButton)
				.map(Node::getParent)
				.filter(parent -> parent instanceof HBox)
				.map(parent -> (HBox) parent)
				.ifPresent(hBox -> {
					hBox.getChildren().clear();
					hBox.getChildren().addAll(reloadSchematicButton, exitSchematicGUIButton);
					Pane parent = (Pane) hBox.getParent();
					if("schematicSceneWarningLabelBox".equals(parent.getId())) {
						VBox vBox = (VBox) parent;
						vBox.getChildren().add(0, schematicSceneWarningLabel);
					} else {
						int i = parent.getChildren().indexOf(hBox);
						VBox vBox = new VBox(6.0, schematicSceneWarningLabel);
						vBox.setId("schematicSceneWarningLabelBox");
						parent.getChildren().set(i, vBox);
						vBox.getChildren().add(hBox);
					}
				});
		}
	}
}
