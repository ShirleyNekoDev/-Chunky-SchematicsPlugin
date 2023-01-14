package de.groovybyte.chunky.schematicsplugin.gui;

import de.groovybyte.chunky.schematicsplugin.ChunkySceneInjector;
import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import de.groovybyte.chunky.schematicsplugin.data.SchematicSceneSerializer;
import de.groovybyte.chunky.schematicsplugin.data.SchematicStructure;
import de.groovybyte.chunky.schematicsplugin.data.UnloadedSchematicStructure;
import de.groovybyte.chunky.schematicsplugin.formats.SchematicFormats;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.ui.controller.ChunkyFxController;
import se.llbit.fxutil.Dialogs;
import se.llbit.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SchematicController {
	private final Supplier<javafx.scene.Scene> fxSceneSupplier;
	private final Consumer<SchematicStructure> structureChangedCallback;

	private final Chunky chunky;
	public ChunkyFxController chunkyFxController;
	public File lastSchematicDirectory = null;

	public Scene currentScene = null;
	public LoadedSchematicStructure currentSchematic = null;

	public SchematicController(
		Chunky chunky,
		Supplier<javafx.scene.Scene> fxSceneSupplier,
		Consumer<SchematicStructure> structureChangedCallback
	) {
		this.chunky = chunky;
		this.fxSceneSupplier = fxSceneSupplier;
		this.structureChangedCallback = structureChangedCallback;
	}

	private javafx.scene.Scene scene = null;
	private javafx.scene.Scene getFxScene() {
		if(scene == null) {
			scene = fxSceneSupplier.get();
		}
		return scene;
	}

	public void loadSchematic() {
		if(currentSchematic != null) {
			Alert alert = Dialogs.createAlert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Create new scene");
			alert.setContentText("Do you really want to load a new schematic to replace your existing scene?");
			alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).ifPresent(bt -> {
				currentSchematic = null;
				showLoadSchematicDialog();
			});
		} else {
			showLoadSchematicDialog();
		}
	}

	private void showLoadSchematicDialog() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Schematic File");
		if(currentSchematic != null) {
			fileChooser.setInitialDirectory(currentSchematic.getFile().getParentFile());
		} else {
			if(lastSchematicDirectory == null) {
				File initialDirectory = PersistentSettings.getLastWorld();
				if(initialDirectory != null && initialDirectory.isDirectory()) {
					lastSchematicDirectory = initialDirectory;
				}
			}
			fileChooser.setInitialDirectory(lastSchematicDirectory);
		}
		// TODO: persist current "schematic" directory in PersistentSettings

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
			"All schematic formats",
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

		File file = fileChooser.showOpenDialog(getFxScene().getWindow());
		if(file != null) {
			lastSchematicDirectory = file.getParentFile();
			ChunkySceneInjector.resetScene(currentScene, chunky.getSceneFactory());
			loadSchematic(new UnloadedSchematicStructure(file), true);
		}
	}

	public void loadSchematic(SchematicStructure structure, boolean alignCamera) {
		if(structure instanceof LoadedSchematicStructure) {
			currentSchematic = (LoadedSchematicStructure) structure;
		} else if(structure instanceof UnloadedSchematicStructure) {
			structureChangedCallback.accept(structure);
			Log.info("Loading schematic \"" + structure.getFile() + "\"");
			try {
				currentSchematic = ((UnloadedSchematicStructure) structure).load();
			} catch(IOException ex) {
				Log.error("Failed to load schematic: " + ex.getMessage(), ex);
				ex.printStackTrace();
				structureChangedCallback.accept(null);
				return;
			}
		} else {
			throw new IllegalStateException();
		}
		Log.info("Loaded schematic \"" + currentSchematic.getFile() + "\"");
		structureChangedCallback.accept(currentSchematic);

		ChunkyFXInjector.toggleToSchematicGUI(getFxScene(), this);
		if(!ChunkySceneInjector.injectIntoScene(currentSchematic, currentScene, alignCamera)) {
			currentSchematic = null;
			structureChangedCallback.accept(null);
			Platform.runLater(() -> {
				ChunkyFXInjector.toggleToChunkGUI(getFxScene());
			});
		} else {
			SchematicSceneSerializer.saveToScene(currentSchematic, currentScene);
			lastSchematicDirectory = structure.getFile().getParentFile();

			Platform.runLater(() -> {
				chunkyFxController.getChunkSelection().clearSelection();
				ChunkyFXUtils.selectTab(getFxScene(), "mainTabs", "previewTab");
				currentScene.refresh();
			});
		}
	}

	public void reloadSchematic() {
		try {
			currentSchematic = currentSchematic.load();
		} catch(IOException ex) {
			Log.error("Failed to load schematic: " + ex.getMessage(), ex);
			ex.printStackTrace();
			structureChangedCallback.accept(null);
			return;
		}
		Log.info("Loaded schematic \"" + currentSchematic.getFile() + "\"");
		structureChangedCallback.accept(currentSchematic);

		ChunkySceneInjector.injectIntoScene(currentSchematic, currentScene, false);
		SchematicSceneSerializer.saveToScene(currentSchematic, currentScene);

		Platform.runLater(() -> {
			currentScene.refresh();
		});
	}

	public void closeSchematic() {
		if(currentSchematic != null) {
			Alert alert = Dialogs.createAlert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Create new scene");
			alert.setContentText("Do you really want to reset and start with a fresh scene without schematic?");
			alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).ifPresent(bt -> {
				currentSchematic = null;
				ChunkyFXInjector.toggleToChunkGUI(getFxScene());
				ChunkySceneInjector.resetScene(currentScene, chunky.getSceneFactory());
				chunkyFxController.refreshSettings(); // this will trigger this::update
				ChunkyFXUtils.selectTab(getFxScene(), "mainTabs", "worldMapTab");
			});
		} else {
			ChunkyFXInjector.toggleToChunkGUI(getFxScene());
		}
	}

	public void updateScene(Scene scene) {
		currentScene = scene;
		if(SchematicSceneSerializer.hasSchematic(scene)) {
			SchematicStructure schematic = SchematicSceneSerializer.loadFromScene(scene);
			if(currentSchematic == null || !schematic.getFile().equals(currentSchematic.getFile())) {
				loadSchematic(schematic, false);
				return;
			}
		}
		ChunkyFXInjector.toggleToChunkGUI(getFxScene());
		structureChangedCallback.accept(null);
	}
}
