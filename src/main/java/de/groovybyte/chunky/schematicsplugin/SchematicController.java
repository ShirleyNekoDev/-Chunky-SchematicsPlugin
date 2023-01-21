package de.groovybyte.chunky.schematicsplugin;

import de.groovybyte.chunky.schematicsplugin.data.*;
import de.groovybyte.chunky.schematicsplugin.utils.EventHandler;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.renderer.ResetReason;
import se.llbit.chunky.renderer.SceneProvider;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SchematicController {
	private final EventHandler.Impl<SchematicChangedEvent> schematicChangeHandler = new EventHandler.Impl<>();

	private final Chunky chunky;
	public SceneProvider sceneProvider;
	private File lastSchematicDirectory = null;

	private LoadedSchematicStructure currentSchematic = null;

	public Optional<LoadedSchematicStructure> getCurrentSchematic() {
		return Optional.ofNullable(currentSchematic);
	}

	public SchematicController(Chunky chunky) {
		this.chunky = chunky;
		this.sceneProvider = chunky.getSceneManager().getSceneProvider();
		sceneProvider.addChangeListener((ResetReason resetReason, Scene scene) -> {
			if(resetReason == ResetReason.SCENE_LOADED) {
				onSceneLoaded(scene);
			}
		});
	}

	public EventHandler<SchematicChangedEvent> getSchematicChangeHandler() {
		return schematicChangeHandler;
	}


	// TODO: persist current "schematic" directory in PersistentSettings
	public File getSchematicDirectory() {
		if(currentSchematic != null) {
			return currentSchematic.getFile().getParentFile();
		} else {
			if(lastSchematicDirectory == null) {
				File initialDirectory = PersistentSettings.getLastWorld();
				if(initialDirectory != null && initialDirectory.isDirectory()) {
					lastSchematicDirectory = initialDirectory;
				}
			}
			return lastSchematicDirectory;
		}
	}

	private void setCurrentSchematic(LoadedSchematicStructure structure, boolean alignCamera) {
		try {
			currentSchematic = structure;
			sceneProvider.withSceneProtected(scene -> {
				ChunkySceneInjector.injectIntoScene(currentSchematic, scene, alignCamera);
				SchematicSceneSerializer.saveToScene(currentSchematic, scene);
				scene.refresh();
				lastSchematicDirectory = structure.getFile().getParentFile();

				Log.info("Loaded schematic \"" + currentSchematic.getFile() + "\"");
				schematicChangeHandler.handle(SchematicChangedEvent.loaded(currentSchematic));
			});
		} catch(Exception ex) {
			currentSchematic = null;
			schematicChangeHandler.handle(SchematicChangedEvent.loadingFailed(ex));
		}
	}

	public void loadSchematicFromFile(File file) {
		lastSchematicDirectory = file.getParentFile();
		sceneProvider.withSceneProtected(scene ->
			ChunkySceneInjector.resetScene(scene, chunky.getSceneFactory())
		);
		loadSchematic(new UnloadedSchematicStructure(file), true);
	}

	public void loadSchematic(SchematicStructure structure, boolean alignCamera) {
		if(structure instanceof LoadedSchematicStructure) {
			setCurrentSchematic((LoadedSchematicStructure) structure, alignCamera);
		} else if(structure instanceof UnloadedSchematicStructure) {
			UnloadedSchematicStructure unloaded = (UnloadedSchematicStructure) structure;
			loadSchematic(unloaded, unloaded::load, alignCamera);
		} else {
			throw new IllegalStateException();
		}
	}

	@FunctionalInterface
	interface Load {
		LoadedSchematicStructure load() throws IOException;
	}

	private void loadSchematic(
		UnloadedSchematicStructure tempStructure,
		Load load,
		boolean alignCamera
	) {
		try {
			schematicChangeHandler.handle(SchematicChangedEvent.loading(tempStructure));
			LoadedSchematicStructure structure = load.load();
			setCurrentSchematic(structure, alignCamera);
		} catch(IOException ex) {
			schematicChangeHandler.handle(SchematicChangedEvent.loadingFailed(ex));
		}
	}

	public void reloadSchematic() {
		loadSchematic(currentSchematic, currentSchematic::load, false);
	}

	public void closeSchematic() {
		if(currentSchematic != null) {
			currentSchematic = null;
			sceneProvider.withSceneProtected(scene -> ChunkySceneInjector.resetScene(scene,
				chunky.getSceneFactory()
			));
		}
		schematicChangeHandler.handle(SchematicChangedEvent.unloaded());
	}

	private void onSceneLoaded(Scene scene) {
		if(SchematicSceneSerializer.hasSchematic(scene)) {
			SchematicStructure schematic = SchematicSceneSerializer.loadFromScene(scene);
			if(currentSchematic == null || !schematic.getFile().equals(currentSchematic.getFile())) {
				loadSchematic(schematic, false);
				return;
			}
		}
		schematicChangeHandler.handle(SchematicChangedEvent.unloaded());
	}
}
