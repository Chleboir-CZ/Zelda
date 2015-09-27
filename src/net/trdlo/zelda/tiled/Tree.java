package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Tree extends GameObject {

	BufferedImage img;

	public Tree() throws Exception {
		super('T', 0.5f, Z_OVERHEAD);

		try {
			img = ImageIO.read(new File("images/tree.png"));
		} catch (IOException ex) {
			throw new Exception("Tree instantion did not load it's graphics correctly.", ex);
		}
	}

	@Override
	public GameObjectInstance getInstance(float x, float y, String args) {
		return new TreeInstance(this, x, y);
	}

}
