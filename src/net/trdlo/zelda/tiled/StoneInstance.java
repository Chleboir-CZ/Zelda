package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;

public class StoneInstance extends TileInstance {
	private static final BufferedImage[] imgs = new BufferedImage[1];

	public StoneInstance(Tile tile) {
		super(tile);
		assert tile instanceof Stone;
		imgs[0] = ((Stone) tile).getImg();
	}

	@Override
	public BufferedImage[] getImgs(long time) {
		return imgs;
	}

}
