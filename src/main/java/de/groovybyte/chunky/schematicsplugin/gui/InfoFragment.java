package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import de.groovybyte.chunky.schematicsplugin.data.SchematicChangedEvent;
import de.groovybyte.chunky.schematicsplugin.data.SchematicStructure;
import de.groovybyte.chunky.schematicsplugin.gui.utils.FXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;

public class InfoFragment {

	private final VBox root;
	private final TextField fileName = FXUtils.newOutputTextField();

	private final TitledPane details;

	public InfoFragment() {
		details = new TitledPane("Schematic Metadata", buildDetailsNode());
		details.setAnimated(false);
		details.setMinWidth(140.0);

		Label fileLabel = new Label("File:");
		HBox.setHgrow(fileName, Priority.ALWAYS);
		HBox fileBox = new HBox(4.0, fileLabel, fileName);
		fileBox.setAlignment(Pos.BASELINE_LEFT);

		root = new VBox(
			fileBox,
			details
		);
		FXUtils.addSpace(root, 10.0, 0.0);
		reset();
	}

	private final TextField format = FXUtils.newOutputTextField();
	private final TextField size = FXUtils.newOutputTextField();
	private final TextField blockPalette = FXUtils.newOutputTextField();
	private final TextField tileEntities = FXUtils.newOutputTextField();
	private final TextField entities = FXUtils.newOutputTextField();

	private Node buildDetailsNode() {
		GridPane detailsGrid = new GridPane();
		FXUtils.addSpace(detailsGrid, 6.0, 6.0, 10.0);
		detailsGrid.getColumnConstraints()
			.add(new ColumnConstraints(80.0));
		detailsGrid.getColumnConstraints()
			.add(new ColumnConstraints(80.0, 200.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));

        // TODO: camera offset
		// TODO: dataversion
		FXUtils.addLabeledTextFieldRow(detailsGrid, "Format:", format, 0);
		FXUtils.addLabeledTextFieldRow(detailsGrid, "Size:", size, 1);
		FXUtils.addLabeledTextFieldRow(detailsGrid, "Block Palette:", blockPalette, 2);
		FXUtils.addLabeledTextFieldRow(detailsGrid, "Tile Entities:", tileEntities, 3);
		FXUtils.addLabeledTextFieldRow(detailsGrid, "Entities:", entities, 4);

		return detailsGrid;
	}

	public Node getNode() {
		return root;
	}


	private void reset() {
		FXUtils.setTextAndTooltip(fileName, "nothing loaded");
		fileName.setDisable(true);
		details.setDisable(true);
		details.setExpanded(false);
	}

	public void onSchematicChanged(SchematicChangedEvent event) {
		if(!event.hasSchematic()) {
			reset();
		} else {
			SchematicStructure schematic = event.getSchematic();
			FXUtils.setTextAndTooltip(fileName, schematic.getFile().toString());
			fileName.setDisable(false);
			if(event.isLoaded()) {
				details.setDisable(false);
				details.setExpanded(true);
				LoadedSchematicStructure structure = (LoadedSchematicStructure) schematic;
				FXUtils.setTextAndTooltip(format, structure.format);
				FXUtils.setTextAndTooltip(size, String.format("%d ⨯ %d ⨯ %d", structure.size.x, structure.size.y, structure.size.z));
				FXUtils.setTextAndTooltip(blockPalette, Integer.toString(structure.blockPalette.getPalette().size()));
				FXUtils.setTextAndTooltip(tileEntities, Integer.toString(structure.tileEntities.size()));
				// TODO: count parsed in scene (count total)
				FXUtils.setTextAndTooltip(entities, Integer.toString(structure.entities.size()));
			}
		}
	}
}
