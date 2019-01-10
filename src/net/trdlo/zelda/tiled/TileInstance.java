package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;


public abstract class TileInstance {
	
	protected Tile tile;
	protected TileInstance[] neighbours;

	public TileInstance(Tile tile) {
		this.tile = tile;
		neighbours = new TileInstance[World.NEIGHBOUR_COUNT];
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	public abstract BufferedImage[] getImgs(long time);

	public void setNeighbour(int direction, TileInstance tileInstance) {
		neighbours[direction] = tileInstance;
	}
}
