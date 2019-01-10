package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bush extends Tile {

	private final BufferedImage img;

	public Bush() throws IOException {
		super('#');
		img = ImageIO.read(new File("images/bush.png"));
	}

	@Override
	public TileInstance createInstance(int x, int y) {
		return new BushInstance(this);
	}

	@Override
	public boolean isPassable() {
		return false;
	}

	public BufferedImage getImg() {
		return img;
	}
}
