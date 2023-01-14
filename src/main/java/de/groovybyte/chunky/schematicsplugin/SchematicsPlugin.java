package de.groovybyte.chunky.schematicsplugin;

import de.groovybyte.chunky.schematicsplugin.gui.PluginTab;
import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.chunky.main.ChunkyOptions;
import se.llbit.chunky.ui.ChunkyFx;
import se.llbit.chunky.ui.render.RenderControlsTabTransformer;

public class SchematicsPlugin implements Plugin {

	@Override
	public void attach(Chunky chunky) {
		RenderControlsTabTransformer prev = chunky.getRenderControlsTabTransformer();
		chunky.setRenderControlsTabTransformer(tabs -> {
			tabs = prev.apply(tabs);
			tabs.add(new PluginTab(chunky));
			return tabs;
		});
	}

	public static void main(String[] args) {
		// Start Chunky normally with this plugin attached.
		Chunky.loadDefaultTextures();
		Chunky chunky = new Chunky(ChunkyOptions.getDefaults());
		new SchematicsPlugin().attach(chunky);
		ChunkyFx.startChunkyUI(chunky);
	}
}
