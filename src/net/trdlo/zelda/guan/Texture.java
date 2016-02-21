package net.trdlo.zelda.guan;

import java.awt.image.BufferedImage;

/**
 *
 * @author bayer
 */
public class Texture {

	private final BufferedImage image;
	private final Point position;

	public Texture(BufferedImage image, Point position) {
		this.image = image;
		this.position = position;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Point getPosition() {
		return position;
	}
}
