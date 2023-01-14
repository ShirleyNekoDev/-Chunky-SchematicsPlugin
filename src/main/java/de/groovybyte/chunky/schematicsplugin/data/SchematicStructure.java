package de.groovybyte.chunky.schematicsplugin.data;

import se.llbit.json.JsonValue;

import java.io.File;

public interface SchematicStructure {
	File getFile();

	JsonValue toJson();
}
