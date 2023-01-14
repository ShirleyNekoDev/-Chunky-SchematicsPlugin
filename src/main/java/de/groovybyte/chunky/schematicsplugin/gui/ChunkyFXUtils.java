package de.groovybyte.chunky.schematicsplugin.gui;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.llbit.fx.ToolPane;

import java.util.Objects;
import java.util.Optional;

public class ChunkyFXUtils {

	private static ToolPane renderControls;

	public static ToolPane findToolPane(Scene fxScene) {
		if(renderControls == null) {
			renderControls = Objects.requireNonNull((ToolPane) fxScene.lookup("#renderControls"));
		}
		return renderControls;
	}

	public static Optional<Node> findParentOfType(Node node, Class<?> clazz, int maxHeight) {
		if(node != null) {
			Node parent = node.getParent();
			for(int i = 1; i <= maxHeight; i++) {
				if(clazz.isInstance(parent)) {
					return Optional.of(parent);
				}
				parent = parent.getParent();
			}
		}
		return Optional.empty();
	}

	public static Optional<CheckBox> findToolPaneCheckbox(Scene fxScene, String id) {
		Node node = findToolPane(fxScene).lookup("#" + id);
		if(node instanceof CheckBox) {
			return Optional.of((CheckBox) node);
		}
		return Optional.empty();
	}

	public static Optional<Button> findToolPaneButton(Scene fxScene, String id) {
		Node node = findToolPane(fxScene).lookup("#" + id);
		if(node instanceof Button) {
			return Optional.of((Button) node);
		}
		return Optional.empty();
	}

	public static Tab selectTab(Scene fxScene, String tabPaneId, String tabId) {
		TabPane tabPane = (TabPane) fxScene.lookup("#" + tabPaneId);
		Tab tabToSelect = tabPane.getTabs()
			.stream()
			.filter(tab -> tab.getId().equals(tabId))
			.findFirst()
			.get();
		tabPane.getSelectionModel().select(tabToSelect);
		return tabToSelect;
	}
}
