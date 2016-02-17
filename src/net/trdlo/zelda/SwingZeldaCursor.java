package net.trdlo.zelda;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author bayer
 */
class SwingZeldaCursor extends ZeldaCursor {
	private final Cursor swingCursor;

	public SwingZeldaCursor(BufferedImage image, float hotSpotX, float hotSpotY) {
		super(image, hotSpotX, hotSpotY);

		int px = (int) (image.getWidth() * hotSpotX);
		int py = (int) (image.getHeight() * hotSpotY);

		swingCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(px, py), "");
	}

	public Cursor getSwingCursor() {
		return swingCursor;
	}
}
