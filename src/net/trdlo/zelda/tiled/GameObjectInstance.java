package net.trdlo.zelda.tiled;

import java.awt.Graphics2D;
import java.util.Comparator;

public abstract class GameObjectInstance {

	public final GameObject gameObject;
	protected float x, y;

	public GameObjectInstance(GameObject gameObject, float x, float y) {
		this.gameObject = gameObject;
		this.x = x;
		this.y = y;
	}

	public void update() {

	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getDX() {
		return 0;
	}

	public float getDY() {
		return 0;
	}

	public abstract void render(Graphics2D graphics, float x, float y, float renderFraction);

	public int getZIndex() {
		return gameObject.defaultZIndex;
	}

	public static Comparator<GameObjectInstance> zIndexComparator = (GameObjectInstance go1, GameObjectInstance go2) -> go1.getZIndex() - go2.getZIndex();

	public abstract void stateFromString(String args);

	public abstract String stateToString();
}
