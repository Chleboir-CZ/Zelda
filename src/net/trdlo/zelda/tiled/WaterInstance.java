package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class WaterInstance extends TileInstance {

	private static final int WATER_IMG_ID = 0;

	private int x, y;
	private BufferedImage[] images;

	public WaterInstance(Tile tile, int x, int y) {
		super(tile);
		assert tile instanceof Water;
		this.x = x;
		this.y = y;
		images = new BufferedImage[1];
	}

	private BufferedImage[] getWaterSet() {
		return ((Water) tile).getWaterSet();
	}

	private BufferedImage[] getGrassNeighbourSet() {
		return ((Water) tile).getGrassNeighbourSet();
	}

	private BufferedImage[] getGrassCornerSet() {
		return ((Water) tile).getGrassCornerSet();
	}

	private int getAnimFrame(long time) {
		return (int) ((time / 500 + x + y) % Water.WATER_FRAMES);
	}

	private boolean isNeighbourShore(int direction) {
		return neighbours[direction] != null && !(neighbours[direction] instanceof WaterInstance);
	}

	public int getGrassNeighbourCombo() {
		int combo = 0;
		for (int direction = 0; direction < World.FOUR_DIR_NEIGHBOUR_COUNT; direction++) {
			combo += isNeighbourShore(direction) ? (1 << direction) : 0;
		}
		return combo;
	}

	public List<Integer> getGrassCorners() {
		List<Integer> directions = new ArrayList<>(4);
		for (int direction = World.FOUR_DIR_NEIGHBOUR_COUNT; direction < World.NEIGHBOUR_COUNT; direction++) {
			if (!isNeighbourShore(direction - 4) && isNeighbourShore(direction) && !isNeighbourShore((direction - 3) % 4)) {
				directions.add(direction);
			}
		}
		return directions;
	}

	private void updateShoreImages() {
		List<BufferedImage> neighbourAndCornerImages = new ArrayList<>();
		int combo = getGrassNeighbourCombo();
		if (combo > 0) {
			neighbourAndCornerImages.add(getGrassNeighbourSet()[combo - 1]);
		}
		for (int i : getGrassCorners()) {
			neighbourAndCornerImages.add(getGrassCornerSet()[i - 4]);
		}
		images = new BufferedImage[1 + neighbourAndCornerImages.size()];
		for(int i = 0; i < neighbourAndCornerImages.size(); i++) {
			images[1 + i] = neighbourAndCornerImages.get(i);
		}
	}

	@Override
	public void setNeighbour(int direction, TileInstance tileInstance) {
		super.setNeighbour(direction, tileInstance);
		updateShoreImages();
	}

	@Override
	public BufferedImage[] getImgs(long time) {
		images[0] = getWaterSet()[getAnimFrame(time)];
		return images;
	}

}
