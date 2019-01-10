package net.trdlo.zelda.tiled;


public abstract class Tile implements Identifiable {
	private final char identifier;

	public Tile(char identifier) {
		this.identifier = identifier;
	}

	public abstract boolean isPassable();

	@Override
	public char getIdentifier() {
		return identifier;
	}

	public abstract TileInstance createInstance(int x, int y);
}
