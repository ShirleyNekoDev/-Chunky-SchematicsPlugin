package de.groovybyte.chunky.schematicsplugin.formats;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import se.llbit.chunky.block.legacy.LegacyBlocks;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.Tag;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class SchematicFormatUtils {

	private final static Method getTag;

	static {
		try {
			getTag = LegacyBlocks.class.getDeclaredMethod("getTag", int.class, int.class);
			getTag.setAccessible(true);
		} catch(ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Tag getLegacyBlockTag(byte blockId, byte blockData) {
		try {
			return (Tag) getTag.invoke(null, blockId&0xff, blockData&0xf);
		} catch(ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static double[] readDoublePosition(CompoundTag tag, String key) {
		ListTag listTag = tag.get(key).asList();
		return new double[]{
			listTag.get(0).doubleValue(tag.get("x").doubleValue()),
			listTag.get(1).doubleValue(tag.get("y").doubleValue()),
			listTag.get(2).doubleValue(tag.get("z").doubleValue())
		};
	}

	public static int[] readIntPosition(CompoundTag tag, String key) {
		return tag.get(key)
			.intArray(new int[]{
				tag.get("x").intValue(),
				tag.get("y").intValue(),
				tag.get("z").intValue()
			});
	}

	public static LoadedSchematicStructure defaultStructure(CompoundTag root) {
		LoadedSchematicStructure structure = new LoadedSchematicStructure();
		structure.setSize(
			root.get("Width").shortValue(),
			root.get("Height").shortValue(),
			root.get("Length").shortValue()
		);
		return structure;
	}

	public static Stream<LoadedSchematicStructure.Entity> readEntities(
		LoadedSchematicStructure structure, CompoundTag root
	) {
		ListTag entities = root.get("Entities").asList();
		if(entities.isEmpty()) {
			return Stream.empty();
		}
		return entities.items.stream()
			.map(Tag::asCompound)
			.map(tag -> new LoadedSchematicStructure.Entity(
				tag,
				tag.get("Id").stringValue(tag.get("id").stringValue()),
				readDoublePosition(tag, "Pos")
			));
	}

	public static Stream<LoadedSchematicStructure.TileEntity> readTileEntities(
		LoadedSchematicStructure structure, String key, CompoundTag root
	) {
		ListTag tileEntities = root.get(key).asList();
		if(tileEntities.isEmpty()) {
			return Stream.empty();
		}
		return tileEntities.items.stream()
			.map(Tag::asCompound)
			.map(tag -> new LoadedSchematicStructure.TileEntity(
				tag,
				tag.get("Id").stringValue(tag.get("id").stringValue()),
				readIntPosition(tag, "Pos")
			));
	}
}
