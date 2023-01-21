package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.SchematicController;
import de.groovybyte.chunky.schematicsplugin.data.SchematicChangedEvent;
import de.groovybyte.chunky.schematicsplugin.gui.utils.ChunkyFXUtils;
import de.groovybyte.chunky.schematicsplugin.gui.utils.FXUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.*;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.controller.ChunkyFxController;
import se.llbit.chunky.ui.controller.RenderControlsFxController;
import se.llbit.chunky.ui.render.RenderControlsTab;
import se.llbit.chunky.world.Icon;
import se.llbit.log.Log;

public class PluginTab implements RenderControlsTab {

	public final SchematicController schematicController;
	public final SchematicFXController schematicFXController;
	private ChunkyFxController chunkyFxController;

	private final VBox root;
	public final InfoFragment infoFragment;

	public PluginTab(Chunky chunky) {
		schematicController = new SchematicController(chunky);
		schematicFXController = new SchematicFXController(
			schematicController,
			() -> getFxScene().getWindow()
		);

		VBox loadButtonBar = new VBox(
			10.0,
			FXUtils.newButton(
				"Open schematic file",
				Icon.load.fxImage(),
				evt -> schematicFXController.openSchematic()
			)
		);
		infoFragment = new InfoFragment();
		root = new VBox(
			loadButtonBar,
			infoFragment.getNode()
		);
		root.setFillWidth(true);
		root.setPrefWidth(410.0);
		FXUtils.addSpace(root, 10.0, 10.0);

		schematicController.getSchematicChangeHandler().addEventListener(
			this::onSchematicChanged
		);
		schematicController.getSchematicChangeHandler().addEventListener(
			infoFragment::onSchematicChanged
		);
		schematicController.getSchematicChangeHandler().addEventListener(event -> {
			System.out.println(event);
			System.out.println();
		});
	}

	private javafx.scene.Scene getFxScene() {
		return root.getScene();
	}

	public void onSchematicChanged(SchematicChangedEvent event) {
		if(event.hasLoadingFailed() || event.hasUnloaded()) {
			Platform.runLater(() -> {
				ChunkyFXInjector.toggleToChunkGUI(getFxScene());
				chunkyFxController.refreshSettings();
				ChunkyFXUtils.selectTab(getFxScene(), "mainTabs", "worldMapTab");
			});
			if(event.hasLoadingFailed()) {
				Exception exception = event.getLoadException();
				exception.printStackTrace();
				Log.error("Failed to load schematic: " + exception.getMessage(), exception);
			}
		} else if(event.isLoaded()) {
			Platform.runLater(() -> {
				ChunkyFXInjector.toggleToSchematicGUI(getFxScene(), schematicFXController);
				chunkyFxController.getChunkSelection().clearSelection();
				ChunkyFXUtils.selectTab(getFxScene(), "mainTabs", "previewTab");
			});
		}
	}

	@Override
	public String getTabTitle() {
		return "Schematic Plugin";
	}

	@Override
	public Node getTabContent() {
		// hack to not get resized by ToolTabSkin::layoutChildren (broken with SplitPane)
		return new Pane(root);
	}

	@Override
	public void setController(RenderControlsFxController controls) {
		chunkyFxController = controls.getChunkyController();
	}

	@Override
	public void update(Scene scene) {
		// on scene update when the tab is open
	}
}
