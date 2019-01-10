package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Grass extends Tile {

	private final BufferedImage img;

	public Grass() throws IOException {
		super('.');
		img = ImageIO.read(new File("images/grass.png"));
	}

	@Override
	public TileInstance createInstance(int x, int y) {
		return new GrassInstance(this);
	}

	@Override
	public boolean isPassable() {
		return true;
	}

	public BufferedImage getImg() {
		return img;
	}
}
