package net.trdlo.zelda;

import java.awt.image.BufferedImage;

/**
 *
 * @author bayer
 */
public abstract class ZeldaCursor {

	private final BufferedImage image;
	private final float hotSpotX, hotSpotY;

	public ZeldaCursor(BufferedImage image, float hotSpotX, float hotSpotY) {
		this.image = image;
		this.hotSpotX = hotSpotX;
		this.hotSpotY = hotSpotY;
	}

	public BufferedImage getImage() {
		return image;
	}

	public float getHotSpotX() {
		return hotSpotX;
	}

	public float getHotSpotY() {
		return hotSpotY;
	}

}
