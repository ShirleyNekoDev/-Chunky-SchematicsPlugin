package de.groovybyte.chunky.schematicsplugin.data;

import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.chunk.BlockPalette;
import se.llbit.math.Octree;
import se.llbit.math.QuickMath;
import se.llbit.math.Vector3;
import se.llbit.math.Vector3i;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.DoubleTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadedSchematicStructure extends UnloadedSchematicStructure {

	public LoadedSchematicStructure() {
		super(null);
	}

	public String format;

	/**
	 * width, height, length
	 */
	public Vector3i size;
	/**
	 * camera offset
	 */
	public Optional<Vector3> offset = Optional.empty();

	/**
	 * minecraft data version (if available)
	 */
	public int dataVersion = 0;

	public BlockPalette blockPalette = new BlockPalette();
	public int[] blocks;

	//	public BiomePalette biomePalette;

	public List<Entity> entities = new ArrayList<>();
	public List<TileEntity> tileEntities = new ArrayList<>();

	public void setSize(int width, int height, int length) {
		if(width <= 0 || height <= 0 || length <= 0) {
			throw new IllegalArgumentException("Size dimensions must be > 0");
		}
		this.size = new Vector3i(width, height, length);
		blocks = new int[width * height * length];
	}

	public void readOffset(CompoundTag tag) {
		Tag x = tag.get("WEOffsetX");
		Tag y = tag.get("WEOffsetY");
		Tag z = tag.get("WEOffsetZ");
		if(!x.isError() && !y.isError() && !z.isError()) {
			offset = Optional.of(new Vector3(
				x.doubleValue(x.intValue()),
				y.doubleValue(y.intValue()),
				z.doubleValue(z.intValue())
			));
		}
	}

	public static class Entity {

		String id;
		Vector3 position;
		CompoundTag tag;

		public Entity(CompoundTag tag, String id, double[] position) {
			this.tag = tag;
			this.id = id;
			this.position = new Vector3(position[0], position[1], position[2]);
		}
	}

	public static class TileEntity {

		String id;
		Vector3i position;
		/**
		 * TODO: might be flattened or not (-> "Data")
		 */
		CompoundTag tag;

		public TileEntity(CompoundTag tag, String id, int[] position) {
			this.tag = tag;
			this.id = id;
			this.position = new Vector3i(position[0], position[1], position[2]);
		}
	}

	@Override
	public String toString() {
		return String.format(
			"SchematicStructure{file=%s, size=%d⨯%d⨯%d, palette=%d, entities=%d, tileEntities=%d}",
			file,
			size.x,
			size.y,
			size.z,
			blockPalette.getPalette().size(),
			entities.size(),
			tileEntities.size()
		);
	}
}
