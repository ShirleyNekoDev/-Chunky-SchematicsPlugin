package de.groovybyte.chunky.schematicsplugin.formats;

import java.io.IOException;

public class UnsupportedSchematicFormatException extends IOException {

	public UnsupportedSchematicFormatException() {
		super();
	}

	public UnsupportedSchematicFormatException(String message) {
		super(message);
	}
}
