package net.trdlo.zelda;

import java.awt.Graphics2D;


public abstract class GameObjectInstance implements Comparable<GameObjectInstance> {
	protected float posX, posY;

	public GameObjectInstance(float posX, float posY) {
		this.posX = posX;
		this.posY = posY;
	}
	
	public void update() {
		
	}

	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}

	public float getMoveX() {
		return 0;
	}

	public float getMoveY() {
		return 0;
	}

	public abstract void render(Graphics2D graphics, float x, float y, float renderFraction);
	
	public abstract int getZIndex();
	
	@Override
	public int compareTo(GameObjectInstance t) {
		/*int zDiff = getZIndex() - t.getZIndex();
		if (zDiff != 0)
			return zDiff;
		else
			return hashCode() - t.hashCode();*/
		return getZIndex() - t.getZIndex();
	}
}
