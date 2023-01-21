package de.groovybyte.chunky.schematicsplugin;

import de.groovybyte.chunky.schematicsplugin.data.LoadedSchematicStructure;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.stage.Modality;
import se.llbit.chunky.PersistentSettings;
import se.llbit.chunky.block.Air;
import se.llbit.chunky.chunk.BlockPalette;
import se.llbit.chunky.renderer.projection.ProjectionMode;
import se.llbit.chunky.renderer.scene.Camera;
import se.llbit.chunky.renderer.scene.Scene;
import se.llbit.chunky.renderer.scene.SceneFactory;
import se.llbit.chunky.world.ChunkPosition;
import se.llbit.chunky.world.Material;
import se.llbit.fxutil.Dialogs;
import se.llbit.log.Log;
import se.llbit.math.*;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class ChunkySceneInjector {

	private static final Field paletteField;
	private static final Field worldOctreeField;
	private static final Field waterOctreeField;
	private static final Field chunksField;

	static {
		try {
			paletteField = Scene.class.getDeclaredField("palette");
			paletteField.setAccessible(true);
			worldOctreeField = Scene.class.getDeclaredField("worldOctree");
			worldOctreeField.setAccessible(true);
			waterOctreeField = Scene.class.getDeclaredField("waterOctree");
			waterOctreeField.setAccessible(true);
			chunksField = Scene.class.getDeclaredField("chunks");
			chunksField.setAccessible(true);
		} catch(ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to hook into Chunky", ex);
		}
	}

	public static void resetScene(Scene scene, SceneFactory sceneFactory) {
		Scene newScene = sceneFactory.newScene();
		scene.copyState(newScene, true); // reset octree and chunks
		String newSceneName = "default_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		scene.resetScene(newSceneName, sceneFactory); // reset rest
	}

	private static Octree structureToOctree(LoadedSchematicStructure structure) {
		Vector3i size = structure.size;
		int maxDimension = Math.max(size.x, Math.max(size.y, size.z));
		int sizePow2 = QuickMath.nextPow2(maxDimension);
		int requiredDepth = QuickMath.log2(sizePow2);

		// TODO: check for degenerated octrees (maxDimension >> minDimension)
		if(sizePow2 >= 1024) {
			Alert alert = Dialogs.createAlert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Big Octree");
			alert.setHeaderText("This schematic is very large. It will be memory intensive and slow to load.");
			alert.setContentText("Proceed?");
			if(alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isEmpty()) {
				return null;
			}
		}

		int[] octreeBlocks = new int[sizePow2 * sizePow2 * sizePow2];
		for(int y = 0; y < size.y; y++) {
			for(int z = 0; z < size.z; z++) {
				System.arraycopy(structure.blocks,
					(y * size.z + z) * size.x,
					octreeBlocks,
					(z * sizePow2 + y) * sizePow2,
					size.x
				);
			}
		}

		// TODO: support water octree

		Octree octree = new Octree(PersistentSettings.getOctreeImplementation(), requiredDepth);
		octree.setCube(requiredDepth, octreeBlocks, 0, 0, 0);
		return octree;
	}

	public static void injectIntoScene(
		LoadedSchematicStructure structure,
		Scene scene,
		boolean alignCamera
	) {
		// TODO: load entities / tileEntities

		Octree octree = structureToOctree(structure);
		if(octree == null)
			throw new IllegalStateException("Failed to create octree");
		octree.startFinalization();
		// TODO: finalize octrees
		octree.endFinalization();

		// TODO: support biomes
		scene.setBiomeColorsEnabled(false);

		try {
			paletteField.set(scene, structure.blockPalette);

			scene.getOrigin().set(0, 0, 0);

			// TODO: support water octree
			worldOctreeField.set(scene, octree);
			waterOctreeField.set(
				scene,
				// use empty water octree for now
				new Octree(PersistentSettings.getOctreeImplementation(), octree.getDepth())
			);

			scene.camera().setWorldSize(1<<octree.getDepth());

			// there are no chunks to load, but the scene would be "illegal" if no chunk was contained in it
			Collection<ChunkPosition> chunks = (Collection<ChunkPosition>) chunksField.get(scene);
			chunks.clear();
			chunks.add(ChunkPosition.get(0, 0));
		} catch(IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}

		if(alignCamera) {
			alignCamera(structure, octree, scene);
		}
	}

	private static void alignCamera(LoadedSchematicStructure structure, Octree octree, Scene scene) {
		Vector3 center = new Vector3(structure.size.x, structure.size.y, structure.size.z);
		center.scale(0.5);
		double minTargetDistance = center.length();

		// use provided camera offset if available
		Vector3 offset = structure.offset.orElseGet(() -> {
			// otherwise position at front of structure
			Vector3 front = new Vector3(center);
			front.z = -1.0; // to not collide with blocks
			return front;
		});
		// move camera out of blocks
		if(isSolidBlock(octree, structure.blockPalette, (int) offset.x, (int) offset.y, (int) offset.z)) {
			while(isSolidBlock(octree,
				structure.blockPalette,
				(int) offset.x,
				(int) offset.y,
				(int) offset.z
			) && offset.y <= scene.getYClipMax()) {
				offset.y += 1.0;
			}
			offset.y += minTargetDistance;
		}
		scene.camera().setPosition(offset);

		// point camera at center of structure
		double dx = offset.x - center.x;
		double dy = offset.y - center.y;
		double dz = offset.z - center.z;
		// Chunky camera pitch & yaw are weird
		double yaw = -Math.atan2(dz, dx);
		double pitch = Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - QuickMath.HALF_PI;
		scene.camera().setView(yaw, pitch, 0.0);

		if(structure.offset.isEmpty()) {
			// adjust distance for frontal view
			Ray ray = new Ray();
			if(scene.traceTarget(ray)) {
				if(ray.distance < minTargetDistance) {
					scene.camera().moveBackward(minTargetDistance - ray.distance);
				}
			}
		}
	}

	private static boolean isSolidBlock(Octree octree, BlockPalette blockPalette, int x, int y, int z) {
		Material block = octree.getMaterial(x, y, z, blockPalette);
		return !(block instanceof Air);
	}
}
