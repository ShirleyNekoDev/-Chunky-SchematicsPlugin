package de.groovybyte.chunky.schematicsplugin.data;

import de.groovybyte.chunky.schematicsplugin.formats.SchematicFormat;
import de.groovybyte.chunky.schematicsplugin.formats.SchematicFormats;
import se.llbit.json.JsonObject;

import java.io.File;
import java.io.IOException;

public class UnloadedSchematicStructure implements SchematicStructure {
	public File file;

	public UnloadedSchematicStructure(File file) {
		this.file = file;
	}

	@Override
	public File getFile() {
		return file;
	}

	public LoadedSchematicStructure load() throws IOException {
		LoadedSchematicStructure structure = null;
		IOException lastException = null;
		for(SchematicFormat format : SchematicFormats.FORMATS) {
			if(format.maybeCanLoad(file)) {
				try {
					structure = format.load(file);
					if(structure != null) break;
				} catch(IOException ex) {
					lastException = ex;
					// continue
				} catch(Exception ex) {
					ex.printStackTrace();
					// continue
				}
			}
		}
		if(structure == null) {
			if(lastException != null) {
				throw lastException;
			}
			throw new IOException("File is not a supported schematic");
		}
		structure.file = file;
		return structure;
	}

	@Override
	public JsonObject toJson() {
		JsonObject root = new JsonObject(1);
		root.add("File", file.toString());
		return root;
	}

	public static UnloadedSchematicStructure fromJson(JsonObject root) {
		return new UnloadedSchematicStructure(
			new File(
				root.get("File").stringValue(null)
			)
		);
	}

	@Override
	public String toString() {
		return String.format(
			"SchematicStructure{unloaded, file=%s}",
			file
		);
	}
}
