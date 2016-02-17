package net.trdlo.zelda;

/**
 *
 * @author bayer
 */
public class FrameFactory {
	private static ZeldaFrame instance = null;

	private static ZeldaFrame buildSwingInstance(GameInterface game) {
		assert instance == null;

		SwingZeldaFrame swingInstance = new SwingZeldaFrame(game.getWindowCaption(), game);
		swingInstance.setListeners();

		instance = swingInstance;
		game.setZeldaFrame(instance);

		return instance;
	}

	public static ZeldaFrame buildInstance(GameInterface game) {
		//tady by se mohlo větvit platform-specific
		return buildSwingInstance(game);
	}

	public static ZeldaFrame getInstance() {
		assert instance != null;

		return instance;
	}
}
