package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Road extends Tile {

	public static final int ROAD_COMBOS = 16;
	private final BufferedImage[] roadSet;

	public Road() throws IOException {
		super('_');
		BufferedImage roads = ImageIO.read(new File("images/roads.png"));
		roadSet = new BufferedImage[ROAD_COMBOS];
		for (int i = 0; i < ROAD_COMBOS; i++) {
			roadSet[i] = roads.getSubimage(i * World.GRID_SIZE, 0, World.GRID_SIZE, World.GRID_SIZE);
		}
	}

	@Override
	public TileInstance createInstance(int x, int y) {
		return new RoadInstance(this);
	}

	@Override
	public boolean isPassable() {
		return false;
	}

	public BufferedImage[] getRoadSet() {
		return roadSet;
	}
}
