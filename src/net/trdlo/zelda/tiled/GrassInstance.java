package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;

public class GrassInstance extends TileInstance {
	private static final BufferedImage[] imgs = new BufferedImage[1];

	public GrassInstance(Tile tile) {
		super(tile);
		assert tile instanceof Grass;
		imgs[0] = ((Grass) tile).getImg();
	}

	@Override
	public BufferedImage[] getImgs(long time) {
		return imgs;
	}

}
