package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.SchematicController;
import de.groovybyte.chunky.schematicsplugin.formats.SchematicFormats;
import de.groovybyte.chunky.schematicsplugin.gui.utils.FXUtils.ConfirmChoice;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.groovybyte.chunky.schematicsplugin.gui.utils.FXUtils.confirm;

public class SchematicFXController {

	private final SchematicController schematicController;
	private final Supplier<Window> fxWindowSupplier;

	public SchematicFXController(
		SchematicController schematicController, Supplier<Window> fxWindowSupplier
	) {
		this.schematicController = schematicController;
		this.fxWindowSupplier = fxWindowSupplier;
	}

	public void openSchematic() {
		schematicController.getCurrentSchematic().map(structure -> confirm(fxWindowSupplier.get(),
			"Create new scene",
			"Do you really want to load a new schematic to replace your existing scene?"
		)).orElse(ConfirmChoice.CONFIRM).onConfirm(this::showOpenSchematicDialog);
	}

	public void closeSchematic() {
		schematicController.getCurrentSchematic().map(structure -> confirm(fxWindowSupplier.get(),
			"Create new scene",
			"Do you really want to reset and start with a fresh scene without schematic?"
		)).orElse(ConfirmChoice.CONFIRM).onConfirm(schematicController::closeSchematic);
	}

	public void reloadSchematic() {
		assert schematicController.getCurrentSchematic().isPresent();
		schematicController.reloadSchematic();
	}

	private void showOpenSchematicDialog() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Schematic File");
		fileChooser.setInitialDirectory(schematicController.getSchematicDirectory());

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All schematic formats",
			SchematicFormats.FORMATS.stream()
				.flatMap(format -> Arrays.stream(format.getExpectedFileExtensions()))
				.map(extension -> "*." + extension)
				.collect(Collectors.toList())
		));
		SchematicFormats.FORMATS.stream()
			.map(format -> new FileChooser.ExtensionFilter(format.getDescription(),
				Arrays.stream(format.getExpectedFileExtensions())
					.map(extension -> "*." + extension)
					.collect(Collectors.toList())
			))
			.forEach(fileChooser.getExtensionFilters()::add);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

		Optional.ofNullable(fileChooser.showOpenDialog(fxWindowSupplier.get()))
			.ifPresent(schematicController::loadSchematicFromFile);
	}
}
