package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Water extends Tile {

	public static final int WATER_FRAMES = 3;
	private final BufferedImage[] waterSet;
	public static final int GRASS_NEIGHBOUR_COMBOS = 15;
	private final BufferedImage[] grassNeighbourSet;
	public static final int GRASS_CORNERS = 4;
	private final BufferedImage[] grassCornerSet;

	public Water() throws IOException {
		super('~');
		BufferedImage water = ImageIO.read(new File("images/water.png"));
		waterSet = new BufferedImage[WATER_FRAMES];
		for (int i = 0; i < WATER_FRAMES; i++) {
			waterSet[i] = water.getSubimage(i * World.GRID_SIZE, 0, World.GRID_SIZE, World.GRID_SIZE);
		}
		BufferedImage water_to_grass = ImageIO.read(new File("images/water_to_grass.png"));
		grassNeighbourSet = new BufferedImage[GRASS_NEIGHBOUR_COMBOS];
		for (int i = 0; i < GRASS_NEIGHBOUR_COMBOS; i++) {
			grassNeighbourSet[i] = water_to_grass.getSubimage(i * World.GRID_SIZE, 0, World.GRID_SIZE, World.GRID_SIZE);
		}
		grassCornerSet = new BufferedImage[GRASS_CORNERS];
		for (int i = 0; i < GRASS_CORNERS; i++) {
			grassCornerSet[i] = water_to_grass.getSubimage((GRASS_NEIGHBOUR_COMBOS + i) * World.GRID_SIZE, 0, World.GRID_SIZE, World.GRID_SIZE);
		}
	}

	@Override
	public TileInstance createInstance(int x, int y) {
		return new WaterInstance(this, x, y);
	}

	@Override
	public boolean isPassable() {
		return false;
	}

	public BufferedImage[] getWaterSet() {
		return waterSet;
	}

	public BufferedImage[] getGrassNeighbourSet() {
		return grassNeighbourSet;
	}

	public BufferedImage[] getGrassCornerSet() {
		return grassCornerSet;
	}
}
