package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;

public class BushInstance extends TileInstance {
	private static final BufferedImage[] imgs = new BufferedImage[1];

	public BushInstance(Tile tile) {
		super(tile);
		assert tile instanceof Bush;
		imgs[0] = ((Bush) tile).getImg();
	}

	@Override
	public BufferedImage[] getImgs(long time) {
		return imgs;
	}

}
