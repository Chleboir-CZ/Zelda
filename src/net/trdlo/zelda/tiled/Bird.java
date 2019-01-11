package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bird extends SimplyAnimatedObject {
//	private BufferedImage img;

	private World world;
	public final int FRAME_COUNT = 2;

	public Bird(World world) throws IOException {
		super('^', 1.0f, Z_BIRDSPACE);
		this.world = world;

		BufferedImage[] img = new BufferedImage[2];
		for (int i = 0; i < FRAME_COUNT; i++) {
			img[i] = ImageIO.read(new File("images/bird" + i + ".png"));
		}
		setImages(img);
	}

	@Override
	public GameObjectInstance getInstance(float x, float y, String args) {
		return new BirdInstance(this, x, y, world);
	}

}
