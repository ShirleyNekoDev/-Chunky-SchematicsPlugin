package de.groovybyte.chunky.schematicsplugin.formats;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public interface SchematicFormat {
	String getDescription();

	String[] getExpectedFileExtensions();

	default boolean maybeCanLoad(File file) {
		String fileName = file.getName();
		int fileExtensionIndex = fileName.lastIndexOf('.');
		String fileExtension = fileName.substring(fileExtensionIndex + 1);
		return Arrays.stream(getExpectedFileExtensions())
			.anyMatch(fileExtension::equalsIgnoreCase);
	}

	LoadedSchematicStructure load(File file) throws IOException;
}
