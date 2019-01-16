package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Tree extends GameObject {

	BufferedImage img;

	public Tree() throws IOException {
		super('T', 0.5f, Z_OVERHEAD);
		img = ImageIO.read(new File("images/tree.png"));
	}

	@Override
	public GameObjectInstance getInstance(float x, float y, String args) {
		return new TreeInstance(this, x, y);
	}

	@Override
	public GameObjectInstance getInstanceWithDefaults(float x, float y) {
		return new TreeInstance(this, x, y);
	}
}
