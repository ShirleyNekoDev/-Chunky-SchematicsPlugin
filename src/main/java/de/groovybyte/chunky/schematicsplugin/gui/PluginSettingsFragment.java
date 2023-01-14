package de.groovybyte.chunky.schematicsplugin.gui;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;


/*
 * Settings:
 * - inject into chunky gui
 * - save schematic structure in scene (vs safe file reference)
 * - biome to use
 */
public class PluginSettingsFragment {

	private final TitledPane root;

	public PluginSettingsFragment() {
		root = new TitledPane("Settings", new VBox(

		));
	}

	public Node getNode() {
		return root;
	}
}
