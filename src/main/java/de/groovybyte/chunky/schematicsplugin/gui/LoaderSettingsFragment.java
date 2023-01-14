package de.groovybyte.chunky.schematicsplugin.gui;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class LoaderSettingsFragment {

	private final VBox root;

	public LoaderSettingsFragment() {
		root = new VBox();
	}

	public Node getNode() {
		return root;
	}
}
