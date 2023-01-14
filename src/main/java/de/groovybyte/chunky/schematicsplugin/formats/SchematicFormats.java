package de.groovybyte.chunky.schematicsplugin.formats;

import java.util.HashSet;
import java.util.Set;

public class SchematicFormats {
	public final static Set<SchematicFormat> FORMATS = new HashSet<>();

	static {
		FORMATS.add(new MCEditFormat());
		FORMATS.add(new SpongeFormat());
	}

	// TODO
}
