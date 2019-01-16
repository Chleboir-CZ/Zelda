package net.trdlo.zelda.tiled;

public abstract class Tile {

	public final char identifier;

	public Tile(char identifier) {
		this.identifier = identifier;
	}

	public abstract boolean isPassable();

	public abstract TileInstance createInstance(int x, int y);
}
