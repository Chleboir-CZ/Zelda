package net.trdlo.zelda.tiled;


public abstract class GameObject implements Identifiable {

	private final char identifier;
	public final float size;
	public final int zIndex;

	public static final int Z_GROUND = 0;
	public static final int Z_OVERHEAD = 1;
	public static final int Z_SPITHEIGHT = 2;
	public static final int Z_BIRDSPACE = 3;

	public GameObject(char identifier, float size, int zIndex) {
		this.identifier = identifier;
		this.size = size;
		this.zIndex = zIndex;
	}

	@Override
	public char getIdentifier() {
		return identifier;
	}

	public boolean isColliding() {
		return false;
	}

	public abstract GameObjectInstance getInstance(float x, float y, String args);
}
