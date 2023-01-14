package de.groovybyte.chunky.schematicsplugin.data;

import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.json.JsonObject;

import java.io.File;
import java.io.IOException;

public class SchematicSceneSerializer {
	public static final String SCENE_DATA_KEY = "SchematicPlugin";

	public static void saveToScene(LoadedSchematicStructure structure, Scene scene) {
		JsonObject root = new JsonObject(2);
		root.add("Version", 1);
		root.add("Schematic", structure.toJson());
		scene.setAdditionalData(SCENE_DATA_KEY, root);
	}

	public static LoadedSchematicStructure loadFromFile(File file) throws IOException {
		return new UnloadedSchematicStructure(file).load();
	}

	public static SchematicStructure loadFromScene(Scene scene) {
		JsonObject root = scene.getAdditionalData(SCENE_DATA_KEY).asObject();
		int version = root.get("Version").intValue(0);
		switch(version) {
			case 0:
				throw new IllegalStateException("Cannot open schematic from scene - data is corrupted");
			case 1:
				return loadVersion1(root);
			default:
				throw new UnsupportedOperationException(
					"Cannot open schematic from scene - unknown version \"" + version + "\"");
		}
	}

	private static UnloadedSchematicStructure loadVersion1(JsonObject root) {
		JsonObject schematic = root.get("Schematic").asObject();
		return UnloadedSchematicStructure.fromJson(schematic);
	}

	public static boolean hasSchematic(Scene scene) {
		if(scene == null)
			return false;
		return !scene.getAdditionalData(SCENE_DATA_KEY).isUnknown();
	}
}
