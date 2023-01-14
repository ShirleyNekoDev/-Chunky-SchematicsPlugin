package de.groovybyte.chunky.schematicsplugin.gui;

import javafx.scene.Node;
import javafx.scene.layout.*;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.controller.RenderControlsFxController;
import se.llbit.chunky.ui.render.RenderControlsTab;

public class PluginTab implements RenderControlsTab {

	private SchematicController schematicController;

	public PluginTab(Chunky chunky) {
		buildGUI();
		schematicController = new SchematicController(
			chunky,
			root::getScene,
			infoFragment::update
		);
	}

	private VBox root;
	private final InfoFragment infoFragment = new InfoFragment(() -> schematicController.loadSchematic());
//	private LoaderSettingsFragment loaderSettingsFragment = new LoaderSettingsFragment();
//	private PluginSettingsFragment pluginSettingsFragment = new PluginSettingsFragment();

	private void buildGUI() {
		root = new VBox(
			infoFragment.getNode()
//			new Separator(),
//			loaderSettingsFragment.getNode(),
//			new Separator(),
//			pluginSettingsFragment.getNode()
		);
		root.setFillWidth(true);
		root.setPrefWidth(410.0);
		FXUtils.addSpace(root, 10.0, 10.0);
	}

	@Override
	public void update(Scene scene) {
		// TODO(Chunky): tabs not getting notified when a scene is loaded (after another scene has been loaded?)
		schematicController.updateScene(scene);
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
		schematicController.chunkyFxController = controls.getChunkyController();
	}
}
