package de.groovybyte.chunky.schematicsplugin.formats;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import se.llbit.chunky.block.BlockSpec;
import se.llbit.chunky.chunk.BlockPalette;
import se.llbit.nbt.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import static de.groovybyte.chunky.schematicsplugin.formats.SchematicFormatUtils.*;

/**
 * Sponge Schematic format
 * - see <a href="https://github.com/SpongePowered/Schematic-Specification">Specification</a>
 */
public class SpongeFormat implements SchematicFormat {

	@Override
	public String getDescription() {
		return "Sponge schematic";
	}

	public String[] getExpectedFileExtensions() {
		return new String[] {
			"schem"
		};
	}

	@Override
	public LoadedSchematicStructure load(File file) throws IOException {
		try(
			DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(file.toPath())))
		) {
			Tag namedRoot = NamedTag.read(in);
			if(!namedRoot.isNamed("Schematic")) {
				throw new UnsupportedSchematicFormatException();
			}

			CompoundTag root = namedRoot.asCompound();
			int version = root.get("Version").intValue();
			switch(version) {
				case 1:
					return loadFormatVersion1(root);
				case 2:
					return loadFormatVersion2(root);
				case 3:
					return loadFormatVersion3(root);
				default:
					throw new UnsupportedSchematicFormatException("Unknown format version \"" + version + "\"");
			}
		}
	}

	private Map<Integer, Integer> readPalette(BlockPalette blockPalette, String key, CompoundTag root) throws IOException {
		CompoundTag palette = root.get(key).asCompound();
		if(palette.isEmpty()) {
			throw new UnsupportedSchematicFormatException("Unsupported format: missing block palette");
		}
		Map<Integer, Integer> idMapping = new ConcurrentHashMap<>(palette.size());
		StreamSupport.stream(palette.spliterator(), true).forEach(namedTag -> {
			int id = namedTag.tag.intValue();
			if(!idMapping.containsKey(id)) {
				String blockSpecString = namedTag.name;
				Tag blockTag = buildBlockTag(blockSpecString);
				int blockPaletteId = blockPalette.put(new BlockSpec(blockTag));
				idMapping.put(id, blockPaletteId);
			}
		});
		blockPalette.unsynchronize();
		return idMapping;
	}

	/**
	 * Each integer is bitpacked into a single byte with varint encoding.
	 * The first byte determines the length of the integer with a
	 * maximum length of 5 (for a 32 bit number), and depending on the length,
	 * each proceeding byte is or'ed and current value bit shifted by
	 * the length multiplied by 7.
	 *
	 * https://github.com/SpongePowered/Sponge/blob/aa2c8c53b4f9f40297e6a4ee281bee4f4ce7707b/src/main/java/org/spongepowered/common/data/persistence/SchematicTranslator.java#L147-L175
	 * https://github.com/SpongePowered/Sponge/blob/aa2c8c53b4f9f40297e6a4ee281bee4f4ce7707b/src/main/java/org/spongepowered/common/data/persistence/SchematicTranslator.java#L230-L251
	 */
	void readVarIntArray(byte[] bytes) {

	}

	private void readBlocks(
		LoadedSchematicStructure structure,
		Map<Integer, Integer> idMapping,
		CompoundTag root
	) throws IOException {
		// Specifies the main storage array which contains Width * Height * Length entries.
		// Each entry is specified as a varint and refers to an index within the Palette.
		// The entries are indexed by x + z * Width + y * Width * Length.
		byte[] blockData = root.get("BlockData").byteArray();

		// TODO: varint decoding
		if(structure.blockPalette.getPalette().size() > 255) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		for(int y = 0; y < structure.size.y; y++) {
			for(int z = 0; z < structure.size.z; z++) {
				for(int x = 0; x < structure.size.x; x++) {
					int i = (y * structure.size.z + z) * structure.size.x + x;
					byte blockId = blockData[i];
					structure.blocks[i] = idMapping.get(blockId&0xff);
				}
			}
		}
	}

	/**
	 * <a
	 * href="https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-1.md">
	 * Format V1</a>
	 * 2016-08-23 - 2019-05-08
	 */
	private LoadedSchematicStructure loadFormatVersion1(CompoundTag root) throws IOException {
		LoadedSchematicStructure structure = defaultStructure(root);
		structure.format = getDescription() + " V1";

		Map<Integer, Integer> idMapping = readPalette(structure.blockPalette, "Palette", root);
		readBlocks(structure, idMapping, root);

		structure.tileEntities.addAll(
			readTileEntities(structure, "TileEntities", root).collect(Collectors.toList())
		);

		CompoundTag meta = root.get("Metadata").asCompound();
		structure.readOffset(meta);

		return structure;
	}

	/**
	 * <a
	 * href="https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-2.md">
	 * Format V2</a>
	 * 2019-05-08 - 2021-05-04
	 */
	private LoadedSchematicStructure loadFormatVersion2(CompoundTag root) throws IOException {
		LoadedSchematicStructure structure = loadFormatVersion1(root);
		structure.format = getDescription() + " V2";

		structure.dataVersion = root.get("DataVersion").intValue();

		structure.tileEntities.addAll(
			readTileEntities(structure, "BlockEntities", root).collect(Collectors.toList())
		);

		structure.entities.addAll(
			readEntities(structure, root).collect(Collectors.toList())
		);

//		CompoundTag biomePalette = root.get("BiomePalette").asCompound();
//		StreamSupport.stream(biomePalette.spliterator(), true)
//			.forEach(namedTag -> {
//				Biome biome = Biomes.biomesByResourceLocation.get(namedTag.name);
//				System.out.println(biome);
//			});
		// TODO: read BiomePalette / BiomeData
		// Specifies the main storage array which contains Width * Length entries for Biomes at positions.
		// Each entry is specified as a varint and refers to an index within BiomePalette.
		// The entries are indexed by x + z * Width. Biomes occupy the full vertical column in regions.

		return structure;
	}

	/**
	 * <a
	 * href="https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-3.md">
	 * Format V3</a>
	 * 2021-05-04 - now
	 */
	private LoadedSchematicStructure loadFormatVersion3(CompoundTag root) throws IOException {
		LoadedSchematicStructure structure = defaultStructure(root);
		structure.format = getDescription() + " V3";
		structure.dataVersion = root.get("DataVersion").intValue();

		CompoundTag blockContainer = root.get("Blocks").asCompound();
		Map<Integer, Integer> idMapping = readPalette(structure.blockPalette, "Palette", blockContainer);
		readBlocks(structure, idMapping, blockContainer);
		structure.tileEntities.addAll(
			readTileEntities(structure, "BlockEntities", blockContainer).collect(Collectors.toList())
		);

		structure.entities.addAll(
			readEntities(structure, root).collect(Collectors.toList())
		);

		CompoundTag biomesContainer = root.get("Biomes").asCompound();
		// TODO: read biome Palette / 3d Data
		// Specifies the main storage array which contains Width * Height * Length entries for Biomes at positions.
		// Each entry is specified as a varint and refers to an index within the Palette.
		// The entries are indexed by x + z * Width + y * Width * Length.

		return structure;
	}

	private Tag buildBlockTag(String nameWithProperties) {
		CompoundTag block = new CompoundTag();
		int propertyIndex = nameWithProperties.indexOf('[');
		String name;
		CompoundTag properties = new CompoundTag();
		if(propertyIndex < 0) {
			name = nameWithProperties;
		} else {
			name = nameWithProperties.substring(0, propertyIndex);
			String[] propertyStrings = nameWithProperties
				.substring(propertyIndex + 1, nameWithProperties.length() - 1)
				.split(",");
			for(String property : propertyStrings) {
				int valueIndex = property.indexOf('=');
				String propertyName = property.substring(0, valueIndex);
				String propertyValue = property.substring(valueIndex + 1);
				try {
					int value = Integer.parseInt(propertyValue);
					properties.add(propertyName, new IntTag(value));
				} catch(NumberFormatException ex) {
					// booleans are also strings
					properties.add(propertyName, new StringTag(propertyValue));
				}
			}
		}
		block.add("Name", new StringTag(name));
		if(!properties.isEmpty()) {
			block.add("Properties", properties);
		}
		return block;
	}
}
