package de.groovybyte.chunky.schematicsplugin.utils;

import java.util.ArrayList;
import java.util.List;

public interface EventHandler<E extends EventHandler.Event> {
	void addEventListener(Listener<E> listener);
	void removeEventListener(Listener<E> listener);

	class Impl<E extends Event> implements EventHandler<E> {
		List<Listener<E>> registeredListeners = new ArrayList<>(3);

		public void addEventListener(Listener<E> listener) {
			registeredListeners.add(listener);
		}

		public void removeEventListener(Listener<E> listener) {
			registeredListeners.remove(listener);
		}

		public void handle(E event) {
			registeredListeners.forEach(listener -> listener.call(event));
		}
	}

	interface Event {
	}

	@FunctionalInterface
	interface Listener<E extends Event> {
		void call(E event);
	}
}
