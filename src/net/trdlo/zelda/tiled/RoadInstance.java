package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;

public class RoadInstance extends TileInstance {

	private BufferedImage[] images;

	public RoadInstance(Tile tile) {
		super(tile);
		assert tile instanceof Road;
		images = new BufferedImage[1];
		images[0] = getRoadSet()[0];
	}

	private BufferedImage[] getRoadSet() {
		return ((Road) tile).getRoadSet();
	}

	private boolean isNeighbourRoad(int direction) {
		return neighbours[direction] != null && neighbours[direction] instanceof RoadInstance;
	}

	private int getRoadNeighbourCombo() {
		int combo = 0;
		for (int direction = 0; direction < World.FOUR_DIR_NEIGHBOUR_COUNT; direction++) {
			combo += isNeighbourRoad(direction) ? (1 << direction) : 0;
		}
		return combo;
	}

	private void updateImage() {
		int combo = getRoadNeighbourCombo();
		images[0] = getRoadSet()[combo];
	}

	@Override
	public void setNeighbour(int direction, TileInstance tileInstance) {
		super.setNeighbour(direction, tileInstance);
		updateImage();
	}

	@Override
	public BufferedImage[] getImgs(long time) {
		return images;
	}

}
