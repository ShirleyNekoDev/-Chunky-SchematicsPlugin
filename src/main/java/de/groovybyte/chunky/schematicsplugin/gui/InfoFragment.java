package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import de.groovybyte.chunky.schematicsplugin.data.SchematicStructure;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import se.llbit.chunky.world.Icon;

public class InfoFragment {

	private final VBox root;
	private final Button loadButton = new Button("Load Schematic");
	private final TextField fileName = FXUtils.newOutputTextField();

	private final TitledPane details;

	public InfoFragment(Runnable loadSchematicCallback) {
		loadButton.setOnAction(evt -> loadSchematicCallback.run());
		loadButton.setGraphic(new ImageView(Icon.load.fxImage()));

		details = new TitledPane("Schematic Metadata", buildDetailsNode());
		details.setAnimated(false);
		details.setMinWidth(140.0);

		Label fileLabel = new Label("File:");
		HBox.setHgrow(fileName, Priority.ALWAYS);
		HBox fileBox = new HBox(4.0, fileLabel, fileName);
		fileBox.setAlignment(Pos.BASELINE_LEFT);

		root = new VBox(
			loadButton,
			fileBox,
			details
		);
		FXUtils.addSpace(root, 10.0, 0.0);
		update(null);
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

	public void update(SchematicStructure schematic) {
		if(schematic == null) {
			FXUtils.setTextAndTooltip(fileName, "nothing loaded");
			fileName.setDisable(true);
			details.setDisable(true);
			details.setExpanded(false);
		} else {
			FXUtils.setTextAndTooltip(fileName, schematic.getFile().toString());
			fileName.setDisable(false);
			if(schematic instanceof LoadedSchematicStructure) {
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
