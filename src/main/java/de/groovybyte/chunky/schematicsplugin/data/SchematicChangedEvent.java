package de.groovybyte.chunky.schematicsplugin.data;

import de.groovybyte.chunky.schematicsplugin.utils.EventHandler;

public abstract class SchematicChangedEvent implements EventHandler.Event {

	private static class LoadingFailed extends SchematicChangedEvent {
		Exception loadException;

		private LoadingFailed(Exception loadException) {
			this.loadException = loadException;
		}

		@Override
		public Exception getLoadException() {
			return loadException;
		}

		@Override
		public boolean hasLoadingFailed() {
			return true;
		}

		@Override
		public String toString() {
			return "SchematicChangedEvent.LoadingFailed("+loadException+")";
		}
	}
	public static SchematicChangedEvent loadingFailed(Exception loadException) {
		return new LoadingFailed(loadException);
	}

	private static class Loading extends SchematicChangedEvent {
		UnloadedSchematicStructure structure;

		private Loading(UnloadedSchematicStructure structure) {
			this.structure = structure;
		}

		@Override
		public SchematicStructure getSchematic() {
			return structure;
		}

		@Override
		public boolean isLoading() {
			return true;
		}

		@Override
		public String toString() {
			return "SchematicChangedEvent.Loading("+structure+")";
		}
	}
	public static SchematicChangedEvent loading(UnloadedSchematicStructure structure) {
		return new Loading(structure);
	}

	private static class Loaded extends SchematicChangedEvent {
		LoadedSchematicStructure structure;

		private Loaded(LoadedSchematicStructure structure) {
			this.structure = structure;
		}

		@Override
		public SchematicStructure getSchematic() {
			return structure;
		}

		@Override
		public boolean isLoaded() {
			return true;
		}

		@Override
		public String toString() {
			return "SchematicChangedEvent.Loaded("+structure+")";
		}
	}
	public static SchematicChangedEvent loaded(LoadedSchematicStructure structure) {
		return new Loaded(structure);
	}

	private static class Unloaded extends SchematicChangedEvent {
		@Override
		public boolean hasUnloaded() {
			return true;
		}

		@Override
		public String toString() {
			return "SchematicChangedEvent.Unloaded()";
		}
	}
	public static SchematicChangedEvent unloaded() {
		return new Unloaded();
	}

	public SchematicStructure getSchematic() {
		return null;
	}

	public Exception getLoadException() {
		return null;
	}

	public boolean hasLoadingFailed() {
		return false;
	}

	public boolean hasSchematic() {
		return getSchematic() != null;
	}

	public boolean hasUnloaded() {
		return false;
	}

	public boolean isLoading() {
		return false;
	}

	public boolean isLoaded() {
		return false;
	}
}
