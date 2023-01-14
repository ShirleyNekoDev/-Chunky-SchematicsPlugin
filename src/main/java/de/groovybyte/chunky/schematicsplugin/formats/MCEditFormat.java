package de.groovybyte.chunky.schematicsplugin.formats;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import se.llbit.chunky.block.BlockSpec;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static de.groovybyte.chunky.schematicsplugin.formats.SchematicFormatUtils.*;

/**
 * MCEdit Schematic Format
 */
public class MCEditFormat implements SchematicFormat {

	@Override
	public String getDescription() {
		return "MCEdit schematic";
	}

	public String[] getExpectedFileExtensions() {
		return new String[] {
			"schematic"
		};
	}

	@Override
	public LoadedSchematicStructure load(File file) throws IOException {
		try(
			DataInputStream in = new DataInputStream(
				new GZIPInputStream(Files.newInputStream(file.toPath()))
			)
		) {
			Tag namedRoot = NamedTag.read(in);
			if(!namedRoot.isNamed("Schematic")) {
				throw new UnsupportedSchematicFormatException();
			}

			CompoundTag root = namedRoot.asCompound();
			String materials = root.get("Materials").stringValue();
			switch(materials) {
				case "Alpha":
					return loadAlphaLevelFormat(root);
				case "Classic":
				case "Pocket":
					throw new UnsupportedSchematicFormatException("Unsupported materials format \"" + materials + "\"");
				default:
					throw new UnsupportedSchematicFormatException("Unknown materials format \"" + materials + "\"");
			}
		}
	}

	private LoadedSchematicStructure loadAlphaLevelFormat(CompoundTag root) {
		LoadedSchematicStructure structure = defaultStructure(root);
		structure.format = getDescription() + " [Alpha]";
		structure.blockPalette.unsynchronize();
		Map<Short, Integer> idMapping = new HashMap<>();

		// ID mapping for the version this schematic was saved in, used by Schematica.
		// Provided only for materials used in the schematic.
		// [name]: Indicates that name has the given ID
		// (e.g. [name] being minecraft:stone and the value being 1).
		CompoundTag schematicaMapping = root.get("SchematicaMapping").asCompound();
		if(!schematicaMapping.isError()) {
			for(NamedTag tag : schematicaMapping) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.add(new NamedTag("Name", new StringTag(tag.name)));

				short schematicBlockId = tag.getTag().shortValue();
				int paletteBlockId = structure.blockPalette.put(new BlockSpec(compoundTag));
				idMapping.put(schematicBlockId, paletteBlockId);
			}
		}

		// Block IDs defining the terrain. 1 Byte per block.
		// Sorted by:
		//  - height (bottom to top)
		//  - then length
		//  - then width
		// The index of the block at X,Y,Z is (Y×length + Z)×width + X.
		byte[] blocks = root.get("Blocks").byteArray();

		// Block data additionally defining parts of the terrain.
		// Only the lower 4 bits of each byte are used.
		// (Unlike in the chunk format, the block data in
		// the schematic format occupies a full byte per block.)
		byte[] data = root.get("Data").byteArray();

		for(int y = 0; y < structure.size.y; y++) {
			for(int z = 0; z < structure.size.z; z++) {
				for(int x = 0; x < structure.size.x; x++) {
					int i = (y * structure.size.z + z) * structure.size.x + x;
					byte blockId = blocks[i];
					byte blockData = data[i];
					short id = (short) (((blockId&0xff)<<8)|(blockData&0xf));
					if(!idMapping.containsKey(id)) {
						Tag blockTag = getLegacyBlockTag(blockId, blockData);
						int blockPaletteId = structure.blockPalette.put(new BlockSpec(blockTag));
						idMapping.put(id, blockPaletteId);
					}
					structure.blocks[i] = idMapping.get(id);
				}
			}
		}

		/*
		// TODO: https://github.com/ammaraskar/worldedit/blob/master/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java#L114
		// Extra bits that can be used to further define terrain; optional.
		// Two nibbles (4 bits) are put into each index in this array.
		// Unlike normal chunks, even indexes go on the high nibble
		// and odd indexes go on the low nibble.
		Tag additionalDataTag = root.get("AddBlocks");
		byte[] additionalBlocks = new byte[0];
		if(!additionalDataTag.isError()) {
			byte[] additionalBlocksCompacted = additionalDataTag.byteArray();
			additionalBlocks = new byte[additionalBlocksCompacted.length * 2];
			for(int i = 0; i < additionalBlocksCompacted.length; i++) {
				additionalBlocks[i * 2 + 0] = (byte) ((additionalBlocksCompacted[i]>>4)&0xF);
				additionalBlocks[i * 2 + 1] = (byte) (additionalBlocksCompacted[i]&0xF);
			}
		} else {
			// legacy format
			additionalDataTag = root.get("Add");
			if(!additionalDataTag.isError()) {
				additionalBlocks = additionalDataTag.byteArray();
			}
		}
		for(int i = 0; i < additionalBlocks.length; i++) {
			System.out.println(additionalBlocks[i]&0xff);
		}
		*/

		structure.entities.addAll(
			readEntities(structure, root).collect(Collectors.toList())
		);
		structure.tileEntities.addAll(
			readTileEntities(structure, "TileEntities", root).collect(Collectors.toList())
		);

		structure.readOffset(root);

		return structure;
	}
}
